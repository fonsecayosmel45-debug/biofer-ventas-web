package com.biofer.ventas.dto;

import com.biofer.ventas.model.TipoDocumento;
import com.biofer.ventas.model.TipoPersona;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public final class ComercialRequest {

    private ComercialRequest() {
    }

    public record Persona(
        @NotNull TipoPersona tipoPersona,
        @Size(max = 100) String nombres,
        @Size(max = 150) String apellidos,
        @Size(max = 200) String razonSocial,
        @Size(max = 150) String nombreComercial,
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank @Size(max = 30) String numeroDocumento,
        @Size(max = 30) String telefono,
        @Email @Size(max = 150) String email,
        @NotNull Boolean activo
    ) {
    }

    public record Vendedor(
        @NotNull @Valid Persona datos,
        @PastOrPresent LocalDate fechaNacimiento,
        @Size(max = 30) String telefonoAlternativo,
        @Size(max = 250) String direccion,
        @Size(max = 100) String departamento,
        @Size(max = 100) String provincia,
        @Size(max = 100) String ciudad,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String monedaPago,
        LocalDate fechaCese
    ) {
    }

    public record Cliente(
        @NotNull @Valid Persona datos,
        @Size(max = 30) String genero,
        @NotNull @Positive Long vendedorId,
        @Size(max = 100) String metodoEnvio,
        @Size(max = 150) String transportePreferido,
        @Size(max = 500) String motivoCambioVendedor
    ) {
    }

    public record Cuenta(
        @NotBlank @Size(max = 100) String banco,
        @NotBlank @Size(max = 40) String numeroCuenta,
        @Pattern(regexp = "[0-9]{20}") String cci,
        @NotBlank @Size(max = 200) String titular,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String moneda,
        @NotNull Boolean activa
    ) {
    }

    public record Direccion(
        @NotBlank @Size(max = 200) String destinatario,
        @Size(max = 30) String documentoDestinatario,
        @NotBlank @Size(max = 250) String direccion,
        @NotBlank @Size(max = 100) String departamento,
        @NotBlank @Size(max = 100) String provincia,
        @NotBlank @Size(max = 100) String distrito,
        @Size(max = 100) String ciudad,
        @Size(max = 250) String referencia,
        @NotNull Boolean principal,
        @NotNull Boolean activa
    ) {
    }
}