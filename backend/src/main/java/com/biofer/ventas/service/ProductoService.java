package com.biofer.ventas.service;

import com.biofer.ventas.dto.CategoriaResponse;
import com.biofer.ventas.dto.ProductoRequest;
import com.biofer.ventas.dto.ProductoResponse;

import com.biofer.ventas.model.Categoria;
import com.biofer.ventas.model.Producto;

import com.biofer.ventas.repository.CategoriaRepository;
import com.biofer.ventas.repository.ProductoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Page<ProductoResponse> listar(
            String busqueda,
            Pageable pageable) {

        String texto = busqueda == null ? "" : busqueda.strip();

        if (texto.isEmpty()) {
            return productoRepository.findAll(pageable)
                    .map(this::convertir);
        }

        return productoRepository
                .findByNombreComercialContainingIgnoreCaseOrNombreProductoContainingIgnoreCase(
                    texto,
                    texto,
                    pageable
                )
                .map(this::convertir);
    }

    public ProductoResponse buscarPorId(Long id) {
        return convertir(obtener(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        asignarDatos(producto, request);

        return convertir(
            productoRepository.saveAndFlush(producto)
        );
    }

    @Transactional
    public ProductoResponse actualizar(
            Long id,
            ProductoRequest request) {

        Producto producto = obtener(id);
        asignarDatos(producto, request);

        return convertir(
            productoRepository.saveAndFlush(producto)
        );
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = obtener(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
                ));
    }

    private void asignarDatos(
            Producto producto,
            ProductoRequest request) {

        Set<Categoria> categorias =
                obtenerCategorias(request.categoriasIds());

        producto.setNombreComercial(
            request.nombreComercial().strip()
        );

        producto.setNombreProducto(
            request.nombreProducto().strip()
        );

        producto.setProcedencia(request.procedencia());

        producto.setDescripcion(
            request.descripcion() == null
                ? null
                : request.descripcion().strip()
        );

        producto.setActivo(request.activo());
        producto.setCategorias(categorias);
    }

    private Set<Categoria> obtenerCategorias(Set<Long> ids) {
        List<Categoria> categorias =
                categoriaRepository.findAllById(ids);

        if (categorias.size() != ids.size()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Una o más categorías no existen"
            );
        }

        boolean hayInactivas = categorias.stream()
                .anyMatch(categoria -> !categoria.isActiva());

        if (hayInactivas) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se pueden asignar categorías inactivas"
            );
        }

        return new HashSet<>(categorias);
    }

    private ProductoResponse convertir(Producto producto) {
        List<CategoriaResponse> categorias =
                producto.getCategorias().stream()
                    .sorted(Comparator.comparing(Categoria::getNombre))
                    .map(categoria -> new CategoriaResponse(
                        categoria.getId(),
                        categoria.getNombre(),
                        categoria.getPadre() == null
                            ? null
                            : categoria.getPadre().getId(),
                        categoria.isActiva()
                    ))
                    .toList();

        return new ProductoResponse(
            producto.getId(),
            producto.getNombreComercial(),
            producto.getNombreProducto(),
            producto.getProcedencia(),
            producto.getDescripcion(),
            producto.isActivo(),
            categorias,
            producto.getFechaCreacion(),
            producto.getFechaActualizacion()
        );
    }
}