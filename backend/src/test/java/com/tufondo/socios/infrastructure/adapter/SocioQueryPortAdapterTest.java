package com.tufondo.socios.infrastructure.adapter;

import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.model.enums.TipoContrato;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import com.tufondo.socios.infrastructure.persistence.entity.SocioEntity;
import com.tufondo.socios.infrastructure.persistence.jpa.SocioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests del adaptador {@link SocioQueryPortAdapter} (issue #199).
 *
 * <p>Cobertura de los dos métodos que siguen vivos tras el cleanup del
 * issue #199 (que eliminó {@code obtenerSocioIdPorCuenta}, un stub sin
 * callers reales que solo lanzaba excepción).</p>
 */
@ExtendWith(MockitoExtension.class)
class SocioQueryPortAdapterTest {

    @Mock
    private SocioJpaRepository socioJpaRepository;

    @InjectMocks
    private SocioQueryPortAdapter adapter;

    private UUID socioId;

    @BeforeEach
    void setUp() {
        socioId = UUID.randomUUID();
    }

    // === existeSocio ===

    @Test
    @DisplayName("existeSocio: delega en existsById y devuelve true cuando el socio existe")
    void existeSocio_devuelve_true_cuando_existe() {
        when(socioJpaRepository.existsById(socioId)).thenReturn(true);

        assertThat(adapter.existeSocio(socioId)).isTrue();
    }

    @Test
    @DisplayName("existeSocio: devuelve false cuando el socio no existe")
    void existeSocio_devuelve_false_cuando_no_existe() {
        when(socioJpaRepository.existsById(socioId)).thenReturn(false);

        assertThat(adapter.existeSocio(socioId)).isFalse();
    }

    // === obtenerDatosSocioParaPdf ===

    @Test
    @DisplayName("obtenerDatosSocioParaPdf: devuelve null cuando el socio no existe (no rompe el PDF)")
    void obtenerDatos_devuelve_null_cuando_socio_no_existe() {
        when(socioJpaRepository.findById(socioId)).thenReturn(Optional.empty());

        Map<String, Object> datos = adapter.obtenerDatosSocioParaPdf(socioId);

        assertThat(datos).isNull();
    }

    @Test
    @DisplayName("obtenerDatosSocioParaPdf: mapea todos los campos del socio al map del PDF")
    void obtenerDatos_mapea_campos_del_socio() {
        SocioEntity socio = nuevoSocioMock();
        when(socioJpaRepository.findById(socioId)).thenReturn(Optional.of(socio));

        Map<String, Object> datos = adapter.obtenerDatosSocioParaPdf(socioId);

        assertThat(datos).isNotNull();
        assertThat(datos.get("id")).isEqualTo(socioId);
        assertThat(datos.get("numeroSocio")).isEqualTo("SOC-001");
        assertThat(datos.get("primerNombre")).isEqualTo("Juan");
        assertThat(datos.get("primerApellido")).isEqualTo("Pérez");
        // El nombre completo concatena nombres y apellidos saltando vacíos
        assertThat(datos.get("nombreCompleto")).isEqualTo("Juan Carlos Pérez González");
        assertThat(datos.get("cedula")).isEqualTo("V-12345678");
        assertThat(datos.get("numeroDocumento")).isEqualTo("V-12345678");
        // Enums serializados como String (.name())
        assertThat(datos.get("tipoDocumento")).isEqualTo("CEDULA");
        assertThat(datos.get("estado")).isEqualTo("ACTIVO");
        assertThat(datos.get("tipoContrato")).isEqualTo("INDEFINIDO");
        // Datos laborales
        assertThat(datos.get("empresa")).isEqualTo("Cooperativa Transporte XYZ");
        assertThat(datos.get("cargo")).isEqualTo("Conductor");
    }

    @Test
    @DisplayName("obtenerDatosSocioParaPdf: maneja nombres/apellidos opcionales sin null pointer")
    void obtenerDatos_construye_nombre_sin_segundos() {
        SocioEntity socio = nuevoSocioMockMinimo();
        when(socioJpaRepository.findById(socioId)).thenReturn(Optional.of(socio));

        Map<String, Object> datos = adapter.obtenerDatosSocioParaPdf(socioId);

        assertThat(datos).isNotNull();
        // Sin segundo nombre ni segundo apellido — el helper no debe romperse
        assertThat(datos.get("nombreCompleto")).isEqualTo("María López");
    }

    @Test
    @DisplayName("obtenerDatosSocioParaPdf: enums null no rompen el mapeo (serializa a null)")
    void obtenerDatos_enums_null_no_rompen() {
        SocioEntity socio = nuevoSocioMockMinimo();
        when(socioJpaRepository.findById(socioId)).thenReturn(Optional.of(socio));

        Map<String, Object> datos = adapter.obtenerDatosSocioParaPdf(socioId);

        // Estado y tipoDocumento se quedaron null en el mock mínimo
        assertThat(datos).containsKey("estado").containsKey("tipoDocumento").containsKey("tipoContrato");
        assertThat(datos.get("tipoContrato")).isNull();
    }

    // === Helpers ===

    /** Socio mock con todos los datos relevantes para el PDF. */
    private SocioEntity nuevoSocioMock() {
        SocioEntity socio = org.mockito.Mockito.mock(SocioEntity.class);
        lenient().when(socio.getId()).thenReturn(socioId);
        lenient().when(socio.getNumeroSocio()).thenReturn("SOC-001");
        lenient().when(socio.getPrimerNombre()).thenReturn("Juan");
        lenient().when(socio.getSegundoNombre()).thenReturn("Carlos");
        lenient().when(socio.getPrimerApellido()).thenReturn("Pérez");
        lenient().when(socio.getSegundoApellido()).thenReturn("González");
        lenient().when(socio.getTipoDocumento()).thenReturn(TipoDocumento.CEDULA);
        lenient().when(socio.getNumeroDocumento()).thenReturn("V-12345678");
        lenient().when(socio.getCorreoElectronico()).thenReturn("juan@example.com");
        lenient().when(socio.getTelefonoPrincipal()).thenReturn("+584141234567");
        lenient().when(socio.getEmpresa()).thenReturn("Cooperativa Transporte XYZ");
        lenient().when(socio.getCargo()).thenReturn("Conductor");
        lenient().when(socio.getTipoContrato()).thenReturn(TipoContrato.INDEFINIDO);
        lenient().when(socio.getEstado()).thenReturn(EstadoSocio.ACTIVO);
        lenient().when(socio.getFechaRegistro()).thenReturn(LocalDateTime.now());
        return socio;
    }

    /** Socio mock sin segundo nombre ni apellido — para probar el helper. */
    private SocioEntity nuevoSocioMockMinimo() {
        SocioEntity socio = org.mockito.Mockito.mock(SocioEntity.class);
        lenient().when(socio.getId()).thenReturn(socioId);
        lenient().when(socio.getPrimerNombre()).thenReturn("María");
        lenient().when(socio.getSegundoNombre()).thenReturn("");  // vacío, no null
        lenient().when(socio.getPrimerApellido()).thenReturn("López");
        lenient().when(socio.getSegundoApellido()).thenReturn(null);  // null directo
        return socio;
    }
}
