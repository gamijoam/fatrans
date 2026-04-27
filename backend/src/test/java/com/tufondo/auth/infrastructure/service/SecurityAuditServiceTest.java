package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.domain.model.audit.SecurityEvent;
import com.tufondo.auth.infrastructure.persistence.entity.SecurityAuditEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.SecurityAuditJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityAuditService - Tests Completos")
class SecurityAuditServiceTest {

    @Mock
    private SecurityAuditJpaRepository auditRepository;

    @Captor
    private ArgumentCaptor<SecurityAuditEntity> entityCaptor;

    private SecurityAuditService securityAuditService;

    private UUID usuarioId;
    private String ipAddress;

    @BeforeEach
    void setUp() {
        securityAuditService = new SecurityAuditService(auditRepository);
        usuarioId = UUID.randomUUID();
        ipAddress = "192.168.1.1";
    }

    @Nested
    @DisplayName("logLoginExitoso")
    class LogLoginExitosoTests {

        @Test
        @DisplayName("Registra login exitoso en base de datos")
        void registra_login_exitoso() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logLoginExitoso(usuarioId.toString(), ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("LOGIN_SUCCESS");
            assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Login exitoso tiene detalles nulos")
        void login_exitoso_detalles_nulos() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logLoginExitoso(usuarioId.toString(), ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getDetalles()).isNull();
        }
    }

    @Nested
    @DisplayName("logLoginFallido")
    class LogLoginFallidoTests {

        @Test
        @DisplayName("Registra login fallido con razón")
        void registra_login_fallido() {
            String identificador = "usuario_fallido";
            String razon = "Credenciales inválidas";

            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logLoginFallido(identificador, ipAddress, razon);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("LOGIN_FAILED");
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getDetalles()).contains(razon);
        }

