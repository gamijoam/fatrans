package com.tufondo.contabilidad.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartidaAsientoTest {

    private static final UUID CUENTA_ID = UUID.randomUUID();

    @Test
    @DisplayName("alDebe: crea partida con debe positivo y haber en cero")
    void al_debe_basico() {
        PartidaAsiento p = PartidaAsiento.alDebe(CUENTA_ID, new BigDecimal("100.50"), 1, "depósito");
        assertThat(p.getDebe()).isEqualByComparingTo("100.50");
        assertThat(p.getHaber()).isEqualByComparingTo("0");
        assertThat(p.esDeDebe()).isTrue();
        assertThat(p.esDeHaber()).isFalse();
        assertThat(p.monto()).isEqualByComparingTo("100.50");
        assertThat(p.getId()).isNotNull();
        assertThat(p.getCuentaId()).isEqualTo(CUENTA_ID);
        assertThat(p.getOrden()).isEqualTo(1);
        assertThat(p.getGlosa()).isEqualTo("depósito");
    }

    @Test
    @DisplayName("alHaber: crea partida con haber positivo y debe en cero")
    void al_haber_basico() {
        PartidaAsiento p = PartidaAsiento.alHaber(CUENTA_ID, new BigDecimal("250"), 2, null);
        assertThat(p.getHaber()).isEqualByComparingTo("250");
        assertThat(p.getDebe()).isEqualByComparingTo("0");
        assertThat(p.esDeHaber()).isTrue();
        assertThat(p.esDeDebe()).isFalse();
    }

    @Test
    @DisplayName("escala se normaliza a 4 decimales (igual que BD NUMERIC(18,4))")
    void escala_normalizada() {
        PartidaAsiento p = PartidaAsiento.alDebe(CUENTA_ID, new BigDecimal("100"), 1, null);
        assertThat(p.getDebe().scale()).isEqualTo(PartidaAsiento.ESCALA_MONTO);
    }

    @Test
    @DisplayName("monto cero al DEBE → rechazado")
    void rechaza_monto_cero() {
        assertThatThrownBy(() -> PartidaAsiento.alDebe(CUENTA_ID, BigDecimal.ZERO, 1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positivo");
    }

    @Test
    @DisplayName("monto negativo → rechazado")
    void rechaza_monto_negativo() {
        assertThatThrownBy(() -> PartidaAsiento.alHaber(CUENTA_ID, new BigDecimal("-10"), 1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positivo");
    }

    @Test
    @DisplayName("monto que excede MONTO_MAXIMO → rechazado (detecta typos)")
    void rechaza_monto_excesivo() {
        BigDecimal enorme = PartidaAsiento.MONTO_MAXIMO.add(BigDecimal.ONE);
        assertThatThrownBy(() -> PartidaAsiento.alDebe(CUENTA_ID, enorme, 1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    @DisplayName("orden < 1 → rechazado")
    void rechaza_orden_invalido() {
        assertThatThrownBy(() -> PartidaAsiento.alDebe(CUENTA_ID, BigDecimal.ONE, 0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orden");
    }

    @Test
    @DisplayName("cuentaId null → rechazado")
    void rechaza_cuenta_null() {
        assertThatThrownBy(() -> PartidaAsiento.alDebe(null, BigDecimal.TEN, 1, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("reconstruir con debe>0 Y haber>0 → rechazado (XOR)")
    void rechaza_debe_y_haber_simultaneos() {
        assertThatThrownBy(() -> PartidaAsiento.reconstruir(
                UUID.randomUUID(), CUENTA_ID,
                new BigDecimal("100"), new BigDecimal("100"),
                1, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEBE o HABER");
    }

    @Test
    @DisplayName("reconstruir con debe=0 Y haber=0 → rechazado")
    void rechaza_partida_vacia() {
        assertThatThrownBy(() -> PartidaAsiento.reconstruir(
                UUID.randomUUID(), CUENTA_ID,
                BigDecimal.ZERO, BigDecimal.ZERO,
                1, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("reconstruir con valor negativo → rechazado")
    void rechaza_negativo_en_reconstruir() {
        assertThatThrownBy(() -> PartidaAsiento.reconstruir(
                UUID.randomUUID(), CUENTA_ID,
                new BigDecimal("-1"), BigDecimal.ZERO,
                1, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativos");
    }

    @Test
    @DisplayName("igualdad se basa solo en id")
    void equals_por_id() {
        UUID id = UUID.randomUUID();
        PartidaAsiento p1 = PartidaAsiento.reconstruir(
                id, CUENTA_ID, BigDecimal.ONE.setScale(4), BigDecimal.ZERO.setScale(4),
                1, "uno", null);
        PartidaAsiento p2 = PartidaAsiento.reconstruir(
                id, UUID.randomUUID(), BigDecimal.TEN.setScale(4), BigDecimal.ZERO.setScale(4),
                5, "otra", null);
        assertThat(p1).isEqualTo(p2);
    }
}
