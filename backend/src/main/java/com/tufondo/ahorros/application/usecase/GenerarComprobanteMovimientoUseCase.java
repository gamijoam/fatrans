package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.MovimientoNoEncontradoException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.domain.exception.GeneracionPDFException;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Genera el comprobante PDF on-demand de un movimiento individual
 * (issue #220 PR-B).
 *
 * <p><strong>Diseño:</strong> a diferencia de los demás generadores del
 * módulo {@code documentospdf}, este NO persiste el archivo en MinIO ni
 * en la tabla {@code documentos}. El movimiento original es inmutable
 * (RN-006) y constituye la fuente de verdad — el PDF se reconstruye
 * cada vez que el socio lo solicita. Beneficios: cero infraestructura
 * adicional, cero malware scan, sin presigned URLs que expiran. Costo:
 * unos ms de CPU por descarga (insignificante).</p>
 *
 * <p><strong>Seguridad (IDOR):</strong> el socio solo puede descargar
 * comprobantes de cuentas de las que es titular. Admin tiene paso
 * libre por su rol.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerarComprobanteMovimientoUseCase {

    private static final DateTimeFormatter FECHA_LEGIBLE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA_EMISION =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy HH:mm");

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final SocioQueryPort socioQueryPort;
    private final PdfGeneratorPort pdfGeneratorPort;

    public byte[] ejecutar(String numeroCuenta, String numeroOperacion,
                           UUID socioIdToken, boolean isAdmin) {
        log.info("Generando comprobante: cuenta={}, operacion={}, socioIdToken={}, isAdmin={}",
                numeroCuenta, numeroOperacion, socioIdToken, isAdmin);

        // 1. Validar cuenta + IDOR
        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó descargar comprobante "
                    + "de cuenta {} que pertenece a socio {}",
                    socioIdToken, numeroCuenta, cuenta.getSocioId());
            throw new AccesoCuentaAjenaException();
        }

        // 2. Validar movimiento + pertenencia a la cuenta
        Movimiento movimiento = movimientoRepository.buscarPorNumeroOperacion(numeroOperacion)
                .orElseThrow(() -> new MovimientoNoEncontradoException(numeroOperacion));

        if (!movimiento.getCuentaAhorroId().equals(cuenta.getId())) {
            log.warn("Movimiento {} no pertenece a cuenta {} — posible enumeración",
                    numeroOperacion, numeroCuenta);
            throw new MovimientoNoEncontradoException(numeroOperacion);
        }

        // 3. Construir el data map para el generador
        Map<String, Object> datos = new HashMap<>();
        datos.put("socio", socioQueryPort.obtenerDatosSocioParaPdf(cuenta.getSocioId()));
        datos.put("cuenta", construirDatosCuenta(cuenta));
        datos.put("movimiento", construirDatosMovimiento(movimiento));
        datos.put("fechaEmision", LocalDateTime.now().format(FECHA_EMISION));

        // 4. Generar PDF — propagar excepción del generador como GeneracionPDFException
        try {
            byte[] pdf = pdfGeneratorPort.generarPdf(TipoDocumento.COMPROBANTE_MOVIMIENTO, datos);
            log.info("Comprobante generado: {} bytes para operacion {}", pdf.length, numeroOperacion);
            return pdf;
        } catch (GeneracionPDFException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado generando comprobante operacion={}", numeroOperacion, e);
            throw new GeneracionPDFException("Error al generar comprobante: " + e.getMessage());
        }
    }

    private Map<String, Object> construirDatosCuenta(CuentaAhorro cuenta) {
        Map<String, Object> m = new HashMap<>();
        m.put("numeroCuenta", cuenta.getNumeroCuenta());
        m.put("tipoCuenta", cuenta.getTipoCuenta() != null ? cuenta.getTipoCuenta().name() : null);
        m.put("moneda", cuenta.getMoneda() != null ? cuenta.getMoneda().name() : null);
        return m;
    }

    private Map<String, Object> construirDatosMovimiento(Movimiento mov) {
        Map<String, Object> m = new HashMap<>();
        m.put("numeroOperacion", mov.getNumeroOperacion());
        m.put("tipo", mov.getTipo() != null ? mov.getTipo().name() : null);
        m.put("monto", mov.getMonto());
        m.put("saldoAnterior", mov.getSaldoAnterior());
        m.put("saldoPosterior", mov.getSaldoPosterior());
        m.put("descripcion", mov.getDescripcion());
        m.put("referencia", mov.getReferencia());
        m.put("canalOrigen", mov.getCanalOrigen() != null ? mov.getCanalOrigen().name() : null);
        m.put("estado", mov.getEstado() != null ? mov.getEstado().name() : null);
        m.put("fechaMovimiento", mov.getFechaMovimiento() != null
                ? mov.getFechaMovimiento().format(FECHA_LEGIBLE)
                : null);
        return m;
    }
}