        @Test
        @DisplayName("Login fallido no tiene usuarioId")
        void login_fallido_sin_usuario_id() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logLoginFallido("identificador", ipAddress, "Razón");

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getUsuarioId()).isNull();
        }
    }

    @Nested
    @DisplayName("logLogout")
    class LogLogoutTests {

        @Test
        @DisplayName("Registra logout exitoso")
        void registra_logout() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logLogout(usuarioId.toString(), ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("LOGOUT");
            assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
        }
    }

    @Nested
    @DisplayName("logTokenRefresh")
    class LogTokenRefreshTests {

        @Test
        @DisplayName("Registra refresh de token")
        void registra_token_refresh() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logTokenRefresh(usuarioId.toString(), ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getTipoEvento()).isEqualTo("TOKEN_REFRESH");
        }
    }

    @Nested
    @DisplayName("logCuentaBloqueada")
    class LogCuentaBloqueadaTests {

        @Test
        @DisplayName("Registra cuenta bloqueada")
        void registra_cuenta_bloqueada() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logCuentaBloqueada(usuarioId.toString(), ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("ACCOUNT_LOCKED");
            assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
        }
    }

    @Nested
    @DisplayName("logDashboardAcceso")
    class LogDashboardAccesoTests {

        @Test
        @DisplayName("Registra acceso a dashboard")
        void registra_dashboard_acceso() {
            String rol = "ADMIN";
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logDashboardAcceso(usuarioId.toString(), ipAddress, rol);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("DASHBOARD_ADMIN_ACCESS");
            assertThat(saved.getDetalles()).contains(rol);
        }
    }

    @Nested
    @DisplayName("registrarIntentoVerificacion")
    class RegistrarIntentoVerificacionTests {

        @Test
        @DisplayName("Registra verificación exitosa")
        void registra_verificacion_exitosa() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.registrarIntentoVerificacion(usuarioId, "PASSWORD_VERIFIED", true, ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("PASSWORD_VERIFIED");
            assertThat(saved.getDetalles()).isEqualTo("Verificación de password exitosa");
        }

        @Test
        @DisplayName("Registra código enviado por email")
        void registra_codigo_email() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.registrarIntentoVerificacion(usuarioId, "CODIGO_ENVIADO_EMAIL", true, ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getDetalles()).isEqualTo("Código de verificación enviado por email");
        }

        @Test
        @DisplayName("Registra código enviado por SMS")
        void registra_codigo_sms() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.registrarIntentoVerificacion(usuarioId, "CODIGO_ENVIADO_SMS", true, ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getDetalles()).isEqualTo("Código de verificación enviado por SMS");
        }

        @Test
        @DisplayName("Registra código confirmado")
        void registra_codigo_confirmado() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.registrarIntentoVerificacion(usuarioId, "CODIGO_CONFIRMED", true, ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getDetalles()).isEqualTo("Código de verificación confirmado");
        }

        @Test
        @DisplayName("Registra verificación fallida")
        void registra_verificacion_fallida() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.registrarIntentoVerificacion(usuarioId, "PASSWORD_VERIFIED", false, ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getDetalles()).isEqualTo("Verificación de password exitosa");
        }

        @Test
        @DisplayName("Registra tipo de verificación desconocido")
        void registra_tipo_desconocido() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.registrarIntentoVerificacion(usuarioId, "UNKNOWN_TYPE", false, ipAddress);

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getDetalles()).contains("Intento de verificación: UNKNOWN_TYPE");
        }
    }

    @Nested
    @DisplayName("logSesionesInvalidadas")
    class LogSesionesInvalidadasTests {

        @Test
        @DisplayName("Registra invalidación de sesiones")
        void registra_sesiones_invalidadas() {
            UUID invalidadoPor = UUID.randomUUID();
            int cantidad = 5;

            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logSesionesInvalidadas(usuarioId, ipAddress, invalidadoPor, cantidad);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("SESSIONS_INVALIDATED");
            assertThat(saved.getDetalles()).contains(String.valueOf(cantidad));
        }
    }

    @Nested
    @DisplayName("logSesionIndividualInvalidadas")
    class LogSesionIndividualInvalidadasTests {

        @Test
        @DisplayName("Registra invalidación de sesión individual")
        void registra_sesion_individual_invalidadas() {
            UUID invalidadoPor = UUID.randomUUID();
            String sesionId = UUID.randomUUID().toString();

            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logSesionIndividualInvalidadas(usuarioId, ipAddress, invalidadoPor, sesionId);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo("SESSION_INVALIDATED");
            assertThat(saved.getDetalles()).contains(sesionId);
        }
    }

    @Nested
    @DisplayName("logCustomEvent")
    class LogCustomEventTests {

        @Test
        @DisplayName("Registra evento custom")
        void registra_evento_custom() {
            String tipoEvento = "MI_EVENTO_CUSTOM";
            String detalles = "Detalles del evento custom";

            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logCustomEvent(tipoEvento, usuarioId, ipAddress, detalles);

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo(tipoEvento);
            assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getDetalles()).isEqualTo(detalles);
        }

        @Test
        @DisplayName("Registra evento custom sin usuario")
        void registra_evento_custom_sin_usuario() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logCustomEvent("EVENTO_SISTEMA", null, ipAddress, "Evento del sistema");

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getUsuarioId()).isNull();
        }
    }

    @Nested
    @DisplayName("logEntityEvent")
    class LogEntityEventTests {

        @Test
        @DisplayName("Registra evento de entidad con todos los campos")
        void registra_evento_entidad_completo() {
            String tipoEvento = "TIPO_CREDITO_CREADO";
            UUID entityId = UUID.randomUUID();
            String action = "CREATE";

            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logEntityEvent(
                    tipoEvento, usuarioId, ipAddress, "TIPO_CREDITO", entityId.toString(), action, "Micro crédito creado"
            );

            verify(auditRepository).save(entityCaptor.capture());
            SecurityAuditEntity saved = entityCaptor.getValue();

            assertThat(saved.getTipoEvento()).isEqualTo(tipoEvento);
            assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getEntityType()).isEqualTo("TIPO_CREDITO");
            assertThat(saved.getEntityId()).isEqualTo(entityId.toString());
            assertThat(saved.getAction()).isEqualTo(action);
            assertThat(saved.getDetalles()).isEqualTo("Micro crédito creado");
        }

        @Test
        @DisplayName("Registra evento de admin creado")
        void registra_admin_creado() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logEntityEvent(
                    "ADMIN_CREADO", usuarioId, ipAddress, "USUARIO", UUID.randomUUID().toString(), "ADMIN_CREADO", "Admin creado"
            );

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getTipoEvento()).isEqualTo("ADMIN_CREADO");
        }

        @Test
        @DisplayName("Registra evento tipo crédito actualizado")
        void registra_tipo_credito_actualizado() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenAnswer(i -> i.getArgument(0));

            securityAuditService.logEntityEvent(
                    "TIPO_CREDITO_ACTUALIZADO", usuarioId, ipAddress, "TIPO_CREDITO", UUID.randomUUID().toString(),
                    "TIPO_CREDITO_ACTUALIZADO", "Tasa actualizada"
            );

            verify(auditRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getTipoEvento()).isEqualTo("TIPO_CREDITO_ACTUALIZADO");
        }
    }

    @Nested
    @DisplayName("Manejo de errores")
    class ManejoErroresTests {

        @Test
        @DisplayName("No lanza excepción cuando save falla")
        void no_lanza_excepcion_cuando_save_falla() {
            when(auditRepository.save(any(SecurityAuditEntity.class))).thenThrow(new RuntimeException("DB error"));

            securityAuditService.logLoginExitoso(usuarioId.toString(), ipAddress);

            verify(auditRepository).save(any(SecurityAuditEntity.class));
        }
    }
}