package com.biofer.ventas.dto;

public record CategoriaResponse(
    Long id,
    String nombre,
    Long padreId,
    boolean activa
) {
}