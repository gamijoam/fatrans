// com.tufondo.documentospdf.TestDataFactory
package com.tufondo.documentospdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Fábrica de datos de prueba para el módulo de Documentos PDF.
 * Proporciona datos realistas y consistentes para tests unitarios e integración.
 */
public class TestDataFactory {

    // ==================== UUIDs Fijos para Tests ====================

    public static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SOCIO_ID_OTRO = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID CUENTA_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID CREDITO_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID SOLICITUD_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID DOCUMENTO_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    // Clave privada RSA 2048 bits para tests (Base64 encoded PKCS8)
    public static final String TEST_PRIVATE_KEY_BASE64 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQChru7vGPv18sJK" +
            "AJVFHGEt+DscCQa32nXMHZqtZOBkZM1sGfoyLHlwcgiu5vWmfHLAB1vcwjdvI3m+jFJBtEXqOOGw5hVwSEck/GsH" +
            "+h39UqniZJTJQDwcAd1yRjb8mbdEHR2zPkiVPd8LZdjTXcXKTQq1XfHsX3RhqakdVstwFG8ZMKxrajk0q77zC8qj1Z" +
            "zvOCGJ9DZGt4lCoq4b646OlJ0L+wdeuUwvf67gdWbER4WPBDgKKTCbjCTVixXDxxQxzVdzMvAwG2kDk51YVQP+Vdtd8" +
            "PzMGhq9h61L7dWnRG2bKP+wKMQstQClggTMWQcVGWIf3V3eCcEld8sC4ek/AgMBAAECggEAARyp+dWJm+VrszNR03" +
            "43vxBH/CdgGhbfc8zVadz21hzU0e/fb7n+Nrp9rp2IY1YCUdgj1t7XE9nfJ5GbfQlI6FtLbbSTB1p6z983dTpnr9nID" +
            "g2iq/Y085/+7nChLxeO0jN3m41AG6JKkpg454hUS7qdAYNozOW75HI9W7HAk1ccYkJeJaWYDIGSDC0ZQF3bBF85auU" +
            "iRbGks+nudFJlcWKD6mTgDaKjd/Qvf3vRf6/6Zc1fydlQ76ztgHl88V+4LZNPVPhLEBhmXgpcI0ZJVbbwlZFoucXF3" +
            "9NnzGHUD8tIcbXHGUk4bU2duCZbCWWkOTjY9qZP+j1eqrqfwuFcQQKBgQDMz6z74eERIzzdmBLrBPDLIa5E96liuH" +
            "X0G7XBzSFTjtiZqOicAYyq5+vabHdPUjnquAzZjFdn99AnIs+oj1dfB4lbOWiPXCZmQBLCybqvHA5a+Dt7zzeWUao" +
            "1UMu9eEctfprjR0Ch5Gu7XmkIGDIK7cJhNExsAx2/t/pEhoEBMQKBgQDKF9Q2xtE0F/LZ9fH4S6axWdhcc7jDSR1rlI" +
            "uKD1BNTGHywsPJDCEo8kS6g6oX7smbRjYugBVkHQwuQ3eZ19ekhsY03SqAaeWRVRxF9M2eg1Ll08u/AAJrwwTSDC2C+" +
            "FHlzuAPGo1RfO2bo5Up3mqiVjwMdkCo115S8+8moBd1bwKBgCcVMjNWpTY5/TFkBagnZzO8F589i2s1O/z7FLIIzOU/" +
            "CwI3gzvR59/tJcqmjBXs4PRzbrBM4ZkLZwMw12OZBtkxfPqVd+hzczhr3aiiCifEONEPMXqszjS3HL3Tzy02uI5r7G" +
            "6WeDTse8pjd7N9un7vY/pcmoC6D4E4QF+3MwLhAoGAd3kO5DM1im5+C4zyt69BpKdQ2ZcwK4MV+Xuf4saa2pfFcBbkV" +
            "/7ru6E+MGWWMP/iAAHKFkNyYfUqNk98bV7FBWcH1kjJz5DLPtfqFxFaXdOVNnm3gfsncz2fo+2/GiZjhFCA9WzlDr+" +
            "kWNakYFQ1BjOk3FkscKSiBrhfMZ6DP9ECgYEAsouAc84AqQxgEj5AM15a4ivFfquDtR/BFErK1Og0HBXrAgZ4Q8fQKP" +
            "Ab1rtgHrk5HgQnogM9ze0KC6YgYkmMakTSTnAkCki5wj01GEI4yC6yb6cM13HIzux4PeGjYMGr9R7OWTgvHZcAHvlT" +
            "29NzOWkw0P8cuCweNXNLPY89pk0=";

    // ==================== Datos de Cuenta ====================

