package com.biofer.ventas.dto;

import com.biofer.ventas.model.UnidadMedida;

import java.math.BigDecimal;
import java.time.Instant;

public record PresentacionResponse(
    Long id,
    Long productoId,
    String nombreComercial,
    String codigo,
    BigDecimal capacidad,
    UnidadMedida unidad,
    String otraUnidad,
    BigDecimal precioSinIgv,
    String moneda,
    int stockActual,
    int stockReservado,
    int stockDisponible,
    int stockMinimo,
    int stockAlto,
    String estadoStock,
    boolean activa,
    Instant fechaCreacion,
    Instant fechaActualizacion
) {
}