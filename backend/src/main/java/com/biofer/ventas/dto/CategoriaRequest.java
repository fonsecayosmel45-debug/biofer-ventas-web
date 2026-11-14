package com.biofer.ventas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    String nombre,

    Long padreId,

    @NotNull(message = "Debe indicar si la categoría está activa")
    Boolean activa

) {
}