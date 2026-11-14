package com.biofer.ventas.repository;

import com.biofer.ventas.model.HistorialAsignacionCliente;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialAsignacionClienteRepository
        extends JpaRepository<HistorialAsignacionCliente, Long> {

    @Query("""
        SELECT h
        FROM HistorialAsignacionCliente h
        WHERE h.cliente.id = :clienteId
        ORDER BY h.id DESC
        """)
    List<HistorialAsignacionCliente> listarPorCliente(
        @Param("clienteId") Long clienteId
    );
}