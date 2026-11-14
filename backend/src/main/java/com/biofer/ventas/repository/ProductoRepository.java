package com.biofer.ventas.repository;

import com.biofer.ventas.model.Producto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    Page<Producto>
        findByNombreComercialContainingIgnoreCaseOrNombreProductoContainingIgnoreCase(
            String nombreComercial,
            String nombreProducto,
            Pageable pageable
        );
}