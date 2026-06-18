package com.tufondo.productos.application.usecase;

import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.infrastructure.persistence.entity.CuentaAhorroEntity;
import com.tufondo.ahorros.infrastructure.persistence.jpa.CuentaAhorroJpaRepository;
import com.tufondo.creditos.application.dto.CrearSolicitudCreditoRequest;
import com.tufondo.creditos.application.dto.SolicitudCreditoResponse;
import com.tufondo.creditos.application.usecase.CrearSolicitudCreditoUseCase;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import com.tufondo.productos.application.dto.PrecalificacionProductoResponse;
import com.tufondo.productos.application.dto.ProductoFinanciableRequest;
import com.tufondo.productos.application.dto.ProductoFinanciableResponse;
import com.tufondo.productos.infrastructure.persistence.entity.ProductoFinanciableEntity;
import com.tufondo.productos.infrastructure.persistence.jpa.ProductoFinanciableJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GestionarProductosFinanciablesUseCase {

    private static final String ESTADO_BORRADOR = "BORRADOR";
    private static final String ESTADO_PUBLICADO = "PUBLICADO";
    private static final String ESTADO_PAUSADO = "PAUSADO";
    private static final String ESTADO_ARCHIVADO = "ARCHIVADO";

    private final ProductoFinanciableJpaRepository repository;
    private final CuentaAhorroJpaRepository cuentaRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final CrearSolicitudCreditoUseCase crearSolicitudCreditoUseCase;

    @Transactional(readOnly = true)
    public List<ProductoFinanciableResponse> listarPublicados() {
        return repository.findByEstadoOrderByUpdatedAtDesc(ESTADO_PUBLICADO).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoFinanciableResponse> listarAdmin() {
        return repository.findAllByOrderByUpdatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public void validarExiste(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductoNoEncontradoException(id.toString());
        }
    }

    @Transactional(readOnly = true)
    public ProductoFinanciableResponse obtenerPublicado(String slug) {
        return repository.findBySlugAndEstado(slug, ESTADO_PUBLICADO)
            .map(this::toResponse)
            .orElseThrow(() -> new ProductoNoEncontradoException(slug));
    }

    @Transactional
    public ProductoFinanciableResponse crear(ProductoFinanciableRequest request, UUID adminId) {
        TipoCredito tipoCredito = validarRequest(request);
        String codigo = request.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByCodigoIgnoreCase(codigo)) {
            throw new IllegalArgumentException("Ya existe un producto con ese codigo");
        }

        ProductoFinanciableEntity entity = new ProductoFinanciableEntity();
        entity.setCodigo(codigo);
        entity.setSlug(generarSlugUnico(request.getNombre()));
        aplicarRequest(entity, request, tipoCredito);
        entity.setEstado(ESTADO_BORRADOR);
        entity.setCreatedBy(adminId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ProductoFinanciableResponse actualizar(Long id, ProductoFinanciableRequest request) {
        TipoCredito tipoCredito = validarRequest(request);
        ProductoFinanciableEntity entity = repository.findById(id)
            .orElseThrow(() -> new ProductoNoEncontradoException(id.toString()));
        aplicarRequest(entity, request, tipoCredito);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ProductoFinanciableResponse cambiarEstado(Long id, String estado) {
        if (!List.of(ESTADO_BORRADOR, ESTADO_PUBLICADO, ESTADO_PAUSADO, ESTADO_ARCHIVADO).contains(estado)) {
            throw new IllegalArgumentException("Estado no permitido");
        }
        ProductoFinanciableEntity entity = repository.findById(id)
            .orElseThrow(() -> new ProductoNoEncontradoException(id.toString()));
        entity.setEstado(estado);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ProductoFinanciableResponse actualizarImagen(Long id, String imagenUrl) {
        ProductoFinanciableEntity entity = repository.findById(id)
            .orElseThrow(() -> new ProductoNoEncontradoException(id.toString()));
        entity.setImagenUrl(imagenUrl);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public PrecalificacionProductoResponse precalificar(Long productoId, UUID socioId) {
        ProductoFinanciableEntity producto = repository.findById(productoId)
            .filter(p -> ESTADO_PUBLICADO.equals(p.getEstado()))
            .orElseThrow(() -> new ProductoNoEncontradoException(productoId.toString()));
        return calcularPrecalificacion(producto, socioId);
    }

    @Transactional
    public SolicitudCreditoResponse solicitarFinanciamiento(Long productoId, UUID socioId) {
        ProductoFinanciableEntity producto = repository.findById(productoId)
            .filter(p -> ESTADO_PUBLICADO.equals(p.getEstado()))
            .orElseThrow(() -> new ProductoNoEncontradoException(productoId.toString()));
        PrecalificacionProductoResponse precalificacion = calcularPrecalificacion(producto, socioId);
        if (!precalificacion.isElegible()) {
            throw new IllegalArgumentException(precalificacion.getMensaje());
        }
        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(producto.getTipoCreditoId())
            .orElseThrow(() -> new IllegalArgumentException("Tipo de credito asociado no existe"));

        CrearSolicitudCreditoRequest request = new CrearSolicitudCreditoRequest();
        request.setTipoCreditoId(producto.getTipoCreditoId());
        request.setMontoSolicitado(producto.getPrecio());
        request.setPlazoMeses(calcularPlazoSolicitud(producto, tipoCredito));
        request.setDestinoCredito("Financiamiento de producto: " + producto.getNombre());
        request.setColateralCuentaId(precalificacion.getCuentaColateralId().toString());
        request.setProductoFinanciableId(producto.getId());
        request.setProductoNombreSnapshot(producto.getNombre());
        request.setProductoPrecioSnapshot(producto.getPrecio());
        request.setProductoMonedaSnapshot(producto.getMoneda());
        request.setProductoColateralRequeridoSnapshot(precalificacion.getColateralRequerido());
        return crearSolicitudCreditoUseCase.ejecutar(request, socioId);
    }

    private PrecalificacionProductoResponse calcularPrecalificacion(ProductoFinanciableEntity producto, UUID socioId) {
        BigDecimal colateral = calcularColateral(producto);
        List<CuentaAhorroEntity> cuentas = cuentaRepository.findBySocioId(socioId, PageRequest.of(0, 50)).getContent()
            .stream()
            .filter(c -> EstadoCuenta.ACTIVA.equals(c.getEstado()))
            .toList();
        BigDecimal saldoDisponible = cuentas.stream()
            .map(CuentaAhorroEntity::getSaldoDisponible)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        CuentaAhorroEntity cuentaElegible = cuentas.stream()
            .filter(c -> c.getSaldoDisponible().compareTo(colateral) >= 0)
            .max(Comparator.comparing(CuentaAhorroEntity::getSaldoDisponible))
            .orElse(null);

        PrecalificacionProductoResponse response = new PrecalificacionProductoResponse();
        response.setProductoId(producto.getId());
        response.setSaldoDisponible(saldoDisponible);
        response.setColateralRequerido(colateral);
        response.setElegible(cuentaElegible != null);
        response.setCuentaColateralId(cuentaElegible != null ? cuentaElegible.getId() : null);
        response.setMontoFaltante(cuentaElegible != null ? BigDecimal.ZERO : colateral.subtract(saldoDisponible).max(BigDecimal.ZERO));
        response.setMensaje(cuentaElegible != null
            ? "El socio precalifica para solicitar este financiamiento."
            : "El saldo disponible no cubre el colateral requerido.");
        return response;
    }

    private void aplicarRequest(ProductoFinanciableEntity entity, ProductoFinanciableRequest request, TipoCredito tipoCredito) {
        entity.setNombre(request.getNombre().trim());
        entity.setDescripcion(request.getDescripcion());
        entity.setCategoria(request.getCategoria().trim().toUpperCase(Locale.ROOT));
        entity.setProveedor(request.getProveedor());
        entity.setPrecio(request.getPrecio());
        entity.setMoneda(request.getMoneda().trim().toUpperCase(Locale.ROOT));
        entity.setImagenUrl(request.getImagenUrl());
        entity.setTipoCreditoId(request.getTipoCreditoId());
        entity.setPlazoMinimoMeses(request.getPlazoMinimoMeses());
        entity.setPlazoMaximoMeses(calcularPlazoMaximoPermitido(request.getPlazoMaximoMeses(), tipoCredito));
        entity.setPorcentajeColateral(request.getPorcentajeColateral());
        entity.setRequiereAprobacionManual(Boolean.TRUE.equals(request.getRequiereAprobacionManual()));
    }

    private TipoCredito validarRequest(ProductoFinanciableRequest request) {
        if (request.getPlazoMaximoMeses() < request.getPlazoMinimoMeses()) {
            throw new IllegalArgumentException("El plazo maximo debe ser mayor o igual al plazo minimo");
        }
        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(request.getTipoCreditoId())
            .orElseThrow(() -> new IllegalArgumentException("Tipo de credito asociado no existe"));
        Integer tipoPlazoMinimo = tipoCredito.getPlazoMinimoMeses();
        Integer tipoPlazoMaximo = tipoCredito.getPlazoMaximoMeses();
        if (tipoPlazoMinimo != null && request.getPlazoMinimoMeses() < tipoPlazoMinimo) {
            throw new IllegalArgumentException("El plazo minimo del producto no puede ser menor al tipo de credito");
        }
        if (tipoPlazoMaximo != null && request.getPlazoMaximoMeses() > tipoPlazoMaximo) {
            throw new IllegalArgumentException("El plazo maximo del producto no puede superar el tipo de credito");
        }
        return tipoCredito;
    }

    private Integer calcularPlazoSolicitud(ProductoFinanciableEntity producto, TipoCredito tipoCredito) {
        Integer plazo = calcularPlazoMaximoPermitido(producto.getPlazoMaximoMeses(), tipoCredito);
        if (tipoCredito.getPlazoMinimoMeses() != null && plazo < tipoCredito.getPlazoMinimoMeses()) {
            plazo = tipoCredito.getPlazoMinimoMeses();
        }
        if (plazo < producto.getPlazoMinimoMeses()) {
            throw new IllegalArgumentException("El producto tiene una configuracion de plazo incompatible con su tipo de credito");
        }
        return plazo;
    }

    private Integer calcularPlazoMaximoPermitido(Integer plazoProducto, TipoCredito tipoCredito) {
        if (tipoCredito.getPlazoMaximoMeses() == null) {
            return plazoProducto;
        }
        return Math.min(plazoProducto, tipoCredito.getPlazoMaximoMeses());
    }

    private ProductoFinanciableResponse toResponse(ProductoFinanciableEntity entity) {
        ProductoFinanciableResponse response = new ProductoFinanciableResponse();
        response.setId(entity.getId());
        response.setCodigo(entity.getCodigo());
        response.setSlug(entity.getSlug());
        response.setNombre(entity.getNombre());
        response.setDescripcion(entity.getDescripcion());
        response.setCategoria(entity.getCategoria());
        response.setProveedor(entity.getProveedor());
        response.setPrecio(entity.getPrecio());
        response.setMoneda(entity.getMoneda());
        response.setImagenUrl(entity.getImagenUrl());
        response.setTipoCreditoId(entity.getTipoCreditoId());
        response.setPlazoMinimoMeses(entity.getPlazoMinimoMeses());
        response.setPlazoMaximoMeses(entity.getPlazoMaximoMeses());
        response.setPorcentajeColateral(entity.getPorcentajeColateral());
        response.setColateralRequerido(calcularColateral(entity));
        response.setRequiereAprobacionManual(entity.getRequiereAprobacionManual());
        response.setEstado(entity.getEstado());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private BigDecimal calcularColateral(ProductoFinanciableEntity producto) {
        return producto.getPrecio()
            .multiply(producto.getPorcentajeColateral())
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    private String generarSlugUnico(String nombre) {
        String base = Normalizer.normalize(nombre, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "producto";
        }
        String slug = base;
        int i = 2;
        while (repository.existsBySlugIgnoreCase(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    public static class ProductoNoEncontradoException extends RuntimeException {
        public ProductoNoEncontradoException(String id) {
            super("Producto financiable no encontrado: " + id);
        }
    }
}
