package com.tufondo.compliance.application.service;

import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion;
import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion.TipoOperacion;
import com.tufondo.compliance.domain.repository.ConsentimientoLocdoftRepository;
import com.tufondo.parametros.domain.repository.ParametroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Servicio compartido para validar y registrar declaraciones LOCDOFT en
 * operaciones financieras grandes (#218 PR-C).
 *
 * <p>Lo invocan {@code RealizarDepositoUseCase} y {@code RealizarRetiroUseCase}
 * antes de ejecutar la operación. Si el monto NO supera el umbral, devuelve
 * {@code null} (operación normal sin declaración). Si SÍ supera el umbral y
 * el flag {@code confirmaOrigenLicito} no es {@code true}, lanza
 * {@link LocdoftConsentimientoRequeridoException}. Si el flag es true,
 * persiste el consentimiento y devuelve el registro.</p>
 *
 * <p><strong>Resiliencia:</strong> si lectura del parámetro falla por
 * cualquier razón transient, NO se bloquea la operación — se loguea y
 * se asume "sin umbral" (fail-open). Razón: el umbral es defensa
 * adicional, no la única protección; impedir todas las operaciones por
 * un bug del módulo de parámetros sería peor que dejar pasar
 * operaciones grandes durante el bug.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocdoftOperacionService {

    private static final String KEY_UMBRAL_VES = "LOCDOFT_UMBRAL_VES";
    private static final String KEY_UMBRAL_USD = "LOCDOFT_UMBRAL_USD";

    private final ParametroRepository parametroRepository;
    private final ConsentimientoLocdoftRepository consentimientoRepository;

    public record DatosOperacion(
            UUID socioId,
            UUID cuentaAhorroId,
            TipoOperacion tipoOperacion,
            BigDecimal monto,
            String moneda,
            boolean confirmaOrigenLicito,
            String origenFondos,
            String ipOrigen,
            String userAgent,
            String sessionId,
            String requestId
    ) {}

    /**
     * Valida el umbral y persiste el consentimiento si aplica.
     *
     * @return el consentimiento persistido (con id) si la operación supera
     *         el umbral, o {@code null} si no lo supera (operación normal).
     * @throws LocdoftConsentimientoRequeridoException si supera el umbral y
     *         el cliente no envió {@code confirmaOrigenLicito=true}.
     */
    @Transactional
    public ConsentimientoLocdoftOperacion validarYRegistrar(DatosOperacion datos) {
        BigDecimal umbral = obtenerUmbral(datos.moneda());
        if (umbral == null || datos.monto().compareTo(umbral) <= 0) {
            return null; // operación normal — no requiere declaración
        }

        // Supera el umbral → requiere confirmación
        if (!datos.confirmaOrigenLicito()) {
            throw new LocdoftConsentimientoRequeridoException(datos.monto(), datos.moneda(), umbral);
        }

        // Persistir como evidencia (movimientoId queda null hasta que la
        // operación principal lo asocie con `asociarConMovimiento`).
        return consentimientoRepository.guardar(ConsentimientoLocdoftOperacion.builder()
                .socioId(datos.socioId())
                .cuentaAhorroId(datos.cuentaAhorroId())
                .movimientoId(null)
                .tipoOperacion(datos.tipoOperacion())
                .monto(datos.monto())
                .moneda(datos.moneda())
                .umbralAplicado(umbral)
                .aceptaOrigenLicito(true)
                .origenFondos(datos.origenFondos())
                .ipOrigen(datos.ipOrigen())
                .userAgent(datos.userAgent())
                .sessionId(datos.sessionId())
                .requestId(datos.requestId())
                .build());
    }

    /** Lee el umbral vigente. Fail-open: si no se puede leer, devuelve null. */
    public BigDecimal obtenerUmbral(String moneda) {
        String key = "USD".equalsIgnoreCase(moneda) ? KEY_UMBRAL_USD : KEY_UMBRAL_VES;
        try {
            return parametroRepository.buscarPorKey(key)
                    .map(p -> {
                        try {
                            return new BigDecimal(p.valor());
                        } catch (NumberFormatException nfe) {
                            log.warn("Parámetro {} tiene valor inválido '{}' — fail-open",
                                    key, p.valor());
                            return null;
                        }
                    })
                    .orElse(null);
        } catch (Throwable t) {
            log.warn("No se pudo leer parámetro {} ({}) — fail-open en LOCDOFT umbral",
                    key, t.getMessage());
            return null;
        }
    }

    /**
     * Asocia el consentimiento ya persistido con el movimiento real
     * que se acaba de crear. Resiliente: si falla, log y skip — el
     * consentimiento queda con movimientoId=null pero igual sirve como
     * evidencia. La operación principal NO se aborta por esto.
     */
    public void asociarConMovimiento(UUID consentimientoId, UUID movimientoId) {
        if (consentimientoId == null || movimientoId == null) return;
        try {
            consentimientoRepository.asociarConMovimiento(consentimientoId, movimientoId);
        } catch (Throwable t) {
            log.error("No se pudo asociar consentimiento {} con movimiento {}: {}",
                    consentimientoId, movimientoId, t.getMessage(), t);
        }
    }
}
