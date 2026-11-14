package com.biofer.ventas.repository;

import com.biofer.ventas.model.Presentacion;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PresentacionRepository
        extends JpaRepository<Presentacion, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByProducto_IdAndStockReservadoGreaterThan(Long productoId,int stock);

    boolean existsByCodigoIgnoreCaseAndIdNot(
        String codigo,
        Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Presentacion p WHERE p.id = :id")
    Optional<Presentacion> buscarParaActualizar(
        @Param("id") Long id
    );
}
