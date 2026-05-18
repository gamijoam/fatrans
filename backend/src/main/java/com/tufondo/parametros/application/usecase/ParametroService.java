package com.tufondo.parametros.application.usecase;

import com.tufondo.parametros.application.dto.ActualizarParametroRequest;
import com.tufondo.parametros.application.dto.ParametroResponse;
import com.tufondo.parametros.domain.model.ParametroSistema;
import com.tufondo.parametros.domain.repository.ParametroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ParametroService {

    private final ParametroRepository repository;

    public ParametroService(ParametroRepository repository) {
        this.repository = repository;
    }

    public List<ParametroResponse> listarTodos() {
        return repository.listarTodos().stream()
                .map(ParametroResponse::fromDomain)
                .toList();
    }

    public List<ParametroResponse> listarPorCategoria(String categoria) {
        return repository.buscarPorCategoria(categoria).stream()
                .map(ParametroResponse::fromDomain)
                .toList();
    }

    public ParametroResponse buscarPorKey(String key) {
        return repository.buscarPorKey(key)
                .map(ParametroResponse::fromDomain)
                .orElseThrow(() -> new ParametroNoEncontradoException(key));
    }

    @Transactional
    public ParametroResponse actualizar(String key, ActualizarParametroRequest request, UUID usuarioId) {
        ParametroSistema parametro = repository.buscarPorKey(key)
                .orElseThrow(() -> new ParametroNoEncontradoException(key));

        if (!parametro.editable()) {
            throw new ParametroNoEditableException(key);
        }

        validarValor(parametro, request.valor());

        ParametroSistema actualizado = parametro.conValor(request.valor(), usuarioId);
        repository.actualizar(actualizado);

        return ParametroResponse.fromDomain(actualizado);
    }

    private void validarValor(ParametroSistema parametro, String valor) {
        switch (parametro.tipo()) {
            case NUMERIC, PERCENTAGE, CURRENCY -> {
                try {
                    new BigDecimal(valor);
                } catch (NumberFormatException e) {
                    throw new ValorInvalidoException(parametro.tipo().name(), valor);
                }
            }
            case BOOLEAN -> {
                if (!valor.equalsIgnoreCase("true") && !valor.equalsIgnoreCase("false")) {
                    throw new ValorInvalidoException("BOOLEAN", valor);
                }
            }
            case STRING, DATE -> {
                if (valor == null || valor.isBlank()) {
                    throw new ValorInvalidoException(parametro.tipo().name(), valor);
                }
            }
        }

        if (parametro.tipo() == ParametroSistema.TipoParametro.PERCENTAGE) {
            BigDecimal num = new BigDecimal(valor);
            if (num.compareTo(BigDecimal.ZERO) < 0 || num.compareTo(BigDecimal.ONE) > 0) {
                throw new ValorInvalidoException("PERCENTAGE", "debe estar entre 0 y 1");
            }
        }
    }

    public static class ParametroNoEncontradoException extends RuntimeException {
        public ParametroNoEncontradoException(String key) {
            super("Parámetro no encontrado: " + key);
        }
    }

    public static class ParametroNoEditableException extends RuntimeException {
        public ParametroNoEditableException(String key) {
            super("El parámetro no es editable: " + key);
        }
    }

    public static class ValorInvalidoException extends RuntimeException {
        public ValorInvalidoException(String tipo, String valor) {
            super("Valor inválido para tipo " + tipo + ": " + valor);
        }
    }
}