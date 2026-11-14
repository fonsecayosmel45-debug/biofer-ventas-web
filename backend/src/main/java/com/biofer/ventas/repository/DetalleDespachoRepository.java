package com.biofer.ventas.repository;
import com.biofer.ventas.model.DetalleDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DetalleDespachoRepository extends JpaRepository<DetalleDespacho, Long> {
java.util.List<DetalleDespacho> findByDespachoIdOrderByIdAsc(Long despachoId);
}
