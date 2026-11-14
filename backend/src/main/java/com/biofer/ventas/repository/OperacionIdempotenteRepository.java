package com.biofer.ventas.repository;
import com.biofer.ventas.model.OperacionIdempotente;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OperacionIdempotenteRepository extends JpaRepository<OperacionIdempotente, Long> {
java.util.Optional<OperacionIdempotente> findByClave(String clave);
}
