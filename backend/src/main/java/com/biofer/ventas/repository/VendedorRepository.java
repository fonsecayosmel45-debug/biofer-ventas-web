package com.biofer.ventas.repository;

import com.biofer.ventas.model.TipoDocumento;
import com.biofer.ventas.model.Vendedor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VendedorRepository
        extends JpaRepository<Vendedor, Long> {

    boolean existsByTipoDocumentoAndNumeroDocumento(
        TipoDocumento tipoDocumento,
        String numeroDocumento
    );

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        Long id
    );

    String FILTRO = """
        WHERE (
            :texto = ''
            OR LOWER(COALESCE(v.nombres, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(v.apellidos, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(v.razonSocial, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(v.nombreComercial, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR v.numeroDocumento LIKE CONCAT('%', :texto, '%')
        )
        """;

    @Query(
        value = "SELECT v FROM Vendedor v " + FILTRO,
        countQuery = "SELECT COUNT(v) FROM Vendedor v " + FILTRO
    )
    Page<Vendedor> buscar(
        @Param("texto") String texto,
        Pageable pageable
    );
}