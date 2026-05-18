package com.tufondo.notificaciones.application.service;

import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.model.enums.PrioridadNotificacion;
import com.tufondo.notificaciones.domain.model.enums.TipoNotificacion;
import com.tufondo.notificaciones.domain.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para issue #214 PR-C: publicador semántico de notificaciones.
 *
 * <p>Verifica que (a) las notificaciones se persisten con los campos correctos,
 * (b) la resolución socioId→usuarioId funciona, (c) los fallos NO se propagan
 * al caller (defensa en profundidad — un fallo de notificación nunca debe
 * romper aprobar un KYC o desembolsar un crédito).</p>
 */
@ExtendWith(MockitoExtension.class)
class NotificacionPublisherTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificacionPublisher publisher;

    private UUID socioId;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        socioId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        // Nota: el stub `buscarPorSocioId` se hace por test (no en setUp) porque
        // algunos tests prueban explícitamente la ausencia de usuario o IDs
        // diferentes. Mockito strict mode marca como error los stubs no usados.
    }

    /** Helper: stub que devuelve usuario válido para el socio del setUp. */
    private void stubUsuarioParaSocio() {
        Usuario usuario = Usuario.desdeParametros(
                usuarioId, "socio_test", "socio@test.com", "hash",
                "Test", Rol.SOCIO, socioId, true,
                Instant.now(), Instant.now(), 0, null, false);
        when(usuarioRepository.buscarPorSocioId(socioId))
                .thenReturn(Optional.of(usuario));
    }

    @Test
    @DisplayName("notificarSocioKycAprobado: persiste notificación con tipo KYC_APROBADO + linkAccion correcto")
    void kyc_aprobado_persiste_correctamente() {
        stubUsuarioParaSocio();
        publisher.notificarSocioKycAprobado(socioId);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).guardar(captor.capture());
        Notificacion n = captor.getValue();

        assertThat(n.getDestinatarioId()).isEqualTo(usuarioId);
        assertThat(n.getTipo()).isEqualTo(TipoNotificacion.KYC_APROBADO);
        assertThat(n.getLinkAccion()).isEqualTo("/dashboard/kyc");
        assertThat(n.isLeida()).isFalse();
        assertThat(n.getPrioridad()).isEqualTo(PrioridadNotificacion.NORMAL);
        assertThat(n.getTitulo()).isNotBlank();
        assertThat(n.getMensaje()).isNotBlank();
        assertThat(n.getId()).isNotNull();
        assertThat(n.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Issue #214: notificarSocioKycRechazado CON motivo → el motivo aparece en el mensaje")
    void kyc_rechazado_con_motivo_lo_propaga() {
        stubUsuarioParaSocio();
        String motivo = "La foto de la cédula está borrosa";

        publisher.notificarSocioKycRechazado(socioId, motivo);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).guardar(captor.capture());
        Notificacion n = captor.getValue();

        assertThat(n.getTipo()).isEqualTo(TipoNotificacion.KYC_RECHAZADO);
        assertThat(n.getPrioridad()).isEqualTo(PrioridadNotificacion.URGENTE);
        // CRÍTICO: el socio necesita saber QUÉ corregir
        assertThat(n.getMensaje()).contains(motivo);
    }

    @Test
    @DisplayName("notificarSocioCreditoAprobado: monto y número en el mensaje")
    void credito_aprobado_incluye_monto_y_numero() {
        stubUsuarioParaSocio();
        publisher.notificarSocioCreditoAprobado(
                socioId, new BigDecimal("5000.00"), "Bs", "SOL-CRED-2026-000001");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).guardar(captor.capture());
        Notificacion n = captor.getValue();

        assertThat(n.getTipo()).isEqualTo(TipoNotificacion.CREDITO_APROBADO);
        assertThat(n.getMensaje()).contains("5000.00").contains("SOL-CRED-2026-000001");
    }

    @Test
    @DisplayName("notificarSocioCreditoDesembolsado: monto y referencia en el mensaje")
    void credito_desembolsado_incluye_datos() {
        stubUsuarioParaSocio();
        publisher.notificarSocioCreditoDesembolsado(
                socioId, new BigDecimal("4800.00"), "Bs", "SOL-CRED-2026-000001");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).guardar(captor.capture());
        Notificacion n = captor.getValue();

        assertThat(n.getTipo()).isEqualTo(TipoNotificacion.CREDITO_DESEMBOLSADO);
        assertThat(n.getMensaje()).contains("4800.00");
    }

    @Test
    @DisplayName("Issue #214 RESILIENCIA: si socio NO tiene usuario asociado → log + skip, NO lanza")
    void socio_sin_usuario_skip_silencioso() {
        UUID socioHuerfano = UUID.randomUUID();
        when(usuarioRepository.buscarPorSocioId(socioHuerfano)).thenReturn(Optional.empty());

        // NO debe lanzar — el caller (ej. AprobarKycUseCase) no debe abortar
        // su operación principal por un problema de notificación.
        publisher.notificarSocioKycAprobado(socioHuerfano);

        verify(notificacionRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Issue #214 RESILIENCIA: socioId null → skip silencioso, no lanza")
    void socio_null_skip_silencioso() {
        publisher.notificarSocioKycAprobado(null);

        verify(notificacionRepository, never()).guardar(any());
        verify(usuarioRepository, never()).buscarPorSocioId(any());
    }

    @Test
    @DisplayName("Issue #214 RESILIENCIA: si el repositorio lanza → publisher atrapa, no propaga")
    void error_persistencia_atrapado() {
        stubUsuarioParaSocio();
        doThrow(new RuntimeException("BD caída"))
                .when(notificacionRepository).guardar(any());

        // NO debe lanzar — el aprobar KYC del caller debe completarse igual
        publisher.notificarSocioKycAprobado(socioId);

        // Verificamos que SÍ intentó persistir (no fue skip)
        verify(notificacionRepository).guardar(any());
        // Sin assert sobre exception — el punto es que NO lanzó
    }

    @Test
    @DisplayName("publicarAUsuario: ruta directa sin lookup de socio")
    void publicar_directo_a_usuario_sin_lookup() {
        UUID admin = UUID.randomUUID();
        Notificacion plantilla = Notificacion.builder()
                .tipo(TipoNotificacion.ADMIN_NUEVO_KYC)
                .titulo("Nuevo KYC para revisar")
                .mensaje("Tienes una nueva verificación pendiente.")
                .prioridad(PrioridadNotificacion.NORMAL)
                .build();

        publisher.publicarAUsuario(admin, plantilla);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).guardar(captor.capture());
        assertThat(captor.getValue().getDestinatarioId()).isEqualTo(admin);
        // No debió consultar el repo de usuarios (es ruta directa)
        verify(usuarioRepository, never()).buscarPorSocioId(any());
    }
}
