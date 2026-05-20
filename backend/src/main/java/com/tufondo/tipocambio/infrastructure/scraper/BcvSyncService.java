package com.tufondo.tipocambio.infrastructure.scraper;

import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Servicio que orquesta el scraping + persistencia del tipo de cambio BCV
 * (issue #231).
 *
 * <p>Dos puntos de entrada:
 * <ul>
 *   <li>{@link #sincronizarDesdeBcv()} — llamado por el job programado y por
 *       el endpoint admin manual.</li>
 *   <li>{@link #ejecutarJobDiario()} — cron a las 08:30 hora Venezuela
 *       (después de que BCV publique la tasa del día).</li>
 * </ul>
 * Si el scraping falla, NO se borra la tasa anterior — preferimos servir un
 * valor stale (de hace 1+ día) que dejar al frontend sin datos.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BcvSyncService {

    private final BcvScraperService bcvScraperService;
    private final TipoCambioRepository tipoCambioRepository;

    /**
     * Job programado: cada 2 horas entre 8 AM y 6 PM hora Venezuela
     * (8, 10, 12, 14, 16, 18 → 6 ejecuciones/día).
     *
     * <p>Antes corría 1 vez al día (08:30 Caracas). Problemas observados en PROD:
     * <ul>
     *   <li>Si el backend estaba reiniciándose a esa hora (deploy nocturno),
     *       se perdía el cron y la tasa quedaba 24h+ stale.</li>
     *   <li>BCV publica usualmente entre 6-8 AM pero ocasionalmente actualiza
     *       más tarde — una sola ejecución no captura ese caso.</li>
     * </ul>
     * Solución: scrapear cada 2h en horario laboral del BCV. Es idempotente
     * (si la tasa ya existe para esa fecha valor, no se duplica), así que las
     * ejecuciones extra solo loguean "ya existía" y no afectan al BCV ni a la
     * BD. 6 requests/día al sitio del BCV es muy razonable, sin riesgo de
     * rate-limit.</p>
     */
    @Scheduled(cron = "0 0 8-18/2 * * *", zone = "America/Caracas")
    public void ejecutarJobDiario() {
        log.debug("Iniciando job programado de sincronización BCV");
        try {
            SincronizacionResultado resultado = sincronizarDesdeBcv();
            // Solo log INFO si se insertó algo nuevo; las re-ejecuciones idempotentes
            // se loguean en DEBUG para no inundar logs con "ya existía" 6 veces al día.
            if (resultado.insertada()) {
                log.info("Job BCV insertó tasa nueva: {}", resultado);
            } else {
                log.debug("Job BCV idempotente (ya existía): {}", resultado);
            }
        } catch (Exception e) {
            // CRÍTICO: el job nunca debe lanzar excepción (rompería el scheduler).
            log.error("Error en job programado BCV — la tasa anterior se preserva: {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Catch-up al arrancar el backend: si la última tasa en BD es vieja
     * (> 1 día desde la última fecha de valor) o no hay ninguna, dispara
     * un scrape inmediato.
     *
     * <p>Antes (bug observado 19-may-2026): el backend se reiniciaba para un
     * deploy DESPUÉS de la hora del cron, y la tasa quedaba 24h+ stale hasta
     * la próxima ejecución programada. Este catch-up cubre ese caso.</p>
     *
     * <p>Lo lanzamos en un Thread aparte porque el scrape hace I/O HTTP al BCV
     * y puede tardar varios segundos — no queremos bloquear el startup del
     * Spring context. Si el scrape falla, se loguea pero no rompe el arranque
     * (preferimos un backend vivo con tasa stale que uno apagado).</p>
     */
    @PostConstruct
    public void programarCatchUpAlArrancar() {
        // Disparamos en otro thread para no bloquear startup ni el bean wiring.
        // No usamos @Async directo en este método para evitar el típico bug de
        // self-invocation (Spring AOP no intercepta llamadas desde el propio
        // bean), preferimos un Thread simple acá.
        new Thread(this::ejecutarCatchUpStartup, "bcv-startup-catchup").start();
    }

    /**
     * Lógica del catch-up. Separado del @PostConstruct para que el thread
     * acceda al método @Transactional vía proxy Spring correctamente.
     *
     * <p>Estrategia: <b>scrapear SIEMPRE al arrancar</b>, sin condición de
     * "fresca". El use case ya es idempotente — si ya existe tasa para la
     * fecha valor que devuelve el BCV, simplemente loguea "ya existía" y
     * no inserta duplicado. Esto:</p>
     *
     * <ul>
     *   <li>Evita el bug 20-may-2026: comparaba {@code ultima.getFecha()}
     *       (fecha valor BCV) contra hoy, y como BCV publica el día anterior
     *       la tasa del día siguiente, "fecha valor = hoy" no significaba
     *       "scrapeada hoy". Resultado: catch-up decía "fresca, skip" cuando
     *       en realidad llevábamos 3 días sin scrapear.</li>
     *   <li>Garantiza que cada deploy actualiza la tasa, sin importar la
     *       hora.</li>
     *   <li>No tiene costo: BCV es HTML público, 1 request extra al arrancar
     *       es despreciable.</li>
     * </ul>
     */
    public void ejecutarCatchUpStartup() {
        try {
            // Esperar 5s para que el resto del Spring context termine de inicializar
            // (HikariCP, etc.) — evita race conditions en logs muy tempranos.
            Thread.sleep(5_000);

            Optional<TipoCambio> ultima = tipoCambioRepository.buscarTasaActual();
            if (ultima.isPresent()) {
                LocalDate hoy = LocalDate.now(ZoneId.of("America/Caracas"));
                long diasFechaValor = ChronoUnit.DAYS.between(ultima.get().getFecha(), hoy);
                log.info("Catch-up BCV al arrancar: última fecha valor={} (Δ{}d vs hoy Caracas={}), " +
                                "scrapeando para garantizar tasa fresca…",
                        ultima.get().getFecha(), diasFechaValor, hoy);
            } else {
                log.info("Catch-up BCV al arrancar: no hay ninguna tasa en BD, scrapeando…");
            }

            SincronizacionResultado r = sincronizarDesdeBcv();
            log.info("Catch-up BCV al arrancar completado: {}", r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Defensivo: NUNCA romper el arranque por un scrape fallido.
            log.error("Catch-up BCV al arrancar falló — la tasa anterior se preserva: {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Sincroniza la tasa del BCV con la BD.
     *
     * <ul>
     *   <li>Si ya existe una tasa para la fecha valor del BCV → no hace nada
     *       (idempotente).</li>
     *   <li>Si no existe → la inserta.</li>
     * </ul>
     *
     * Para BCV usamos {@code tasaCompra = tasaVenta} porque el BCV no publica
     * spread (es una tasa de referencia única). Los servicios cambiarios reales
     * aplican spread por su cuenta.
     *
     * @return resultado con detalle (insertada / ya existente / error)
     */
    @Transactional
    public SincronizacionResultado sincronizarDesdeBcv() {
        BcvScraperService.TasaScrapeada scrapeada = bcvScraperService.scrapearTasaBcv();

        Optional<TipoCambio> existente = tipoCambioRepository.buscarPorFecha(scrapeada.fechaValor());
        if (existente.isPresent()) {
            log.info("Tasa BCV para {} ya existe en BD ({}), skip insert.",
                    scrapeada.fechaValor(), existente.get().getTasaVenta());
            return new SincronizacionResultado(false, scrapeada.fechaValor(),
                    scrapeada.tasaUsdEnBs(), "Ya existía en BD");
        }

        TipoCambio nuevo = TipoCambio.builder()
                .fecha(scrapeada.fechaValor())
                .tasaCompra(scrapeada.tasaUsdEnBs())  // BCV: sin spread, compra = venta
                .tasaVenta(scrapeada.tasaUsdEnBs())
                .fuente("BCV")
                .creadoPor(null)  // origen sistema, no admin
                .createdAt(Instant.now())
                .build();

        tipoCambioRepository.guardar(nuevo);
        log.info("Tasa BCV insertada en BD: fecha={} tasa={}",
                scrapeada.fechaValor(), scrapeada.tasaUsdEnBs());
        return new SincronizacionResultado(true, scrapeada.fechaValor(),
                scrapeada.tasaUsdEnBs(), "Insertada");
    }

    /**
     * Resultado de una ejecución de sincronización.
     *
     * @param insertada {@code true} si se insertó una fila nueva, {@code false}
     *                  si ya existía la tasa para esa fecha.
     */
    public record SincronizacionResultado(
            boolean insertada,
            java.time.LocalDate fecha,
            java.math.BigDecimal tasa,
            String detalle
    ) {}
}
