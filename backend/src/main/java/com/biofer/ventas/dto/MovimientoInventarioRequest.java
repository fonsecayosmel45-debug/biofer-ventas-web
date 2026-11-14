package com.biofer.ventas.dto;

import com.biofer.ventas.model.TipoMovimiento;

import jakarta.validation.constraints.*;

public record MovimientoInventarioRequest(

    @NotNull
    TipoMovimiento tipo,

    @Min(1)
    @Max(1000000)
    int cantidad,

    @NotBlank
    @Size(max = 500)
    String motivo,

    @Size(max = 100)
    String referencia

) {
}