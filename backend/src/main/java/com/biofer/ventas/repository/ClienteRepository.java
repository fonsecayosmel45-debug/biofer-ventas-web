package com.biofer.ventas.repository;

import com.biofer.ventas.model.Cliente;
import com.biofer.ventas.model.TipoDocumento;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository
        extends JpaRepository<Cliente, Long> {

    boolean existsByTipoDocumentoAndNumeroDocumento(
        TipoDocumento tipoDocumento,
        String numeroDocumento
    );

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cliente c WHERE c.id = :id")
    Optional<Cliente> bloquear(@Param("id") Long id);

    String FILTRO = """
        WHERE (
            :texto = ''
            OR LOWER(COALESCE(c.nombres, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(c.apellidos, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(c.razonSocial, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(COALESCE(c.nombreComercial, ''))
                LIKE LOWER(CONCAT('%', :texto, '%'))
            OR c.numeroDocumento LIKE CONCAT('%', :texto, '%')
        )
        """;

    @Query(
        value = "SELECT c FROM Cliente c " + FILTRO,
        countQuery = "SELECT COUNT(c) FROM Cliente c " + FILTRO
    )
    Page<Cliente> buscar(
        @Param("texto") String texto,
        Pageable pageable
    );
}