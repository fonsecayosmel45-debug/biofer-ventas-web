package com.biofer.ventas.repository;
import com.biofer.ventas.model.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
java.util.List<Devolucion> findByFacturaId(Long facturaId);
}
