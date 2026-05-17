package com.tufondo.notificaciones.application.usecase;

import com.tufondo.notificaciones.application.dto.NotificacionListResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests para issue #214 PR-A: listar notificaciones con paginación y filtros.
 */
@ExtendWith(MockitoExtension.class)
class ListarNotificacionesUseCaseTest {

    @Mock
    private NotificacionRepository repository;

    @InjectMocks
    private ListarNotificacionesUseCase useCase;

    private UUID usuarioId;
    private Notificacion noLeida;
    private Notificacion leida;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        noLeida = mockNotificacion(usuarioId, false);
        leida = mockNotificacion(usuarioId, true);
    }

    @Test
    @DisplayName("Sin filtro soloNoLeidas=false → lista todas las del destinatario")
    void lista_todas_cuando_filtro_apagado() {
        Page<Notificacion> page = new PageImpl<>(List.of(noLeida, leida));
        when(repository.listarPorDestinatario(eq(usuarioId), any(Pageable.class)))
                .thenReturn(page);
        when(repository.contarNoLeidasPorDestinatario(usuarioId)).thenReturn(1L);

        NotificacionListResponseDTO result = useCase.ejecutar(usuarioId, 0, 20, false);

        assertThat(result.notificaciones()).hasSize(2);
        assertThat(result.noLeidas()).isEqualTo(1L);
        // Verificamos que NO se llamó al método con filtro de no-leídas
        verify(repository, never()).listarNoLeidasPorDestinatario(any(), any());
    }

    @Test
    @DisplayName("Con filtro soloNoLeidas=true → solo retorna las no leídas")
    void lista_solo_no_leidas_cuando_filtro_activo() {
        Page<Notificacion> page = new PageImpl<>(List.of(noLeida));
        when(repository.listarNoLeidasPorDestinatario(eq(usuarioId), any(Pageable.class)))
                .thenReturn(page);
        when(repository.contarNoLeidasPorDestinatario(usuarioId)).thenReturn(1L);

        NotificacionListResponseDTO result = useCase.ejecutar(usuarioId, 0, 20, true);

        assertThat(result.notificaciones()).hasSize(1);
        verify(repository).listarNoLeidasPorDestinatario(eq(usuarioId), any());
        verify(repository, never()).listarPorDestinatario(any(), any());
    }

    @Test
    @DisplayName("Issue #214: incluso con filtro de no-leídas, el contador siempre refleja el total real (badge)")
    void contador_no_leidas_independiente_del_filtro() {
        // Si el cliente pide solo no-leídas, igualmente el badge tiene que
        // mostrar el total — sino, sería redundante (el filtro ya lo contiene).
        // Pero también si filtra sin no-leídas, el contador debe ser válido.
        Page<Notificacion> page = new PageImpl<>(List.of());
        when(repository.listarPorDestinatario(any(), any())).thenReturn(page);
        when(repository.contarNoLeidasPorDestinatario(usuarioId)).thenReturn(42L);

        NotificacionListResponseDTO result = useCase.ejecutar(usuarioId, 0, 20, false);

        assertThat(result.noLeidas()).isEqualTo(42L);
    }

    @Test
    @DisplayName("Defensive: size negativo se normaliza a 1 (prevenir error de Spring Pageable)")
    void size_negativo_normaliza() {
        Page<Notificacion> page = new PageImpl<>(List.of());
        when(repository.listarPorDestinatario(any(), any())).thenReturn(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        useCase.ejecutar(usuarioId, 0, -10, false);

        verify(repository).listarPorDestinatario(any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Defensive: size > MAX se clipea a 100 (prevenir abuso)")
    void size_excesivo_se_limita() {
        Page<Notificacion> page = new PageImpl<>(List.of());
        when(repository.listarPorDestinatario(any(), any())).thenReturn(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        useCase.ejecutar(usuarioId, 0, 999_999, false);

        verify(repository).listarPorDestinatario(any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isLessThanOrEqualTo(100);
    }

    private Notificacion mockNotificacion(UUID dest, boolean isRead) {
        return Notificacion.builder()
                .id(UUID.randomUUID())
                .destinatarioId(dest)
                .tipo(TipoNotificacion.KYC_APROBADO)
                .titulo("Test")
                .mensaje("Test")
                .leida(isRead)
                .fechaLectura(isRead ? Instant.now() : null)
                .prioridad(PrioridadNotificacion.NORMAL)
                .createdAt(Instant.now())
                .build();
    }
}
