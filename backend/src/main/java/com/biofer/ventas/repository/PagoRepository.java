package com.biofer.ventas.repository;
import com.biofer.ventas.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PagoRepository extends JpaRepository<Pago, Long> {
java.util.List<Pago> findByVendedorId(Long vendedorId);
}
