package com.tufondo.contabilidad.domain.model;

import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsientoContableTest {

    private static final UUID CAJA = UUID.randomUUID();
    private static final UUID DEPOSITOS = UUID.randomUUID();
    private static final UUID INGRESOS = UUID.randomUUID();
    private static final LocalDate HOY = LocalDate.of(2026, 5, 20);

    private static PartidaAsiento debe(UUID cuenta, String monto, int orden) {
        return PartidaAsiento.alDebe(cuenta, new BigDecimal(monto), orden, null);
    }

    private static PartidaAsiento haber(UUID cuenta, String monto, int orden) {
        return PartidaAsiento.alHaber(cuenta, new BigDecimal(monto), orden, null);
    }

    @Nested
    @DisplayName("Crear asiento — happy paths")
    class HappyPaths {

        @Test
        @DisplayName("asiento de depósito de socio: caja al DEBE, depósitos al HABER, balanceado")
        void asiento_deposito() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "Depósito de Juan Pérez", OrigenAsiento.AHORRO_DEPOSITO,
                    "OP-2026-000123", null, null,
                    List.of(
                            debe(CAJA, "1000.00", 1),
                            haber(DEPOSITOS, "1000.00", 2)
                    ));
            assertThat(a.getId()).isNotNull();
            assertThat(a.getNumero()).isNull();  // se asigna al persistir
            assertThat(a.getEstado()).isEqualTo(EstadoAsiento.REGISTRADO);
            assertThat(a.getPartidas()).hasSize(2);
            assertThat(a.totalDebe()).isEqualByComparingTo("1000.00");
            assertThat(a.totalHaber()).isEqualByComparingTo("1000.00");
            assertThat(a.estaBalanceado()).isTrue();
        }

        @Test
        @DisplayName("asiento complejo de 4 partidas (2 al DEBE, 2 al HABER)")
        void asiento_4_partidas() {
            // Caso real: pago de cuota de crédito con interés.
            // Caja  DEBE 500 (capital + interés)
            //   Créditos por cobrar HABER 400 (capital)
            //   Intereses ganados   HABER 100
            // ...total 500=500. Pero para hacerlo 4 partidas, separamos caja:
            //   Caja Bs DEBE 300
            //   Caja USD DEBE 200
            //   Créditos HABER 400
            //   Intereses HABER 100
            UUID cajaBs = UUID.randomUUID();
            UUID cajaUsd = UUID.randomUUID();
            UUID creditos = UUID.randomUUID();
            UUID intereses = UUID.randomUUID();
            AsientoContable a = AsientoContable.crear(
                    HOY, "Pago cuota crédito Juan Pérez", OrigenAsiento.CREDITO_COBRO,
                    "SOL-CRED-2026-000045", null, null,
                    List.of(
                            debe(cajaBs, "300.00", 1),
                            debe(cajaUsd, "200.00", 2),
                            haber(creditos, "400.00", 3),
                            haber(intereses, "100.00", 4)
                    ));
            assertThat(a.totalDebe()).isEqualByComparingTo("500.00");
            assertThat(a.totalHaber()).isEqualByComparingTo("500.00");
            assertThat(a.estaBalanceado()).isTrue();
        }

        @Test
        @DisplayName("partidas inmutables — lista no se puede modificar después")
        void partidas_inmutables() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2)));
            assertThatThrownBy(() -> a.getPartidas().add(debe(CAJA, "5", 3)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Validación crítica: partida doble (Σdebe = Σhaber)")
    class InvariantePartidaDoble {

        @Test
        @DisplayName("desbalance de 0.01 Bs → rechazado (no tolera errores)")
        void desbalance_minimo() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(
                            debe(CAJA, "100.00", 1),
                            haber(DEPOSITOS, "100.01", 2)  // 1 centavo de diferencia
                    )))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("desbalanceado");
        }

        @Test
        @DisplayName("desbalance grande → rechazado con diferencia clara en el mensaje")
        void desbalance_grande() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(
                            debe(CAJA, "1000.00", 1),
                            haber(DEPOSITOS, "500.00", 2)
                    )))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("diferencia=500");
        }

        @Test
        @DisplayName("4 decimales que cuadran exactamente → aceptado")
        void cuatro_decimales_exactos() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(
                            debe(CAJA, "0.0001", 1),
                            haber(DEPOSITOS, "0.0001", 2)
                    ));
            assertThat(a.estaBalanceado()).isTrue();
        }

        @Test
        @DisplayName("solo partidas al DEBE → rechazado")
        void solo_debe() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "100", 1), debe(DEPOSITOS, "100", 2))))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("solo partidas al HABER → rechazado (vía check de balance que se dispara primero)")
        void solo_haber() {
            // Dos partidas al HABER sumando 200 → Σdebe=0, Σhaber=200, desbalance
            // se detecta antes del check "al menos una al DEBE". Es defensa en
            // profundidad — el dominio rechaza por la primera invariante violada
            // sin importar cuál es. El check "al menos uno DEBE / al menos uno
            // HABER" queda como red de seguridad para casos edge donde el balance
            // técnicamente cuadrara (ej. cuatro partidas con montos 0, aunque eso
            // ya está bloqueado en PartidaAsiento.alHaber que exige monto > 0).
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(haber(CAJA, "100", 1), haber(DEPOSITOS, "100", 2))))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("una sola partida → rechazado (< MIN_PARTIDAS)")
        void una_sola_partida() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "100", 1))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("al menos");
        }

        @Test
        @DisplayName("cuenta duplicada en el mismo lado (DEBE) → rechazado")
        void cuenta_duplicada_lado_debe() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(
                            debe(CAJA, "100", 1),
                            debe(CAJA, "50", 2),  // CAJA duplicada al DEBE
                            haber(DEPOSITOS, "150", 3)
                    )))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cuenta duplicada al DEBE");
        }

        @Test
        @DisplayName("misma cuenta al DEBE y al HABER del MISMO asiento → permitido (caso legítimo)")
        void misma_cuenta_lados_distintos_aceptado() {
            // Ejemplo legítimo: ajuste entre dos sub-cuentas reflejado en la cuenta padre.
            UUID cuentaX = UUID.randomUUID();
            AsientoContable a = AsientoContable.crear(
                    HOY, "ajuste", OrigenAsiento.AJUSTE, null, null, null,
                    List.of(
                            debe(cuentaX, "100", 1),
                            haber(cuentaX, "100", 2)
                    ));
            // Es raro pero válido: Σdebe = Σhaber = 100, no hay duplicado en
            // el mismo lado, ambas cuentas-lado son distintos.
            assertThat(a.estaBalanceado()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validaciones de campos generales")
    class CamposGenerales {

        @Test
        @DisplayName("fechaContable null → rechazado")
        void fecha_null() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    null, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2))))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("fechaContable");
        }

        @Test
        @DisplayName("glosa vacía → rechazado")
        void glosa_vacia() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "  ", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("glosa");
        }

        @Test
        @DisplayName("glosa > 500 chars → rechazado")
        void glosa_muy_larga() {
            String glosa = "x".repeat(501);
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, glosa, OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("500");
        }

        @Test
        @DisplayName("origen null → rechazado")
        void origen_null() {
            assertThatThrownBy(() -> AsientoContable.crear(
                    HOY, "test", null, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2))))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Anulación")
    class Anular {

        @Test
        @DisplayName("anular devuelve nueva instancia con estado ANULADO y motivo")
        void anular_basico() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2)));
            AsientoContable anulado = a.anular("error en monto");

            assertThat(anulado.getEstado()).isEqualTo(EstadoAsiento.ANULADO);
            assertThat(anulado.getMotivoAnulacion()).isEqualTo("error en monto");
            assertThat(a.getEstado()).isEqualTo(EstadoAsiento.REGISTRADO);  // inmutabilidad
        }

        @Test
        @DisplayName("anular sin motivo → rechazado")
        void anular_sin_motivo() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2)));
            assertThatThrownBy(() -> a.anular(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("anular un asiento ya anulado → rechazado")
        void doble_anulacion() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2)));
            AsientoContable anulado = a.anular("primer motivo");
            assertThatThrownBy(() -> anulado.anular("segundo motivo"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya está anulado");
        }
    }

    @Nested
    @DisplayName("Asignación de número correlativo")
    class CorrelativoNumerico {

        @Test
        @DisplayName("conNumero asigna correlativo a asiento nuevo")
        void asignar_numero() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2)));
            assertThat(a.getNumero()).isNull();
            AsientoContable conNumero = a.conNumero(42L);
            assertThat(conNumero.getNumero()).isEqualTo(42L);
        }

        @Test
        @DisplayName("conNumero rechaza re-asignación")
        void no_reasignar_numero() {
            AsientoContable a = AsientoContable.crear(
                    HOY, "test", OrigenAsiento.MANUAL, null, null, null,
                    List.of(debe(CAJA, "10", 1), haber(DEPOSITOS, "10", 2)));
            AsientoContable conNumero = a.conNumero(42L);
            assertThatThrownBy(() -> conNumero.conNumero(43L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya tiene número");
        }
    }
}