    /**
     * Crea datos de prueba para estado de cuenta.
     */
    public static Map<String, Object> crearDatosEstadoCuenta() {
        Map<String, Object> cuenta = new HashMap<>();
        cuenta.put("numeroCuenta", "001-002-003-004-005");
        cuenta.put("nombreCompleto", "Juan María Pérez Rodríguez");
        cuenta.put("socioId", SOCIO_ID);
        cuenta.put("saldoAnterior", new BigDecimal("15000.00"));
        cuenta.put("totalAbonos", new BigDecimal("5000.00"));
        cuenta.put("totalCargos", new BigDecimal("2500.00"));
        cuenta.put("saldoActual", new BigDecimal("17500.00"));

        List<Map<String, Object>> movimientos = crearMovimientosPrueba();

        Map<String, Object> datos = new HashMap<>();
        datos.put("cuenta", cuenta);
        datos.put("movimientos", movimientos);
        datos.put("periodo", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        datos.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        datos.put("socioId", SOCIO_ID.toString());
        datos.put("generadoPor", SOCIO_ID.toString());

        return datos;
    }

    /**
     * Crea lista de movimientos de prueba.
     */
    public static List<Map<String, Object>> crearMovimientosPrueba() {
        List<Map<String, Object>> movimientos = new ArrayList<>();

        movimientos.add(crearMovimiento("19/04/2026", "Depósito inicial", "5000.00", "0.00"));
        movimientos.add(crearMovimiento("20/04/2026", "Transferencia recibida", "2000.00", "0.00"));
        movimientos.add(crearMovimiento("21/04/2026", "Pago servicios", "0.00", "500.00"));
        movimientos.add(crearMovimiento("22/04/2026", "Retiro Cajero", "0.00", "1000.00"));
        movimientos.add(crearMovimiento("23/04/2026", "Depósito nomina", "3000.00", "0.00"));
        movimientos.add(crearMovimiento("24/04/2026", "Pago tarjeta crédito", "0.00", "1000.00"));

        return movimientos;
    }

    private static Map<String, Object> crearMovimiento(String fecha, String descripcion,
                                                          String credito, String debito) {
        Map<String, Object> movimiento = new HashMap<>();
        movimiento.put("fecha", fecha);
        movimiento.put("descripcion", descripcion);
        movimiento.put("credito", new BigDecimal(credito));
        movimiento.put("debito", new BigDecimal(debito));
        return movimiento;
    }

    // ==================== Datos de Socio ====================

    /**
     * Crea datos de prueba para constancia de afiliación.
     */
    public static Map<String, Object> crearDatosConstanciaAfiliacion() {
        Map<String, Object> socio = new HashMap<>();
        socio.put("nombreCompleto", "María del Carmen López Hernández");
        socio.put("cedula", "V-12.345.678");
        socio.put("socioId", SOCIO_ID);
        socio.put("fechaAfiliacion", "15/03/2020");
        socio.put("numeroSocio", "SOC-2020-00123");

        Map<String, Object> datos = new HashMap<>();
        datos.put("socio", socio);
        datos.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        datos.put("socioId", SOCIO_ID.toString());
        datos.put("generadoPor", SOCIO_ID.toString());

        return datos;
    }

    /**
     * Crea datos de prueba para carta de beneficiarios.
     */
    public static Map<String, Object> crearDatosCartaBeneficiarios() {
        Map<String, Object> socio = new HashMap<>();
        socio.put("nombreCompleto", "Pedro Antonio Martínez Sánchez");
        socio.put("cedula", "V-98.765.432");
        socio.put("socioId", SOCIO_ID);

        List<Map<String, Object>> beneficiarios = new ArrayList<>();
        beneficiarios.add(crearBeneficiario("Ana María Martínez", "V-23.456.789", "50"));
        beneficiarios.add(crearBeneficiario("Luis Eduardo Martínez", "V-23.456.790", "30"));
        beneficiarios.add(crearBeneficiario("Sofía Elena Martínez", "V-23.456.791", "20"));

        Map<String, Object> datos = new HashMap<>();
        datos.put("socio", socio);
        datos.put("beneficiarios", beneficiarios);
        datos.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        datos.put("socioId", SOCIO_ID.toString());
        datos.put("generadoPor", SOCIO_ID.toString());

        return datos;
    }

    private static Map<String, Object> crearBeneficiario(String nombre, String cedula, String porcentaje) {
        Map<String, Object> beneficiario = new HashMap<>();
        beneficiario.put("nombre", nombre);
        beneficiario.put("cedula", cedula);
        beneficiario.put("porcentaje", porcentaje);
        return beneficiario;
    }

    // ==================== Datos de Crédito ====================

    /**
     * Crea datos de prueba para pagaré.
     */
    public static Map<String, Object> crearDatosPagare() {
        Map<String, Object> credito = new HashMap<>();
        credito.put("numeroCredito", "CRE-2026-001234");
        credito.put("montoConcedido", new BigDecimal("50000.00"));
        credito.put("tasaInteres", new BigDecimal("18.5"));
        credito.put("plazoMeses", 36);
        credito.put("cuotaMensual", new BigDecimal("1834.56"));
        credito.put("fechaDesembolso", "15/04/2026");

        Map<String, Object> socio = new HashMap<>();
        socio.put("nombreCompleto", "Carlos Alberto Rodríguez Giménez");
        socio.put("cedula", "V-30.987.654");
        socio.put("socioId", SOCIO_ID);

        List<Map<String, Object>> tablaAmortizacion = crearTablaAmortizacionPrueba();

        Map<String, Object> datos = new HashMap<>();
        datos.put("credito", credito);
        datos.put("socio", socio);
        datos.put("tablaAmortizacion", tablaAmortizacion);
        datos.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        datos.put("socioId", SOCIO_ID.toString());
        datos.put("generadoPor", "SYSTEM");

        return datos;
    }

    /**
     * Crea datos de prueba para tabla de amortización.
     */
    public static Map<String, Object> crearDatosTablaAmortizacion() {
        Map<String, Object> credito = new HashMap<>();
        credito.put("numeroCredito", "CRE-2026-001234");
        credito.put("montoConcedido", new BigDecimal("50000.00"));
        credito.put("tasaInteres", new BigDecimal("18.5"));
        credito.put("plazoMeses", 36);

        Map<String, Object> socio = new HashMap<>();
        socio.put("nombreCompleto", "Carlos Alberto Rodríguez Giménez");
        socio.put("cedula", "V-30.987.654");
        socio.put("socioId", SOCIO_ID);

        List<Map<String, Object>> tablaAmortizacion = crearTablaAmortizacionPrueba();

        Map<String, Object> datos = new HashMap<>();
        datos.put("credito", credito);
        datos.put("socio", socio);
        datos.put("tablaAmortizacion", tablaAmortizacion);
        datos.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        datos.put("socioId", SOCIO_ID.toString());
        datos.put("generadoPor", SOCIO_ID.toString());

        return datos;
    }

    /**
     * Crea tabla de amortización de prueba (primeros 6 meses).
     */
    public static List<Map<String, Object>> crearTablaAmortizacionPrueba() {
        List<Map<String, Object>> tabla = new ArrayList<>();

        String[] fechas = {"15/05/2026", "15/06/2026", "15/07/2026", "15/08/2026", "15/09/2026", "15/10/2026"};
        BigDecimal saldo = new BigDecimal("50000.00");

        for (int i = 0; i < 6; i++) {
            BigDecimal interes = saldo.multiply(new BigDecimal("0.185")).divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal cuota = new BigDecimal("1834.56");
            BigDecimal capital = cuota.subtract(interes);
            saldo = saldo.subtract(capital);

            Map<String, Object> fila = new HashMap<>();
            fila.put("numero", i + 1);
            fila.put("fecha", fechas[i]);
            fila.put("capital", capital);
            fila.put("interes", interes);
            fila.put("cuota", cuota);
            fila.put("saldo", saldo.max(BigDecimal.ZERO));

            tabla.add(fila);
        }

        return tabla;
    }

    // ==================== Datos de Contrato ====================

    /**
     * Crea datos de prueba para contrato de adhesión.
     */
    public static Map<String, Object> crearDatosContratoAdhesion() {
        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("numeroSolicitud", "SOL-2026-005678");
        solicitud.put("tipoProducto", "Crédito Personal");
        solicitud.put("montoSolicitado", new BigDecimal("75000.00"));
        solicitud.put("plazoSolicitado", 48);
        solicitud.put("socioId", SOCIO_ID);

        Map<String, Object> socio = new HashMap<>();
        socio.put("nombreCompleto", "Fernanda del Valle Morales");
        socio.put("cedula", "V-25.678.901");
        socio.put("socioId", SOCIO_ID);
        socio.put("direccion", "Av. Principal, Calle 5, Casa #12, Caracas");
        socio.put("telefono", "0212-123-4567");

        Map<String, Object> datos = new HashMap<>();
        datos.put("solicitud", solicitud);
        datos.put("socio", socio);
        datos.put("fechaContrato", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        datos.put("socioId", SOCIO_ID.toString());
        datos.put("generadoPor", "ADMIN");

        return datos;
    }

    // ==================== Mock Results ====================

    /**
     * Crea resultado mock para upload a MinIO.
     */
    public static com.tufondo.documentospdf.application.port.StoragePort.UploadResult crearUploadResultMock() {
        return new com.tufondo.documentospdf.application.port.StoragePort.UploadResult(
                "bucket-documentos",
                "estados-cuenta/" + SOCIO_ID + "/EstadoCuenta_2026-04_" + CUENTA_ID + ".pdf",
                45872L,
                "\"abc123def456\""
        );
    }

    /**
     * Crea resultado mock para escaneo de malware.
     */
    public static com.tufondo.documentospdf.application.port.MalwareScannerPort.ScanResult crearScanResultClean() {
        return new com.tufondo.documentospdf.application.port.MalwareScannerPort.ScanResult(
                true,
                null,
                "Stream scan: OK"
        );
    }

    /**
     * Crea resultado mock para escaneo de malware malicioso.
     */
    public static com.tufondo.documentospdf.application.port.MalwareScannerPort.ScanResult crearScanResultMalicious() {
        return new com.tufondo.documentospdf.application.port.MalwareScannerPort.ScanResult(
                false,
                "Test.Malware.Mock",
                "Stream scan: FOUND"
        );
    }
}
