package com.biofer.ventas.dto;

import com.biofer.ventas.model.Procedencia;

import java.time.Instant;
import java.util.List;

public record ProductoResponse(
    Long id,
    String nombreComercial,
    String nombreProducto,
    Procedencia procedencia,
    String descripcion,
    boolean activo,
    List<CategoriaResponse> categorias,
    Instant fechaCreacion,
    Instant fechaActualizacion
) {
}