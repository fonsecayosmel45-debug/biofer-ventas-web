package com.biofer.ventas.repository;
import com.biofer.ventas.model.ControlConcurrencia;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
public interface ControlConcurrenciaRepository extends JpaRepository<ControlConcurrencia,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ControlConcurrencia c where c.id=1")
    ControlConcurrencia bloquear();
}
