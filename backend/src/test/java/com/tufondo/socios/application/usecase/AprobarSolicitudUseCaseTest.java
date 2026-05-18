package com.tufondo.socios.application.usecase;

import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import com.tufondo.socios.application.dto.AprobarSolicitudRequestDTO;
import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import com.tufondo.socios.domain.repository.SocioRepository;
import com.tufondo.socios.domain.repository.SolicitudRegistroRepository;
import com.tufondo.core.port.UsuarioCreatorPort;
import com.tufondo.socios.infrastructure.notification.EmailNotificationService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AprobarSolicitudUseCase - Tests")
class AprobarSolicitudUseCaseTest {

    @Mock
    private SolicitudRegistroRepository solicitudRepository;

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private UsuarioCreatorPort usuarioCreatorPort;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SolicitudRegistroDTOMapper dtoMapper;

    @Mock
    private VerificacionKYCRepository verificacionKYCRepository;

    private AprobarSolicitudUseCase useCase;

    private UUID solicitudId;
    private String adminId;
    private SolicitudRegistro solicitudMock;
    private Socio socioMock;

    @BeforeEach
    void setUp() {
        useCase = new AprobarSolicitudUseCase(
                solicitudRepository, socioRepository, usuarioCreatorPort,
                emailNotificationService, dtoMapper, verificacionKYCRepository
        );

        solicitudId = UUID.randomUUID();
        adminId = "admin-uuid";

        solicitudMock = mock(SolicitudRegistro.class);
        socioMock = mock(Socio.class);
    }

    @Nested
    @DisplayName("Preservación de datos del socio")
    class PreservacionDatosTests {

        @Test
        @DisplayName("Parsed nombre compuesto venezolano correctamente")
        void nombreCompuestoVenezolano() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("María José González López");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("maria@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa ABC");
            when(solicitudMock.getTipoDocumento()).thenReturn(TipoDocumento.CEDULA);
            when(solicitudMock.getGenero()).thenReturn(Genero.FEMENINO);
            when(solicitudMock.getEstadoCivil()).thenReturn(EstadoCivil.SOLTERO);
            when(solicitudMock.getFechaNacimiento()).thenReturn(LocalDate.of(1995, 5, 15));
            when(solicitudMock.getDepartamento()).thenReturn("Recursos Humanos");
            when(solicitudMock.getCargo()).thenReturn("Analista");
            when(solicitudMock.getDireccionCalle()).thenReturn("Calle 123");
            when(solicitudMock.getDireccionCiudad()).thenReturn("Caracas");
            when(solicitudMock.getDireccionEstado()).thenReturn("Distrito Capital");
            when(solicitudMock.getEmergenciaNombre()).thenReturn("Juan González");
            when(solicitudMock.getEmergenciaTelefono()).thenReturn("04141234567");
            when(solicitudMock.getEmergenciaParentesco()).thenReturn("CONYUGE");

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
            verify(socioRepository).guardar(socioCaptor.capture());

            Socio socioCreado = socioCaptor.getValue();
            assertThat(socioCreado.getPrimerNombre()).isEqualTo("María");
            assertThat(socioCreado.getSegundoNombre()).isEqualTo("José");
            assertThat(socioCreado.getPrimerApellido()).isEqualTo("González");
            assertThat(socioCreado.getSegundoApellido()).isEqualTo("López");
        }

        @Test
        @DisplayName("Preserva genero y estado civil del formulario")
        void preservaGeneroYEstadoCivil() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("Juan Pérez García");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("juan@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa ABC");
            when(solicitudMock.getTipoDocumento()).thenReturn(TipoDocumento.CEDULA);
            when(solicitudMock.getGenero()).thenReturn(Genero.MASCULINO);
            when(solicitudMock.getEstadoCivil()).thenReturn(EstadoCivil.CASADO);
            when(solicitudMock.getFechaNacimiento()).thenReturn(LocalDate.of(1990, 3, 20));
            when(solicitudMock.getDepartamento()).thenReturn("Ventas");
            when(solicitudMock.getCargo()).thenReturn("Vendedor");

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
            verify(socioRepository).guardar(socioCaptor.capture());

