package com.biofer.ventas.repository;
import com.biofer.ventas.model.SaldoFavor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SaldoFavorRepository extends JpaRepository<SaldoFavor, Long> {
java.util.List<SaldoFavor> findByVendedorId(Long vendedorId);
}
