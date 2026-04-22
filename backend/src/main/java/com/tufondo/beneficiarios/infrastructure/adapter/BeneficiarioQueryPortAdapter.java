// Adaptador para el puerto BeneficiarioQueryPort usado por el módulo Documentos PDF
package com.tufondo.beneficiarios.infrastructure.adapter;

import com.tufondo.core.port.BeneficiarioQueryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador para el puerto BeneficiarioQueryPort.
 *
 * ⚠️ MÓDULO NO IMPLEMENTADO: Este adaptador es un PLACEHOLDER.
 * El módulo Beneficiarios aún no tiene entidades JPA implementadas.
 *
 * Cuando se intenta generar una Carta de Beneficiarios, este adaptador
 * LANZARÁ una excepción OperacionNoDisponibleException.
 *
 * Para implementar correctamente:
 * 1. Crear entidades BeneficiarioEntity en el módulo Beneficiarios
 * 2. Crear BeneficiarioJpaRepository
 * 3. Actualizar este adaptador para usar el repositorio real
 */
@Slf4j
@Service
public class BeneficiarioQueryPortAdapter implements BeneficiarioQueryPort {

    /**
     * Obtiene los beneficiarios activos de un socio.
     *
     * ⚠️ IMPORTANTE: Este método NO está implementado.
     * Llamar a este método resultará en una excepción.
     *
     * @throws OperacionNoDisponibleException siempre, indicando que el módulo no está listo
     */
    @Override
    public List<Map<String, Object>> obtenerBeneficiariosActivos(UUID socioId) {
        log.error("INTENTO DE USAR MODULO BENEFICIARIOS NO IMPLEMENTADO");
        log.error("SocioId={}", socioId);
        log.error("El módulo Beneficiarios aún no tiene entidades implementadas");
        log.error("No se puede generar Carta de Beneficiarios sin este módulo");

        throw new OperacionNoDisponibleException(
                "Módulo Beneficiarios no implementado. " +
                "No es posible generar la Carta de Beneficiarios. " +
                "El módulo Beneficiarios debe estar completo para usar esta funcionalidad.");
    }

    /**
     * Excepción para operaciones no disponibles.
     */
    public static class OperacionNoDisponibleException extends RuntimeException {
        public OperacionNoDisponibleException(String mensaje) {
            super(mensaje);
        }
    }
}
