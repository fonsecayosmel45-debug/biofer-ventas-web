package com.biofer.ventas.repository;

import com.biofer.ventas.model.HistorialPrecio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistorialPrecioRepository
        extends JpaRepository<HistorialPrecio, Long> {

    @Query(
        value = """
            SELECT h
            FROM HistorialPrecio h
            WHERE h.presentacion.id = :presentacionId
            """,
        countQuery = """
            SELECT COUNT(h)
            FROM HistorialPrecio h
            WHERE h.presentacion.id = :presentacionId
            """
    )
    Page<HistorialPrecio> findByPresentacionId(
        @Param("presentacionId") Long presentacionId,
        Pageable pageable
    );
}