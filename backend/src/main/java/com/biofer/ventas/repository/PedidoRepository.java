package com.biofer.ventas.repository;
import com.biofer.ventas.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
java.util.List<Pedido> findByEstadoOrderByFechaAprobacionAscIdAsc(String estado);
java.util.List<Pedido> findByVendedorId(Long vendedorId);
}
