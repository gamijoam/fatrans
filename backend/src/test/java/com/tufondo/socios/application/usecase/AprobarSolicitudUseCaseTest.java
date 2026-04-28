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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}