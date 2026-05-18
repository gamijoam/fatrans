package com.tufondo.tipocambio.infrastructure.scraper;

import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
     * Job programado: 08:30 AM hora Venezuela, todos los días.
     * El BCV usualmente publica la tasa del día entre 6 y 8 AM.
     */
    @Scheduled(cron = "0 30 8 * * *", zone = "America/Caracas")
    public void ejecutarJobDiario() {
        log.info("Iniciando job programado de sincronización BCV");
        try {
            SincronizacionResultado resultado = sincronizarDesdeBcv();
            log.info("Job BCV diario completado: {}", resultado);
        } catch (Exception e) {
            // CRÍTICO: el job nunca debe lanzar excepción (rompería el scheduler).
            log.error("Error en job programado BCV — la tasa anterior se preserva: {}",
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
