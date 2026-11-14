package com.biofer.ventas.repository;
import com.biofer.ventas.model.FondoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FondoPedidoRepository extends JpaRepository<FondoPedido, Long> {
java.util.List<FondoPedido> findByPedidoIdAndMoneda(Long pedidoId,String moneda);
java.util.List<FondoPedido> findByPagoId(Long pagoId);
}
