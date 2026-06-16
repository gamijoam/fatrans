package com.tufondo.contabilidad.application.usecase;

import com.tufondo.contabilidad.application.dto.RegistrarAsientoCommand;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.repository.AsientoContableRepository;
import com.tufondo.contabilidad.domain.repository.CuentaContableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de aplicación para asientos contables.
 *
 * <p>Es el punto de entrada para registrar/anular asientos. Combina:</p>
 *
 * <ul>
 *   <li>Validación de invariantes del dominio (ya cubiertas por
 *       {@link AsientoContable#crear}).</li>
 *   <li>Validación de integridad cruzada con el plan de cuentas: las
 *       cuentas referenciadas deben existir, aceptar movimientos y estar
 *       activas. Esto NO se valida en el dominio puro porque requiere
 *       acceso al repositorio.</li>
 *   <li>Asignación de correlativo (delegado al adapter del repositorio
 *       vía secuencia BD).</li>
 *   <li>Persistencia atómica (cabecera + partidas).</li>
 * </ul>
 *
 * <p>Cada operación corre en su propia transacción — fallos de validación
 * impiden el commit y dejan la BD intacta.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsientoContableService {

    private final AsientoContableRepository asientoRepository;
    private final CuentaContableRepository cuentaRepository;

    /**
     * Registra un asiento contable nuevo.
     *
     * <p>Pasos (en orden):</p>
     * <ol>
     *   <li>Resolver los códigos de cuenta a UUIDs vía el plan de cuentas.
     *       Si alguna cuenta no existe → {@link AsientoContableException}.</li>
     *   <li>Validar que TODAS las cuentas referenciadas sean hojas operativas
     *       (acepta_movimientos=true) y estén activas.</li>
     *   <li>Construir las {@link PartidaAsiento} (que valida debe XOR haber
     *       en su construcción).</li>
     *   <li>Construir el {@link AsientoContable} (que valida la invariante
     *       de partida doble: Σdebe = Σhaber).</li>
     *   <li>Persistir vía el repositorio (que asigna correlativo BD y
     *       persiste atómicamente).</li>
     * </ol>
     *
     * @throws AsientoContableException si alguna validación falla
     */
    @Transactional
    public AsientoContable registrar(RegistrarAsientoCommand cmd) {
        Objects.requireNonNull(cmd, "command requerido");
        Objects.requireNonNull(cmd.partidas(), "partidas requeridas");
        if (cmd.partidas().isEmpty()) {
            throw new AsientoContableException("partidas no puede estar vacía");
        }

        // Paso 1: resolver códigos → UUIDs en una sola query batch lookup.
        // Hacemos lookup individual porque solo son 2-10 cuentas por asiento
        // típico — un join de Map suficiente, sin necesidad de IN()
        // query a propósito.
        Map<String, CuentaContable> cuentasPorCodigo = new HashMap<>();
        for (RegistrarAsientoCommand.Partida p : cmd.partidas()) {
            if (cuentasPorCodigo.containsKey(p.codigoCuenta())) continue;
            CuentaContable c = cuentaRepository.buscarPorCodigo(p.codigoCuenta())
                    .orElseThrow(() -> new AsientoContableException(
                            "cuenta no encontrada en el plan: " + p.codigoCuenta()));
            cuentasPorCodigo.put(p.codigoCuenta(), c);
        }

        // Paso 2: validar que las cuentas acepten movimientos y estén activas.
        for (CuentaContable c : cuentasPorCodigo.values()) {
            if (!c.isAceptaMovimientos()) {
                throw new AsientoContableException(String.format(
                        "cuenta %s (%s) es totalizadora y no acepta movimientos directos. " +
                                "Usá una cuenta hoja del mismo grupo.",
                        c.getCodigo(), c.getNombre()));
            }
            if (!c.isActiva()) {
                throw new AsientoContableException(String.format(
                        "cuenta %s (%s) está inactiva y no puede recibir nuevos asientos",
                        c.getCodigo(), c.getNombre()));
            }
        }

        // Paso 3: construir partidas. PartidaAsiento.alDebe/alHaber valida
        // monto > 0, debe XOR haber, etc.
        List<PartidaAsiento> partidas = new ArrayList<>();
        int orden = 1;
        for (RegistrarAsientoCommand.Partida p : cmd.partidas()) {
            CuentaContable cuenta = cuentasPorCodigo.get(p.codigoCuenta());
            BigDecimal debe = p.debe() == null ? BigDecimal.ZERO : p.debe();
            BigDecimal haber = p.haber() == null ? BigDecimal.ZERO : p.haber();
            try {
                if (debe.signum() > 0 && haber.signum() > 0) {
                    throw new AsientoContableException(
                            "una partida no puede tener DEBE y HABER simultáneamente: cuenta " +
                                    p.codigoCuenta());
                }
                if (debe.signum() == 0 && haber.signum() == 0) {
                    throw new AsientoContableException(
                            "una partida debe tener DEBE o HABER positivo: cuenta " + p.codigoCuenta());
                }
                if (debe.signum() > 0) {
                    partidas.add(PartidaAsiento.alDebe(cuenta.getId(), debe, orden, p.glosa()));
                } else {
                    partidas.add(PartidaAsiento.alHaber(cuenta.getId(), haber, orden, p.glosa()));
                }
            } catch (IllegalArgumentException e) {
                // Re-throw como excepción del módulo para que el handler global
                // la mapee a HTTP 422 con mensaje claro al cliente.
                throw new AsientoContableException(
                        "partida inválida en cuenta " + p.codigoCuenta() + ": " + e.getMessage(), e);
            }
            orden++;
        }

        // Paso 4: construir el asiento (valida partida doble Σdebe=Σhaber).
        AsientoContable asiento;
        try {
            asiento = AsientoContable.crear(
                    cmd.fechaContable(), cmd.glosa(), cmd.origen(),
                    cmd.referenciaExterna(), cmd.creadoPorUsuarioId(),
                    cmd.asientoReversaId(), partidas);
        } catch (IllegalArgumentException e) {
            throw new AsientoContableException("asiento inválido: " + e.getMessage(), e);
        }

        // Paso 5: persistir.
        AsientoContable persistido = asientoRepository.guardar(asiento);
        log.info("Asiento contable registrado: numero={} origen={} totalDebe={} fecha={}",
                persistido.getNumero(),
                persistido.getOrigen(),
                persistido.totalDebe().toPlainString(),
                persistido.getFechaContable());
        return persistido;
    }

    /**
     * Anula un asiento existente. No genera el asiento de reversión
     * automáticamente — eso lo hace {@link #revertir} (sub-issue #273).
     * Esta operación solo marca el estado, dejando el asiento visible para
     * auditoría pero ya no afectando saldos.
     */
    @Transactional
    public AsientoContable anular(UUID asientoId, String motivo) {
        AsientoContable a = asientoRepository.buscarPorId(asientoId)
                .orElseThrow(() -> new AsientoContableException(
                        "asiento no encontrado: " + asientoId));
        AsientoContable anulado;
        try {
            anulado = a.anular(motivo);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new AsientoContableException(e.getMessage(), e);
        }
        AsientoContable persistido = asientoRepository.guardar(anulado);
        log.info("Asiento anulado: numero={} motivo={}", persistido.getNumero(), motivo);
        return persistido;
    }

    /** Wrapper de lectura — útil para reportes y endpoints. */
    @Transactional(readOnly = true)
    public AsientoContable obtenerPorNumero(long numero) {
        return asientoRepository.buscarPorNumero(numero)
                .orElseThrow(() -> new AsientoContableException(
                        "asiento no encontrado con número: " + numero));
    }
}
