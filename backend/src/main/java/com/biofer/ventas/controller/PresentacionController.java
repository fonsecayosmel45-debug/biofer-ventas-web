package com.biofer.ventas.controller;

import com.biofer.ventas.dto.*;
import com.biofer.ventas.model.*;
import com.biofer.ventas.service.PresentacionService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presentaciones")
public class PresentacionController {

    private final PresentacionService service;

    public PresentacionController(PresentacionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PresentacionResponse> listar(
            @PageableDefault(size = 10, sort = "codigo")
            Pageable pageable) {

        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    public PresentacionResponse buscar(
            @PathVariable Long id) {

        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PresentacionResponse> crear(
            @Valid @RequestBody PresentacionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crear(request));
    }

    @PutMapping("/{id}")
    public PresentacionResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PresentacionRequest request) {

        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @PostMapping("/{id}/movimientos")
    public ResponseEntity<PresentacionResponse> movimiento(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoInventarioRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.registrarMovimiento(id, request));
    }

    @GetMapping("/{id}/precios")
    public Page<HistorialPrecio> precios(
            @PathVariable Long id,
            @PageableDefault(
                size = 10,
                sort = "id",
                direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return service.historialPrecios(id, pageable);
    }

    @GetMapping("/{id}/kardex")
    public Page<MovimientoInventario> kardex(
            @PathVariable Long id,
            @PageableDefault(
                size = 10,
                sort = "id",
                direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return service.kardex(id, pageable);
    }
}