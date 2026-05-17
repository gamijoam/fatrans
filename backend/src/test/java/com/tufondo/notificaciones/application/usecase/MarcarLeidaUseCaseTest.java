package com.tufondo.notificaciones.application.usecase;

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
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para issue #214 PR-A: marcar leída con check de ownership.
 *
 * <p>Lección aprendida del issue #179 (IDOR): un usuario NUNCA debe poder
 * modificar recursos de OTRO usuario. Estos tests blindan ese check
 * específicamente para notificaciones.</p>
 */
@ExtendWith(MockitoExtension.class)
class MarcarLeidaUseCaseTest {

    @Mock
    private NotificacionRepository repository;

    @InjectMocks
    private MarcarLeidaUseCase useCase;

    private UUID usuarioDueno;
    private UUID notificacionId;
    private Notificacion notificacionNoLeida;

    @BeforeEach
    void setUp() {
        usuarioDueno = UUID.randomUUID();
        notificacionId = UUID.randomUUID();
        notificacionNoLeida = Notificacion.builder()
                .id(notificacionId)
                .destinatarioId(usuarioDueno)
                .tipo(TipoNotificacion.KYC_APROBADO)
                .titulo("Tu KYC fue aprobado")
                .mensaje("Felicidades, ya puedes operar.")
                .prioridad(PrioridadNotificacion.NORMAL)
                .leida(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Usuario marca SU PROPIA notificación → se guarda con leida=true + fechaLectura")
    void usuario_marca_propia_notificacion_pasa() {
        when(repository.buscarPorId(notificacionId)).thenReturn(Optional.of(notificacionNoLeida));

        useCase.ejecutar(notificacionId, usuarioDueno);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).guardar(captor.capture());
        Notificacion guardada = captor.getValue();
        assertThat(guardada.isLeida()).isTrue();
        assertThat(guardada.getFechaLectura()).isNotNull();
    }

    @Test
    @DisplayName("Issue #214 anti-IDOR: usuario intenta marcar notificación de OTRO → AccessDeniedException")
    void usuario_marca_notificacion_ajena_falla() {
        UUID atacante = UUID.randomUUID();
        when(repository.buscarPorId(notificacionId)).thenReturn(Optional.of(notificacionNoLeida));

        assertThatThrownBy(() -> useCase.ejecutar(notificacionId, atacante))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No tienes permisos");

        // CRÍTICO: la notificación NO debe haberse modificado
        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Notificación inexistente → NotificacionNoEncontradaException")
    void notificacion_inexistente_falla() {
        when(repository.buscarPorId(notificacionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(notificacionId, usuarioDueno))
                .isInstanceOf(MarcarLeidaUseCase.NotificacionNoEncontradaException.class);

        verify(repository, never()).guardar(any());
    }

    @Test
    @DisplayName("Idempotente: marcar leída una notificación ya leída → no hace nada (no actualiza fechaLectura)")
    void marcar_leida_dos_veces_es_idempotente() {
        Notificacion yaLeida = Notificacion.builder()
                .id(notificacionId)
                .destinatarioId(usuarioDueno)
                .tipo(TipoNotificacion.KYC_APROBADO)
                .titulo("Test")
                .mensaje("Test")
                .prioridad(PrioridadNotificacion.NORMAL)
                .leida(true)
                .fechaLectura(Instant.parse("2026-01-01T00:00:00Z"))
                .createdAt(Instant.now())
                .build();
        when(repository.buscarPorId(notificacionId)).thenReturn(Optional.of(yaLeida));

        useCase.ejecutar(notificacionId, usuarioDueno);

        // CRÍTICO: NO debe llamar guardar — la fechaLectura original se preserva.
        // Sin este check, un cliente que marca dos veces "leída" sobrescribiría
        // la fecha original (mala práctica de auditoría).
        verify(repository, never()).guardar(any());
    }
}
