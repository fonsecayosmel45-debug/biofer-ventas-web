package com.biofer.ventas.repository;
import com.biofer.ventas.model.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {
java.util.List<DetalleFactura> findByFacturaIdOrderByIdAsc(Long facturaId);
java.util.List<DetalleFactura> findByDetallePedidoId(Long detallePedidoId);
}
