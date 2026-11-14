package com.biofer.ventas.repository;

import com.biofer.ventas.model.DireccionCliente;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DireccionClienteRepository
        extends JpaRepository<DireccionCliente, Long> {

    @Query("""
        SELECT d
        FROM DireccionCliente d
        WHERE d.cliente.id = :clienteId
        ORDER BY d.id
        """)
    List<DireccionCliente> listarPorCliente(
        @Param("clienteId") Long clienteId
    );
}