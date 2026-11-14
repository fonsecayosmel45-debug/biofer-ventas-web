package com.biofer.ventas.repository;
import com.biofer.ventas.model.AplicacionSaldo;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AplicacionSaldoRepository extends JpaRepository<AplicacionSaldo, Long> {
java.util.List<AplicacionSaldo> findByFacturaId(Long facturaId);
}
