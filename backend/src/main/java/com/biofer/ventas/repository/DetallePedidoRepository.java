package com.biofer.ventas.repository;
import com.biofer.ventas.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
java.util.List<DetallePedido> findByPedidoIdOrderByIdAsc(Long pedidoId);
}
