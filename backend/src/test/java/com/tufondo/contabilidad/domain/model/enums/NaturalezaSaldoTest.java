package com.tufondo.contabilidad.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de la mecánica de partida doble en {@link NaturalezaSaldo}.
 *
 * <p>Críticos para precisión contable. Si estos fallan, los reportes de
 * balance van a estar mal. Cubren los casos límite: ceros, valores
 * exactamente iguales (saldo cero), valores muy grandes (sin overflow).</p>
 */
class NaturalezaSaldoTest {

    @Test
    @DisplayName("DEUDORA: saldo = debe − haber (signo +1)")
    void deudora_signo_positivo() {
        assertThat(NaturalezaSaldo.DEUDORA.signoSaldo()).isEqualTo(1);
    }

    @Test
    @DisplayName("ACREEDORA: saldo = haber − debe (signo −1)")
    void acreedora_signo_negativo() {
        assertThat(NaturalezaSaldo.ACREEDORA.signoSaldo()).isEqualTo(-1);
    }

    @ParameterizedTest(name = "DEUDORA: debe={0} haber={1} → saldo {2}")
    @CsvSource({
            "100, 30, 70",
            "1000, 1000, 0",       // saldo cero
            "0, 0, 0",             // sin movimientos
            "50, 80, -30",         // saldo "anormal" (más al haber que al debe en una deudora)
            "1000000.5555, 500000.4444, 500000.1111", // decimales precisos
    })
    void deudora_calcula_saldo_correctamente(String debe, String haber, String esperado) {
        BigDecimal saldo = NaturalezaSaldo.DEUDORA.calcularSaldo(
                new BigDecimal(debe), new BigDecimal(haber));
        assertThat(saldo).isEqualByComparingTo(new BigDecimal(esperado));
    }

    @ParameterizedTest(name = "ACREEDORA: debe={0} haber={1} → saldo {2}")
    @CsvSource({
            "30, 100, 70",          // operación normal de una acreedora
            "0, 0, 0",
            "1000, 1000, 0",
            "80, 50, -30",          // saldo "anormal"
            "500000.4444, 1000000.5555, 500000.1111",
    })
    void acreedora_calcula_saldo_correctamente(String debe, String haber, String esperado) {
        BigDecimal saldo = NaturalezaSaldo.ACREEDORA.calcularSaldo(
                new BigDecimal(debe), new BigDecimal(haber));
        assertThat(saldo).isEqualByComparingTo(new BigDecimal(esperado));
    }

    @Test
    @DisplayName("Operación reversa: deudora con saldo+ vs acreedora con saldo+ con los mismos totales")
    void deudora_acreedora_son_opuestos_perfectos() {
        // En cualquier cuenta, dadas las mismas sumas, el saldo de la deudora
        // tiene signo opuesto al de la acreedora. Propiedad clave para que
        // la partida doble cuadre (sumas debe = sumas haber del asiento).
        BigDecimal debe = new BigDecimal("250.7575");
        BigDecimal haber = new BigDecimal("100.2525");
        BigDecimal saldoDeudora = NaturalezaSaldo.DEUDORA.calcularSaldo(debe, haber);
        BigDecimal saldoAcreedora = NaturalezaSaldo.ACREEDORA.calcularSaldo(debe, haber);
        assertThat(saldoDeudora.add(saldoAcreedora)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