            Socio socioCreado = socioCaptor.getValue();
            assertThat(socioCreado.getGenero()).isEqualTo(Genero.MASCULINO);
            assertThat(socioCreado.getEstadoCivil()).isEqualTo(EstadoCivil.CASADO);
            assertThat(socioCreado.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 3, 20));
            assertThat(socioCreado.getDepartamento()).isEqualTo("Ventas");
            assertThat(socioCreado.getCargo()).isEqualTo("Vendedor");
        }

        @Test
        @DisplayName("Usa valores por defecto cuando campos son null")
        void valoresPorDefectoCuandoNull() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("Test User");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("test@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa");
            when(solicitudMock.getTipoDocumento()).thenReturn(null);
            when(solicitudMock.getGenero()).thenReturn(null);
            when(solicitudMock.getEstadoCivil()).thenReturn(null);
            when(solicitudMock.getFechaNacimiento()).thenReturn(null);

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
            verify(socioRepository).guardar(socioCaptor.capture());

            Socio socioCreado = socioCaptor.getValue();
            assertThat(socioCreado.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA);
            assertThat(socioCreado.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 1, 1));
        }

        @Test
        @DisplayName("Detecta tipo documento V- como CEDULA")
        void detectaCedulaV() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("Test User");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("test@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa");
            when(solicitudMock.getTipoDocumento()).thenReturn(null);
            when(solicitudMock.getGenero()).thenReturn(null);
            when(solicitudMock.getEstadoCivil()).thenReturn(null);
            when(solicitudMock.getFechaNacimiento()).thenReturn(null);

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
            verify(socioRepository).guardar(socioCaptor.capture());

            Socio socioCreado = socioCaptor.getValue();
            assertThat(socioCreado.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA);
        }

        @Test
        @DisplayName("Detecta tipo documento E- como PASAPORTE")
        void detectaPasaporteE() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("Test User");
            when(solicitudMock.getCedula()).thenReturn("E-12345678");
            when(solicitudMock.getCorreoElectronico()).thenReturn("test@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa");
            when(solicitudMock.getTipoDocumento()).thenReturn(null);
            when(solicitudMock.getGenero()).thenReturn(null);
            when(solicitudMock.getEstadoCivil()).thenReturn(null);
            when(solicitudMock.getFechaNacimiento()).thenReturn(null);

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
            verify(socioRepository).guardar(socioCaptor.capture());

            Socio socioCreado = socioCaptor.getValue();
            assertThat(socioCreado.getTipoDocumento()).isEqualTo(TipoDocumento.PASAPORTE);
        }
    }

    @Nested
    @DisplayName("Generación de credenciales")
    class GeneracionCredencialesTests {

        @Test
        @DisplayName("Crea usuario y envia email con credenciales")
        void creaUsuarioYEnviaEmail() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("Juan Pérez");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("juan@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa");
            when(solicitudMock.getTipoDocumento()).thenReturn(TipoDocumento.CEDULA);
            when(solicitudMock.getGenero()).thenReturn(null);
            when(solicitudMock.getEstadoCivil()).thenReturn(null);
            when(solicitudMock.getFechaNacimiento()).thenReturn(null);

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            verify(usuarioCreatorPort).crearUsuarioVinculado(any(), any(), eq("juan@test.com"), any());
            verify(emailNotificationService).enviarCredenciales(eq("juan@test.com"), any(), any());
        }
    }

    @Nested
    @DisplayName("Auto-trigger KYC")
    class AutoTriggerKYCTests {

        @Test
        @DisplayName("Crea VerificacionKYC con estado PENDIENTE al aprobar socio")
        void creaVerificacionKYCAlAprobar() {
            UUID socioId = UUID.randomUUID();

            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("María García");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("maria@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04141234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa");
            when(solicitudMock.getTipoDocumento()).thenReturn(TipoDocumento.CEDULA);
            when(solicitudMock.getGenero()).thenReturn(null);
            when(solicitudMock.getEstadoCivil()).thenReturn(null);
            when(solicitudMock.getFechaNacimiento()).thenReturn(null);

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(socioId);
                return s;
            });
            when(verificacionKYCRepository.save(any(VerificacionKYC.class))).thenAnswer(inv -> {
                VerificacionKYC v = inv.getArgument(0);
                v.setId(UUID.randomUUID());
                return v;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());

            useCase.ejecutar(solicitudId, null, adminId);

            ArgumentCaptor<VerificacionKYC> kycCaptor = ArgumentCaptor.forClass(VerificacionKYC.class);
            verify(verificacionKYCRepository).save(kycCaptor.capture());

            VerificacionKYC kycCreado = kycCaptor.getValue();
            assertThat(kycCreado.getSocioId()).isEqualTo(socioId);
            assertThat(kycCreado.getEstado()).isEqualTo(EstadoVerificacion.PENDIENTE);
            assertThat(kycCreado.getNivel()).isEqualTo(NivelVerificacion.BASICO);
            assertThat(kycCreado.getFechaExpiracion()).isNotNull();
        }
    }

    /**
     * Tests del bug G3: la contraseña temporal NO debe aparecer en logs
     * y debe generarse con SecureRandom (no ThreadLocalRandom).
     */
    @Nested
    @DisplayName("Seguridad de la contraseña temporal (G3)")
    class SeguridadPasswordTests {

        private ListAppender<ILoggingEvent> useCaseAppender;
        private Logger useCaseLogger;

        @BeforeEach
        void attachAppender() {
            useCaseLogger = (Logger) LoggerFactory.getLogger(AprobarSolicitudUseCase.class);
            useCaseAppender = new ListAppender<>();
            useCaseAppender.start();
            useCaseLogger.addAppender(useCaseAppender);
            useCaseLogger.setLevel(Level.DEBUG);
        }

        @AfterEach
        void detachAppender() {
            if (useCaseLogger != null && useCaseAppender != null) {
                useCaseLogger.detachAppender(useCaseAppender);
            }
        }

        @Test
        @DisplayName("El password temporal NUNCA aparece en los logs del use case")
        void passwordTemporalNoApareceEnLogs() {
            stubSolicitudMinima();
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            useCase.ejecutar(solicitudId, null, adminId);

            verify(emailNotificationService).enviarCredenciales(
                    any(), any(), passwordCaptor.capture());
            String passwordGenerado = passwordCaptor.getValue();
            assertThat(passwordGenerado).isNotNull().isNotEmpty();

            // Concatenamos todos los mensajes renderizados (con argumentos sustituidos)
            // y verificamos que la contraseña en plain text NO aparece en NINGUNO.
            String logsCompletos = useCaseAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (a, b) -> a + "\n" + b);

            assertThat(logsCompletos)
                    .as("La contraseña temporal NO debe aparecer en los logs (riesgo seguridad/LOPDP)")
                    .doesNotContain(passwordGenerado);
        }

        @Test
        @DisplayName("Password generado tiene mínimo 12 chars, con dígito y letra")
        void passwordTemporalTieneFortalezaMinima() {
            stubSolicitudMinima();
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            useCase.ejecutar(solicitudId, null, adminId);

            verify(emailNotificationService).enviarCredenciales(
                    any(), any(), passwordCaptor.capture());
            String password = passwordCaptor.getValue();

            assertThat(password).isNotNull();
            assertThat(password.length())
                    .as("Mínimo 12 caracteres")
                    .isGreaterThanOrEqualTo(12);
            assertThat(Pattern.compile("\\d").matcher(password).find())
                    .as("Debe tener al menos 1 dígito")
                    .isTrue();
            assertThat(Pattern.compile("[A-Za-z]").matcher(password).find())
                    .as("Debe tener al menos 1 letra")
                    .isTrue();
        }

        @Test
        @DisplayName("El password también se pasa intacto a UsuarioCreatorPort")
        void passwordSeEntregaAUsuarioCreator() {
            stubSolicitudMinima();
            ArgumentCaptor<String> passwordEmail = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> passwordUsuario = ArgumentCaptor.forClass(String.class);

            useCase.ejecutar(solicitudId, null, adminId);

            verify(usuarioCreatorPort).crearUsuarioVinculado(
                    any(), any(), eq("test@test.com"), passwordUsuario.capture());
            verify(emailNotificationService).enviarCredenciales(
                    eq("test@test.com"), any(), passwordEmail.capture());

            assertThat(passwordUsuario.getValue()).isEqualTo(passwordEmail.getValue());
        }

        @Test
        @DisplayName("Dos invocaciones consecutivas generan passwords distintos (sanity SecureRandom)")
        void passwordsConsecutivosDistintos() {
            stubSolicitudMinima();
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            useCase.ejecutar(solicitudId, null, adminId);
            useCase.ejecutar(solicitudId, null, adminId);

            verify(emailNotificationService, times(2))
                    .enviarCredenciales(any(), any(), passwordCaptor.capture());

            List<String> passwords = passwordCaptor.getAllValues();
            assertThat(passwords).hasSize(2);
            assertThat(passwords.get(0)).isNotEqualTo(passwords.get(1));
        }

        private void stubSolicitudMinima() {
            when(solicitudRepository.buscarPorId(solicitudId)).thenReturn(Optional.of(solicitudMock));
            when(solicitudMock.getEstado()).thenReturn(EstadoSolicitud.PENDIENTE);
            when(solicitudMock.getNombreCompleto()).thenReturn("Test Usuario");
            when(solicitudMock.getCedula()).thenReturn("V-30123456");
            when(solicitudMock.getCorreoElectronico()).thenReturn("test@test.com");
            when(solicitudMock.getTelefono()).thenReturn("04121234567");
            when(solicitudMock.getEmpresa()).thenReturn("Empresa");
            when(solicitudMock.getTipoDocumento()).thenReturn(TipoDocumento.CEDULA);
            when(solicitudMock.getGenero()).thenReturn(null);
            when(solicitudMock.getEstadoCivil()).thenReturn(null);
            when(solicitudMock.getFechaNacimiento()).thenReturn(null);

            when(socioRepository.existePorNumeroSocio(any())).thenReturn(false);
            when(usuarioCreatorPort.existeNombreUsuario(any())).thenReturn(false);
            when(socioRepository.guardar(any(Socio.class))).thenAnswer(inv -> {
                Socio s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });
            when(dtoMapper.toResponseDTO(any())).thenReturn(new SolicitudRegistroResponseDTO());
        }
    }
}