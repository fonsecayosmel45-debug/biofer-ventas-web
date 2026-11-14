package com.biofer.ventas.repository;
import com.biofer.ventas.model.CreditoVendedor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CreditoVendedorRepository extends JpaRepository<CreditoVendedor, Long> {
java.util.Optional<CreditoVendedor> findByVendedorIdAndMoneda(Long vendedorId,String moneda);
}
