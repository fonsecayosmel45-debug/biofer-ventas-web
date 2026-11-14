package com.biofer.ventas.service;

import com.biofer.ventas.dto.*;
import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Currency;

@Service
@Transactional(readOnly = true)
public class PresentacionService {

    private final PresentacionRepository presentaciones;
    private final ProductoRepository productos;
    private final HistorialPrecioRepository precios;
    private final MovimientoInventarioRepository movimientos;

    public PresentacionService(
            PresentacionRepository presentaciones,
            ProductoRepository productos,
            HistorialPrecioRepository precios,
            MovimientoInventarioRepository movimientos) {

        this.presentaciones = presentaciones;
        this.productos = productos;
        this.precios = precios;
        this.movimientos = movimientos;
    }

    public Page<PresentacionResponse> listar(Pageable pageable) {
        return presentaciones.findAll(pageable)
                .map(this::convertir);
    }

    public PresentacionResponse buscarPorId(Long id) {
        return convertir(obtener(id));
    }

    @Transactional
    public PresentacionResponse crear(
            PresentacionRequest request) {

        validar(request);

        if (presentaciones.existsByCodigoIgnoreCase(
                request.codigo().strip())) {
            throw conflicto("El código ya está registrado");
        }

        Producto producto = obtenerProducto(request.productoId());

        Presentacion presentacion = new Presentacion();
        presentacion.setProducto(producto);
        presentacion.setCapacidad(request.capacidad());
        presentacion.setUnidad(request.unidad());
        presentacion.setOtraUnidad(request.otraUnidad());

        asignarDatos(presentacion, request);
        presentaciones.saveAndFlush(presentacion);

        precios.save(new HistorialPrecio(
            presentacion,
            null,
            presentacion.getPrecioSinIgv(),
            null,
            presentacion.getMoneda(),
            "Registro inicial"
        ));

        return convertir(presentacion);
    }

    @Transactional
    public PresentacionResponse actualizar(
            Long id,
            PresentacionRequest request) {

        validar(request);

        Presentacion presentacion = bloquear(id);

        if (presentaciones.existsByCodigoIgnoreCaseAndIdNot(
                request.codigo().strip(), id)) {
            throw conflicto("El código pertenece a otra presentación");
        }

        boolean mismaIdentidad =
                presentacion.getProducto().getId()
                    .equals(request.productoId())
                && presentacion.getCapacidad()
                    .compareTo(request.capacidad()) == 0
                && presentacion.getUnidad() == request.unidad()
                && java.util.Objects.equals(
                    presentacion.getOtraUnidad(),
                    request.otraUnidad()
                );

        if (!mismaIdentidad) {
            throw conflicto(
                "Para otro producto, capacidad o unidad, cree otra presentación"
            );
        }

        if (!request.activa()
                && presentacion.getStockReservado() > 0) {
            throw conflicto(
                "No se puede desactivar una presentación con reservas"
            );
        }

        BigDecimal precioAnterior =
                presentacion.getPrecioSinIgv();

        String monedaAnterior = presentacion.getMoneda();

        boolean cambioPrecio =
                precioAnterior.compareTo(request.precioSinIgv()) != 0
                || !monedaAnterior.equals(request.moneda());

        if (cambioPrecio
                && (request.motivoCambioPrecio() == null
                || request.motivoCambioPrecio().isBlank())) {
            throw conflicto(
                "Indique el motivo del cambio de precio o moneda"
            );
        }

        asignarDatos(presentacion, request);
        presentaciones.saveAndFlush(presentacion);

        if (cambioPrecio) {
            precios.save(new HistorialPrecio(
                presentacion,
                precioAnterior,
                presentacion.getPrecioSinIgv(),
                monedaAnterior,
                presentacion.getMoneda(),
                request.motivoCambioPrecio().strip()
            ));
        }

        return convertir(presentacion);
    }

    @Transactional
    public void desactivar(Long id) {
        Presentacion presentacion = bloquear(id);

        if (presentacion.getStockReservado() > 0) {
            throw conflicto("La presentación tiene reservas");
        }

        presentacion.setActiva(false);
        presentaciones.save(presentacion);
    }

