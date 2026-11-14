package com.biofer.ventas.service;

import com.biofer.ventas.dto.CategoriaRequest;
import com.biofer.ventas.dto.CategoriaResponse;
import com.biofer.ventas.model.Categoria;
import com.biofer.ventas.repository.CategoriaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public Page<CategoriaResponse> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::convertir);
    }

    public CategoriaResponse buscarPorId(Long id) {
        return convertir(obtener(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().strip();

        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe una categoría con ese nombre"
            );
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setActiva(request.activa());
        categoria.setPadre(resolverPadre(request.padreId(), null));

        return convertir(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(
            Long id,
            CategoriaRequest request) {

        Categoria categoria = obtener(id);
        String nombre = request.nombre().strip();

        if (repository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otra categoría con ese nombre"
            );
        }

        if (!request.activa()
                && repository.existsByPadre_IdAndActivaTrue(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Primero debe desactivar sus subcategorías"
            );
        }

        Categoria padre = resolverPadre(request.padreId(), id);

        categoria.setNombre(nombre);
        categoria.setPadre(padre);
        categoria.setActiva(request.activa());

        return convertir(repository.save(categoria));
    }

    @Transactional
    public void desactivar(Long id) {
        Categoria categoria = obtener(id);

        if (repository.existsByPadre_IdAndActivaTrue(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Primero debe desactivar sus subcategorías"
            );
        }

        categoria.setActiva(false);
        repository.save(categoria);
    }

    private Categoria obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoría no encontrada"
                ));
    }

    private Categoria resolverPadre(
            Long padreId,
            Long categoriaId) {

        if (padreId == null) {
            return null;
        }

        Categoria padre = obtener(padreId);

        if (!padre.isActiva()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La categoría padre está inactiva"
            );
        }

        Categoria actual = padre;

        while (actual != null) {
            if (actual.getId().equals(categoriaId)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La relación generaría un ciclo de categorías"
                );
            }

            actual = actual.getPadre();
        }

        return padre;
    }

    private CategoriaResponse convertir(Categoria categoria) {
        Long padreId = categoria.getPadre() == null
                ? null
                : categoria.getPadre().getId();

        return new CategoriaResponse(
            categoria.getId(),
            categoria.getNombre(),
            padreId,
            categoria.isActiva()
        );
    }
}