package com.biofer.ventas.dto;

import com.biofer.ventas.model.TipoDocumento;
import com.biofer.ventas.model.TipoPersona;

import java.time.Instant;
import java.time.LocalDate;

public final class ComercialResponse {

    private ComercialResponse() {
    }

    public record Persona(
        TipoPersona tipoPersona,
        String nombres,
        String apellidos,
        String razonSocial,
        String nombreComercial,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String telefono,
        String email,
        boolean activo
    ) {
    }

    public record Vendedor(
        Long id,
        String codigo,
        Persona datos,
        LocalDate fechaNacimiento,
        String telefonoAlternativo,
        String direccion,
        String departamento,
        String provincia,
        String ciudad,
        String monedaPago,
        LocalDate fechaCese,
        boolean vigente,
        Instant fechaCreacion,
        Instant fechaActualizacion
    ) {
    }

    public record Cliente(
        Long id,
        String codigo,
        Persona datos,
        String genero,
        Long vendedorId,
        String vendedorNombre,
        String metodoEnvio,
        String transportePreferido,
        Instant fechaCreacion,
        Instant fechaActualizacion
    ) {
    }

    public record Cuenta(
        Long id,
        Long vendedorId,
        String banco,
        String numeroCuenta,
        String cci,
        String titular,
        String moneda,
        boolean activa
    ) {
    }

    public record Direccion(
        Long id,
        Long clienteId,
        String destinatario,
        String documentoDestinatario,
        String direccion,
        String departamento,
        String provincia,
        String distrito,
        String ciudad,
        String referencia,
        boolean principal,
        boolean activa
    ) {
    }

    public record Asignacion(
        Long id,
        Long vendedorAnteriorId,
        Long vendedorNuevoId,
        String motivo,
        Instant fecha
    ) {
    }
}