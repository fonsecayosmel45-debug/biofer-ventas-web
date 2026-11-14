package com.biofer.ventas.repository;

import com.biofer.ventas.model.MovimientoInventario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    @Query(
        value = """
            SELECT m
            FROM MovimientoInventario m
            WHERE m.presentacion.id = :presentacionId
            """,
        countQuery = """
            SELECT COUNT(m)
            FROM MovimientoInventario m
            WHERE m.presentacion.id = :presentacionId
            """
    )
    Page<MovimientoInventario> findByPresentacionId(
        @Param("presentacionId") Long presentacionId,
        Pageable pageable
    );
}