package com.biofer.ventas.repository;
import com.biofer.ventas.model.GuiaRemision;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GuiaRemisionRepository extends JpaRepository<GuiaRemision, Long> {
boolean existsByDespachoId(Long despachoId);
boolean existsBySerieOficialAndNumeroOficial(String serie,String numero);
}
