package com.tufondo.contabilidad.domain.model;

import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Cuenta del plan contable (VEN-NIF).
 *
 * <p>El plan de cuentas es una <b>jerarquía de cuentas</b> identificadas por
 * un código numérico estructurado: el primer dígito identifica el tipo
 * (1=ACTIVO, 2=PASIVO, 3=PATRIMONIO, 4=INGRESO, 5=EGRESO, 6=CUENTA_ORDEN),
 * y los siguientes dígitos forman la sub-clasificación según el nivel. Ejemplos:</p>
 *
 * <pre>
 *   Nivel 1: "1"       → ACTIVO (rubro)
 *   Nivel 2: "1.1"     → ACTIVO DISPONIBLE (grupo)
 *   Nivel 3: "1.1.01"  → CAJA PRINCIPAL (cuenta)
 *   Nivel 4: "1.1.01.001" → opcional, sub-cuenta (multi-sucursal, etc.)
 * </pre>
 *
 * <p><b>Invariantes</b>:</p>
 * <ul>
 *   <li>El código no es nulo ni vacío y matchea {@link #PATTERN_CODIGO}.</li>
 *   <li>El primer segmento del código (antes del primer ".") coincide con el
 *       primer dígito del {@link TipoCuentaContable} (1-6).</li>
 *   <li>Las cuentas de nivel 1 (rubro) NO tienen padre. Cuentas de nivel ≥ 2
 *       deben tener padre, y el padre debe tener nivel exactamente
 *       {@code nivel - 1}.</li>
 *   <li>Solo las cuentas que {@link #aceptaMovimientos} pueden ser
 *       referenciadas desde una partida contable. Las totalizadoras
 *       (rubros/grupos) NO aceptan movimientos directos — solo suman los
 *       de sus hijas.</li>
 * </ul>
 *
 * <p>Las invariantes se validan en los métodos de construcción
 * {@link #crear} y {@link #reconstruir}. Las violaciones lanzan
 * {@link IllegalArgumentException}.</p>
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class CuentaContable {

    /**
     * Patrón del código contable. Permite niveles 1 a 5:
     * <ul>
     *   <li>"1" hasta "6" (nivel 1, tipo)</li>
     *   <li>"1.1", "1.99" (nivel 2)</li>
     *   <li>"1.1.01", "1.1.99" (nivel 3)</li>
     *   <li>"1.1.01.001" (nivel 4)</li>
     *   <li>"1.1.01.001.001" (nivel 5)</li>
     * </ul>
     * El primer dígito está restringido a 1-6 (los 6 tipos definidos en
     * {@link TipoCuentaContable}). Los segmentos siguientes pueden ser de
     * 1 a 3 dígitos numéricos. El nivel se infiere del número de segmentos.
     */
    private static final Pattern PATTERN_CODIGO =
            Pattern.compile("^[1-6](\\.\\d{1,3}){0,4}$");

    /** Mapeo del primer dígito al tipo de cuenta. Defensa contra inconsistencia código↔tipo. */
    private static final java.util.Map<Character, TipoCuentaContable> PREFIJO_A_TIPO =
            java.util.Map.of(
                    '1', TipoCuentaContable.ACTIVO,
                    '2', TipoCuentaContable.PASIVO,
                    '3', TipoCuentaContable.PATRIMONIO,
                    '4', TipoCuentaContable.INGRESO,
                    '5', TipoCuentaContable.EGRESO,
                    '6', TipoCuentaContable.CUENTA_ORDEN
            );

    private final UUID id;
    /** Código jerárquico VEN-NIF (ej. "1.1.01"). Único en el plan. */
    private final String codigo;
    /** Nombre legible (ej. "Caja Principal"). Hasta 200 chars. */
    private final String nombre;
    /** Tipo (ACTIVO/PASIVO/...). Debe coincidir con el primer dígito del código. */
    private final TipoCuentaContable tipo;
    /** Naturaleza del saldo (DEUDORA/ACREEDORA). */
    private final NaturalezaSaldo naturaleza;
    /**
     * Nivel jerárquico (1=rubro raíz, 2=grupo, 3=cuenta, 4=subcuenta, 5=detalle).
     * Se deriva del código (cantidad de segmentos separados por "."), pero se
     * almacena explícitamente para queries indexados.
     */
    private final int nivel;
    /**
     * ID de la cuenta padre. {@code null} solo si {@code nivel == 1}.
     * El código del padre es prefijo del código de esta cuenta.
     */
    private final UUID cuentaPadreId;
    /**
     * Si esta cuenta puede recibir movimientos directos.
     * Cuentas totalizadoras (rubros y grupos) tienen {@code false}.
     * Cuentas hoja (operativas) tienen {@code true}.
     */
    private final boolean aceptaMovimientos;
    /**
     * Si la cuenta está activa. Cuentas inactivas no aceptan nuevos asientos
     * pero conservan su histórico. Para "borrar" una cuenta usar
     * {@link #desactivar()} en lugar de eliminar físicamente.
     */
    private final boolean activa;
    /**
     * Descripción opcional / observaciones del contador. Hasta 500 chars.
     */
    private final String descripcion;

    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long version;

    @Builder(toBuilder = true)
    private CuentaContable(
            UUID id, String codigo, String nombre, TipoCuentaContable tipo,
            NaturalezaSaldo naturaleza, int nivel, UUID cuentaPadreId,
            boolean aceptaMovimientos, boolean activa, String descripcion,
            Instant createdAt, Instant updatedAt, Long version) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.naturaleza = naturaleza;
        this.nivel = nivel;
        this.cuentaPadreId = cuentaPadreId;
        this.aceptaMovimientos = aceptaMovimientos;
        this.activa = activa;
        this.descripcion = descripcion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /**
     * Crea una cuenta nueva (ID auto-generado, timestamps null hasta persistir).
     * Valida todas las invariantes — lanza {@link IllegalArgumentException}
     * si algo no cuadra.
     */
    public static CuentaContable crear(
            String codigo, String nombre, TipoCuentaContable tipo,
            NaturalezaSaldo naturaleza, UUID cuentaPadreId,
            boolean aceptaMovimientos, String descripcion) {
        int nivelInferido = inferirNivel(codigo);
        validarInvariantes(codigo, nombre, tipo, naturaleza, nivelInferido, cuentaPadreId);
        return CuentaContable.builder()
                .id(UUID.randomUUID())
                .codigo(codigo)
                .nombre(nombre)
                .tipo(tipo)
                .naturaleza(naturaleza)
                .nivel(nivelInferido)
                .cuentaPadreId(cuentaPadreId)
                .aceptaMovimientos(aceptaMovimientos)
                .activa(true)
                .descripcion(descripcion)
                .build();
    }

    /**
     * Reconstruye una cuenta desde persistencia (no genera ID nuevo).
     * Conserva la validación de invariantes — si la BD tiene basura, falla
     * temprano.
     */
    public static CuentaContable reconstruir(
            UUID id, String codigo, String nombre, TipoCuentaContable tipo,
            NaturalezaSaldo naturaleza, int nivel, UUID cuentaPadreId,
            boolean aceptaMovimientos, boolean activa, String descripcion,
            Instant createdAt, Instant updatedAt, Long version) {
        Objects.requireNonNull(id, "id es requerido al reconstruir");
        validarInvariantes(codigo, nombre, tipo, naturaleza, nivel, cuentaPadreId);
        return CuentaContable.builder()
                .id(id)
                .codigo(codigo)
                .nombre(nombre)
                .tipo(tipo)
                .naturaleza(naturaleza)
                .nivel(nivel)
                .cuentaPadreId(cuentaPadreId)
                .aceptaMovimientos(aceptaMovimientos)
                .activa(activa)
                .descripcion(descripcion)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .version(version)
                .build();
    }

    /**
     * Devuelve una nueva instancia con {@code activa=false}. Operación de
     * "borrado lógico": preserva el histórico de asientos que referencian
     * esta cuenta pero impide registrar nuevos.
     */
    public CuentaContable desactivar() {
        return this.toBuilder().activa(false).build();
    }

    /** Marca la cuenta como activa nuevamente. */
    public CuentaContable reactivar() {
        return this.toBuilder().activa(true).build();
    }

    /**
     * Devuelve {@code true} si esta cuenta es hija (directa o indirecta) de la
     * cuenta con el código dado. Útil para reportes jerárquicos.
     */
    public boolean esDescendienteDe(String codigoAncestro) {
        if (codigoAncestro == null || codigoAncestro.equals(codigo)) return false;
        return codigo.startsWith(codigoAncestro + ".");
    }

    /**
     * Calcula el código del padre desde el código de esta cuenta (quita el
     * último segmento). Útil para validaciones y para reconstruir relaciones
     * jerárquicas durante el seed inicial.
     *
     * @return código del padre, o {@code null} si esta cuenta es nivel 1
     */
    public String codigoPadre() {
        int idx = codigo.lastIndexOf('.');
        return idx < 0 ? null : codigo.substring(0, idx);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Validaciones (privadas — corren en crear/reconstruir)
    // ─────────────────────────────────────────────────────────────────────

    private static void validarInvariantes(
            String codigo, String nombre, TipoCuentaContable tipo,
            NaturalezaSaldo naturaleza, int nivel, UUID cuentaPadreId) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("codigo es requerido");
        }
        if (!PATTERN_CODIGO.matcher(codigo).matches()) {
            throw new IllegalArgumentException(
                    "codigo no respeta formato VEN-NIF (1-6 inicial + segmentos numéricos separados por '.'): "
                            + codigo);
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre es requerido");
        }
        if (nombre.length() > 200) {
            throw new IllegalArgumentException("nombre no puede exceder 200 caracteres");
        }
        Objects.requireNonNull(tipo, "tipo es requerido");
        Objects.requireNonNull(naturaleza, "naturaleza es requerida");

        // Invariante: el primer dígito del código DEBE coincidir con el tipo.
        // Defensa contra inconsistencia código↔tipo que rompería reportes
        // jerárquicos (ej. una cuenta "2.1.01" marcada como ACTIVO).
        TipoCuentaContable tipoEsperado = PREFIJO_A_TIPO.get(codigo.charAt(0));
        if (tipoEsperado != tipo) {
            throw new IllegalArgumentException(String.format(
                    "tipo %s no coincide con prefijo del código %s (esperado %s)",
                    tipo, codigo, tipoEsperado));
        }

        // Invariante: nivel debe coincidir con número de segmentos del código.
        int nivelInferido = inferirNivel(codigo);
        if (nivel != nivelInferido) {
            throw new IllegalArgumentException(String.format(
                    "nivel %d no coincide con el código %s (esperado %d segmentos)",
                    nivel, codigo, nivelInferido));
        }

        // Invariante: nivel 1 NO tiene padre, niveles > 1 DEBEN tenerlo.
        if (nivel == 1 && cuentaPadreId != null) {
            throw new IllegalArgumentException(
                    "una cuenta de nivel 1 (rubro raíz) no debe tener padre");
        }
        if (nivel > 1 && cuentaPadreId == null) {
            throw new IllegalArgumentException(
                    "una cuenta de nivel > 1 debe tener cuenta padre");
        }
    }

    /**
     * Cuenta los segmentos del código separados por ".".
     *
     * <p>Asume que el código ya pasó el regex check del patrón. Si no, puede
     * devolver valores raros — siempre llamar después de validar el patrón.</p>
     */
    private static int inferirNivel(String codigo) {
        if (codigo == null || codigo.isBlank()) return 0;
        // Contar puntos + 1. "1" → 1, "1.1" → 2, "1.1.01" → 3, etc.
        int puntos = 0;
        for (int i = 0; i < codigo.length(); i++) {
            if (codigo.charAt(i) == '.') puntos++;
        }
        return puntos + 1;
    }
}
