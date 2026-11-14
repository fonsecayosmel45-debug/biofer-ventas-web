package com.biofer.ventas.dto;

import com.biofer.ventas.model.UnidadMedida;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PresentacionRequest(

    @NotNull
    @Positive
    Long productoId,

    @NotBlank
    @Size(max = 40)
    String codigo,

    @NotNull
    @DecimalMin("0.001")
    @Digits(integer = 9, fraction = 3)
    BigDecimal capacidad,

    @NotNull
    UnidadMedida unidad,

    @Size(max = 50)
    String otraUnidad,

    @NotNull
    @DecimalMin("0.0001")
    @Digits(integer = 15, fraction = 4)
    BigDecimal precioSinIgv,

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    String moneda,

    @Min(0)
    int stockMinimo,

    @Min(0)
    int stockAlto,

    @NotNull
    Boolean activa,

    @Size(max = 500)
    String motivoCambioPrecio

) {
}