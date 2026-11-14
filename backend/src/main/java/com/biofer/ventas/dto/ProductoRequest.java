package com.biofer.ventas.dto;

import com.biofer.ventas.model.Procedencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ProductoRequest(

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 150)
    String nombreComercial,

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150)
    String nombreProducto,

    @NotNull(message = "La procedencia es obligatoria")
    Procedencia procedencia,

    @Size(max = 2000)
    String descripcion,

    @NotNull(message = "Debe indicar el estado")
    Boolean activo,

    @NotEmpty(message = "Seleccione al menos una categoría")
    Set<@NotNull @Positive Long> categoriasIds

) {
}