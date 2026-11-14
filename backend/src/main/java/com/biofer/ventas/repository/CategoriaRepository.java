package com.biofer.ventas.repository;

import com.biofer.ventas.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(
        String nombre,
        Long id
    );

    boolean existsByPadre_IdAndActivaTrue(Long padreId);
}