    @Transactional
    public PresentacionResponse registrarMovimiento(
            Long id,
            MovimientoInventarioRequest request) {

        Presentacion presentacion = bloquear(id);

        if (!presentacion.isActiva()
                || !presentacion.getProducto().isActivo()) {
            throw conflicto("La presentación o el producto están inactivos");
        }

        if (request.tipo() == TipoMovimiento.SALIDA_VENTA) {
            throw conflicto(
                "Las salidas de venta se registrarán desde el despacho"
            );
        }

        int cambio = switch (request.tipo()) {
            case ENTRADA, AJUSTE_POSITIVO -> request.cantidad();
            case AJUSTE_NEGATIVO -> -request.cantidad();
            default -> throw conflicto("Tipo de movimiento no permitido");
        };

        int anterior = presentacion.getStockActual();
        int posterior;

        try {
            posterior = Math.addExact(anterior, cambio);
        } catch (ArithmeticException exception) {
            throw conflicto("La cantidad supera el límite permitido");
        }

        if (posterior < 0) {
            throw conflicto("El movimiento dejaría stock negativo");
        }

        if (posterior < presentacion.getStockReservado()) {
            throw conflicto("El movimiento afectaría stock reservado");
        }

        presentacion.setStockActual(posterior);

        movimientos.save(new MovimientoInventario(
            presentacion,
            request.tipo(),
            cambio,
            anterior,
            posterior,
            request.motivo().strip(),
            request.referencia()
        ));

        presentaciones.saveAndFlush(presentacion);

        return convertir(presentacion);
    }

    public Page<HistorialPrecio> historialPrecios(
            Long id,
            Pageable pageable) {

        obtener(id);
        return precios.findByPresentacionId(id, pageable);
    }

    public Page<MovimientoInventario> kardex(
            Long id,
            Pageable pageable) {

        obtener(id);
        return movimientos.findByPresentacionId(id, pageable);
    }

    private void validar(PresentacionRequest request) {
        if (request.stockAlto() < request.stockMinimo()) {
            throw conflicto(
                "El stock alto debe ser mayor o igual al mínimo"
            );
        }

        if (request.unidad() == UnidadMedida.OTRA
                && (request.otraUnidad() == null
                || request.otraUnidad().isBlank())) {
            throw conflicto("Describa la otra unidad");
        }

        if (request.unidad() != UnidadMedida.OTRA
                && request.otraUnidad() != null
                && !request.otraUnidad().isBlank()) {
            throw conflicto(
                "otraUnidad solo se utiliza cuando unidad es OTRA"
            );
        }

        try {
            Currency.getInstance(request.moneda());
        } catch (IllegalArgumentException exception) {
            throw conflicto("Código de moneda no reconocido");
        }
    }

    private Producto obtenerProducto(Long id) {
        Producto producto = productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
                ));

        if (!producto.isActivo()) {
            throw conflicto("El producto está inactivo");
        }

        return producto;
    }

    private Presentacion obtener(Long id) {
        return presentaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Presentación no encontrada"
                ));
    }

    private Presentacion bloquear(Long id) {
        return presentaciones.buscarParaActualizar(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Presentación no encontrada"
                ));
    }

    private void asignarDatos(
            Presentacion presentacion,
            PresentacionRequest request) {

        presentacion.setCodigo(request.codigo().strip());
        presentacion.setPrecioSinIgv(request.precioSinIgv());
        presentacion.setMoneda(request.moneda());
        presentacion.setStockMinimo(request.stockMinimo());
        presentacion.setStockAlto(request.stockAlto());
        presentacion.setActiva(request.activa());
    }

    private PresentacionResponse convertir(Presentacion p) {
        String estadoStock;

        if (p.getStockActual() == 0) {
            estadoStock = "SIN_STOCK";
        } else if (p.getStockActual() < p.getStockMinimo()) {
            estadoStock = "BAJO";
        } else if (p.getStockActual() > p.getStockAlto()) {
            estadoStock = "ALTO";
        } else {
            estadoStock = "ESTABLE";
        }

        return new PresentacionResponse(
            p.getId(),
            p.getProducto().getId(),
            p.getProducto().getNombreComercial(),
            p.getCodigo(),
            p.getCapacidad(),
            p.getUnidad(),
            p.getOtraUnidad(),
            p.getPrecioSinIgv(),
            p.getMoneda(),
            p.getStockActual(),
            p.getStockReservado(),
            p.getStockActual() - p.getStockReservado(),
            p.getStockMinimo(),
            p.getStockAlto(),
            estadoStock,
            p.isActiva(),
            p.getFechaCreacion(),
            p.getFechaActualizacion()
        );
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(
            HttpStatus.CONFLICT,
            mensaje
        );
    }
}