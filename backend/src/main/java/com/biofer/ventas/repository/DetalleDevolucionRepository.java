package com.biofer.ventas.repository;
import com.biofer.ventas.model.DetalleDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DetalleDevolucionRepository extends JpaRepository<DetalleDevolucion, Long> {
java.util.List<DetalleDevolucion> findByDevolucionId(Long devolucionId);
}
