package com.biofer.ventas.repository;
import com.biofer.ventas.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
java.util.List<Despacho> findByPedidoIdOrderByIdAsc(Long pedidoId);
}
