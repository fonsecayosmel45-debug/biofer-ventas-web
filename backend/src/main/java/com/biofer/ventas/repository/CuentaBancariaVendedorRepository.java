package com.biofer.ventas.repository;

import com.biofer.ventas.model.CuentaBancariaVendedor;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CuentaBancariaVendedorRepository
        extends JpaRepository<CuentaBancariaVendedor, Long> {

    @Query("""
        SELECT c
        FROM CuentaBancariaVendedor c
        WHERE c.vendedor.id = :vendedorId
        ORDER BY c.id
        """)
    List<CuentaBancariaVendedor> listarPorVendedor(
        @Param("vendedorId") Long vendedorId
    );
}