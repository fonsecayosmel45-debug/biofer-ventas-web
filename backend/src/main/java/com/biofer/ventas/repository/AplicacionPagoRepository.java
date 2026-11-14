package com.biofer.ventas.repository;
import com.biofer.ventas.model.AplicacionPago;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AplicacionPagoRepository extends JpaRepository<AplicacionPago, Long> {
java.util.List<AplicacionPago> findByPagoId(Long pagoId);
java.util.List<AplicacionPago> findByFacturaId(Long facturaId);
}
