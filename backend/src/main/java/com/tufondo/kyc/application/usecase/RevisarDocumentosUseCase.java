// com.tufondo.kyc.application.usecase.RevisarDocumentosUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.request.AprobarVerificacionRequest;
import com.tufondo.kyc.application.dto.request.RechazarVerificacionRequest;
import com.tufondo.kyc.application.dto.request.SolicitarInfoRequest;
import com.tufondo.kyc.application.dto.response.RevisionDecisionResponse;
import com.tufondo.kyc.application.dto.response.RevisionResponse;
import com.tufondo.kyc.domain.model.ConsentimientoKYC;
import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.port.StoragePort;
import com.tufondo.kyc.domain.repository.ConsentimientoKYCRepository;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import com.tufondo.ahorros.application.dto.CreateCuentaAhorroRequest;
import com.tufondo.ahorros.application.usecase.CrearCuentaAhorroUseCase;
import com.tufondo.ahorros.domain.exception.CuentaDuplicadaException;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import com.tufondo.notificaciones.application.service.NotificacionPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case para que el analista revise documentos.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RevisarDocumentosUseCase {

    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;
    private final ConsentimientoKYCRepository consentimientoRepository;
    private final StoragePort storagePort;
    // Issue #214 PR-C: notificar al socio del resultado de la revisión KYC.
    // El publisher es defensivo: si falla la persistencia de la notificación,
    // el flujo de aprobar/rechazar NO se ve afectado.
    private final NotificacionPublisher notificacionPublisher;
    /**
     * Use case para auto-crear cuenta de ahorro en VES al aprobar el KYC
     * (19-may-2026). Antes el socio quedaba aprobado pero sin cuenta — el
     * admin no tenía UI para crearla y el socio tampoco. Bug visible: socios
     * aprobados sin posibilidad de hacer depósitos.
     */
    private final CrearCuentaAhorroUseCase crearCuentaAhorroUseCase;

    /**
     * Defaults para la cuenta auto-creada en VES al aprobar KYC.
     *
     * <p><b>tasaInteres = 0.0001</b>: el mínimo permitido por la validación
     * del DTO. Las cuentas en bolívares NO rinden interés en la práctica por
     * la hiperinflación; el admin puede ajustar la tasa después si decide
     * habilitar rendimientos. No es 0 porque el validator del DTO exige
     * estrictamente > 0.</p>
     *
     * <p><b>montoMinimoRequerido = 1.0</b>: 1 bolívar — el mínimo razonable
     * para que el socio pueda hacer cualquier depósito. Igual que la tasa,
     * el admin puede ajustarlo después.</p>
     */
    private static final BigDecimal CUENTA_DEFAULT_TASA_INTERES = new BigDecimal("0.0001");
    private static final BigDecimal CUENTA_DEFAULT_MONTO_MINIMO = new BigDecimal("1.00");

    public RevisionResponse obtenerDetalle(UUID verificacionId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        // NOTA: aquí teníamos una guarda `estado != EN_REVISION → 403`,
        // pero rompía el caso de uso de ver detalle de un KYC ya APROBADO
        // o RECHAZADO desde el panel admin (historial / auditoría). El
        // `@PreAuthorize` del controller ya restringe el endpoint a roles
        // autorizados (ANALISTA_KYC, ADMIN, SUPER_ADMIN), así que la
        // restricción semántica de "solo en revisión" debe vivir en los
        // endpoints que MUTAN (aprobar/rechazar), no en el GET de detalle.

        List<DocumentoIdentidad> documentos = documentoRepository.findByVerificacionId(verificacionId);

        List<RevisionResponse.DocumentoRevisionResponse> docs = documentos.stream()
            .map(doc -> {
                String urlPresignada = storagePort.generatePresignedUrl(doc.getUrlAlmacenamiento(), 15);
                return RevisionResponse.DocumentoRevisionResponse.builder()
                    .id(doc.getId())
                    .tipo(doc.getTipoDocumento())
                    .descripcion(doc.getTipoDocumento().getDescripcion())
                    .estado(doc.getEstado())
                    .urlVisualizacion(urlPresignada)
                    .nombreOriginal(doc.getNombreOriginal())
                    .tamanoBytes(doc.getTamanoBytes())
                    .fechaSubida(doc.getFechaSubida())
                    .metadatosValidacion(doc.getMetadatosValidacion())
                    .build();
            })
            .collect(Collectors.toList());

        // Obtener ultimo consentimiento (sin datos sensibles)
        ConsentimientoKYC consentimiento = consentimientoRepository
            .findLatestBySocioId(verificacion.getSocioId())
            .orElse(null);

        RevisionResponse.ConsentimientoResponse consentimientoResponse = null;
        if (consentimiento != null) {
            consentimientoResponse = RevisionResponse.ConsentimientoResponse.builder()
                .aceptado(consentimiento.isAceptado())
                .fechaConsentimiento(consentimiento.getFechaConsentimiento())
                .build();
        }

        return RevisionResponse.builder()
            .verificacionId(verificacion.getId())
            .socioId(verificacion.getSocioId())
            .nivel(verificacion.getNivel())
            .estado(verificacion.getEstado())
            .fechaInicio(verificacion.getFechaInicio())
            .fechaEnvio(verificacion.getUpdatedAt())
            .documentos(docs)
            .consentimiento(consentimientoResponse)
            .build();
    }

    /**
     * Aprueba una verificación KYC.
     *
     * `@Transactional` es CRÍTICO: el método actualiza primero el estado del
     * KYC y luego el estado de cada documento individual. Sin transacción, si
     * el save del documento falla (como ocurrió con el bug @Version null en
     * DocumentoIdentidadEntity), el KYC quedaba APROBADO pero los docs en
     * PENDIENTE — inconsistencia visible al usuario. Con @Transactional toda
     * la operación es atómica.
     */
    @Transactional
    public RevisionDecisionResponse aprobar(UUID verificacionId, AprobarVerificacionRequest request, String analistaId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        if (!verificacion.puedeSerRevisada()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en estado de revision");
        }

        // Regla de negocio: no se puede aprobar documentalmente si la biometría
        // no pasó. El analista debe pedir al socio repetir la captura biométrica.
        if (verificacion.getEstadoBiometria() != com.tufondo.kyc.domain.model.enums.EstadoBiometria.APROBADA) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "No se puede aprobar la verificacion: el flujo biometrico no esta aprobado " +
                "(estado actual: " + verificacion.getEstadoBiometria() + ")");
        }

        EstadoVerificacion estadoAnterior = verificacion.getEstado();

        verificacion.setEstado(EstadoVerificacion.APROBADO);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(request.getComentario());
        verificacion.setFechaCompletado(LocalDateTime.now());
        verificacionRepository.save(verificacion);

        // Marcar documentos como validados
        List<DocumentoIdentidad> documentos = documentoRepository.findByVerificacionId(verificacionId);
        for (DocumentoIdentidad doc : documentos) {
            doc.setEstado(EstadoDocumento.VALIDADO);
            documentoRepository.save(doc);
        }

        // Auto-crear cuenta de ahorro en VES (19-may-2026). Antes este paso era
        // manual y nadie lo hacía: ni el admin tenía UI para crear cuenta a un
        // socio, ni el socio podía crearla por sí mismo desde su dashboard.
        // Resultado: socios aprobados sin cuenta, sin poder hacer depósitos.
        //
        // Defensivo: si la creación falla (ej. CuentaDuplicada por una cuenta
        // pre-existente), NO abortamos la aprobación del KYC — solo logueamos.
        // La aprobación documental es el evento crítico; la cuenta puede
        // crearse manualmente después si fuera necesario.
        autoCrearCuentaAhorroVES(verificacion.getSocioId());

        // Issue #214 PR-C: notificar al socio
        notificacionPublisher.notificarSocioKycAprobado(verificacion.getSocioId());

        return RevisionDecisionResponse.builder()
            .verificacionId(verificacion.getId())
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(verificacion.getEstado())
            .mensaje("Su verificacion KYC ha sido aprobada.")
            .build();
    }

    /**
     * Crea la cuenta de ahorro en VES para el socio recién aprobado.
     *
     * <p>Se invoca al final del flujo de aprobación. <b>NUNCA debe romper
     * la aprobación del KYC</b>: cualquier excepción se loguea y se traga.
     * La aprobación documental es el evento crítico para el negocio (el
     * socio ya pasó la verificación), la cuenta es secundaria — si falla
     * por algún edge case, un admin puede crearla manualmente después.</p>
     *
     * <p>Casos manejados:</p>
     * <ul>
     *   <li><b>{@link CuentaDuplicadaException}</b>: el socio ya tenía cuenta
     *       de ahorro (caso esperado — re-aprobación de KYC, KYC con docs
     *       extras). Log INFO, no es error.</li>
     *   <li>Cualquier otra excepción: log ERROR con stacktrace, sigue.</li>
     * </ul>
     */
    private void autoCrearCuentaAhorroVES(UUID socioId) {
        try {
            CreateCuentaAhorroRequest request = new CreateCuentaAhorroRequest(
                    socioId,
                    TipoCuenta.AHORRO,
                    Moneda.VES,
                    CUENTA_DEFAULT_MONTO_MINIMO,
                    CUENTA_DEFAULT_TASA_INTERES
            );
            // isAdmin=true porque este flujo se ejecuta desde el use case del
            // analista (rol ADMIN/ANALISTA_KYC). El check de ownership del
            // CrearCuentaAhorroUseCase exige isAdmin=true cuando socioIdToken
            // no coincide con request.socioId (que es nuestro caso — el
            // analista NO es el dueño de la cuenta).
            crearCuentaAhorroUseCase.ejecutar(request, null, true);
            log.info("Cuenta de ahorro VES auto-creada para socio {}", socioId);
        } catch (CuentaDuplicadaException e) {
            // Caso esperado: re-aprobación o el socio ya tenía cuenta.
            log.info("Cuenta de ahorro VES ya existía para socio {} — skipping", socioId);
        } catch (Throwable t) {
            // Cualquier otro error NO debe romper la aprobación del KYC.
            log.error("Falló auto-creación de cuenta VES para socio {} (se ignora, " +
                            "el admin puede crearla manualmente): {}", socioId, t.getMessage(), t);
        }
    }

    @Transactional
    public RevisionDecisionResponse rechazar(UUID verificacionId, RechazarVerificacionRequest request, String analistaId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        if (!verificacion.puedeSerRevisada()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en estado de revision");
        }

        EstadoVerificacion estadoAnterior = verificacion.getEstado();

        verificacion.setEstado(EstadoVerificacion.RECHAZADO);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(request.getComentario());
        verificacion.setMotivoRechazo(request.getComentario());
        verificacionRepository.save(verificacion);

        // Marcar documentos especificos como rechazados si se especificaron
        if (request.getDocumentosRechazados() != null && !request.getDocumentosRechazados().isEmpty()) {
            for (UUID docId : request.getDocumentosRechazados()) {
                documentoRepository.findById(docId).ifPresent(doc -> {
                    doc.setEstado(EstadoDocumento.RECHAZADO);
                    doc.setMotivoRechazo(request.getComentario());
                    documentoRepository.save(doc);
                });
            }
        }

        // Issue #214 PR-C: notificar al socio del rechazo con el motivo
        notificacionPublisher.notificarSocioKycRechazado(
                verificacion.getSocioId(), request.getComentario());

        return RevisionDecisionResponse.builder()
            .verificacionId(verificacion.getId())
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(verificacion.getEstado())
            .mensaje("Su verificacion KYC ha sido rechazada. Revise el motivo e intente nuevamente.")
            .build();
    }

    @Transactional
    public RevisionDecisionResponse solicitarInfo(UUID verificacionId, SolicitarInfoRequest request, String analistaId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        if (!verificacion.puedeSerRevisada()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en estado de revision");
        }

        EstadoVerificacion estadoAnterior = verificacion.getEstado();

        verificacion.setEstado(EstadoVerificacion.PENDIENTE);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(request.getComentario());
        verificacionRepository.save(verificacion);

        // Issue #214 PR-C: notificar al socio que necesita enviar más info
        notificacionPublisher.notificarSocioKycRequiereInfo(
                verificacion.getSocioId(), request.getComentario());

        return RevisionDecisionResponse.builder()
            .verificacionId(verificacion.getId())
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(verificacion.getEstado())
            .mensaje("Se requieren documentos adicionales para completar su verificacion.")
            .build();
    }
}