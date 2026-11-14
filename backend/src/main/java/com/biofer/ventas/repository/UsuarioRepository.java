package com.biofer.ventas.repository;
import com.biofer.ventas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
java.util.Optional<Usuario> findByNombre(String nombre);
}
