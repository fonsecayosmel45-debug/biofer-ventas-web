package com.biofer.ventas.repository;
import com.biofer.ventas.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FacturaRepository extends JpaRepository<Factura, Long> {
java.util.List<Factura> findByPedidoIdOrderByIdAsc(Long pedidoId);
java.util.List<Factura> findByVendedorId(Long vendedorId);
boolean existsBySerieOficialAndNumeroOficial(String serie,String numero);
}
