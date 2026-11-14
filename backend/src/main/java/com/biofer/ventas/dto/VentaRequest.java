package com.biofer.ventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class VentaRequest {
    private VentaRequest() {}
    public record Linea(@NotNull @Positive Long presentacionId,
        @Min(1) @Max(1000000) int cantidad,
        @NotNull @DecimalMin("0") @Digits(integer=12,fraction=4) BigDecimal precioSinIgv,
        @NotBlank @Pattern(regexp="[A-Z]{3}") String moneda, boolean bonificacion) {}
    public record Pedido(@NotNull @Positive Long clienteId, Long direccionId,
        @Size(max=1000) String observaciones, boolean credito, @Min(0) @Max(3650) int diasCredito,
        @NotNull @DecimalMin("0") @DecimalMax("1") @Digits(integer=1,fraction=4) BigDecimal tasaIgv,
        @NotEmpty @Size(max=200) List<@Valid Linea> detalles,
        @Size(max=500) String motivo) {}
    public record Motivo(@NotBlank @Size(max=500) String motivo) {}
    public record Autorizacion(@Size(max=500) String motivoExcepcion) {}
    public record Cantidad(@NotNull @Positive Long detalleId, @Min(1) @Max(1000000) int cantidad) {}
    public record Despacho(@NotNull @Positive Long pedidoId,
        @NotEmpty @Size(max=200) List<@Valid Cantidad> detalles,
        @NotNull LocalDate fechaDespacho,
        @Size(max=250) String destinatario, @Size(max=30) String documentoDestinatario,
        @Size(max=500) String direccion, @Size(max=150) String agencia,
        @NotBlank @Size(max=150) String recoge,
        @NotNull @DecimalMin("0") @Digits(integer=12,fraction=2) BigDecimal costoEnvio,
        @NotBlank @Pattern(regexp="[A-Z]{3}") String monedaEnvio,
        @NotBlank @Pattern(regexp="CLIENTE|EMPRESA|VENDEDOR") String pagaEnvio,
        @Size(max=150) String conductor, @Size(max=30) String vehiculo,
        @Size(max=1000) String observaciones, @Size(max=500) String motivoExcepcion) {}
    public record Factura(@NotNull @Positive Long despachoId,
        @NotBlank @Pattern(regexp="[A-Z]{3}") String moneda,
        @NotNull LocalDate fechaEmision,
        @NotEmpty @Size(max=200) List<@Valid Cantidad> detalles) {}
    public record Documento(@NotBlank @Size(max=20) String serie,
        @NotBlank @Size(max=30) String numero, @NotBlank @Size(max=500) String archivo) {}
    public record Guia(@NotNull @Positive Long despachoId, @NotNull LocalDate fechaTraslado) {}
    public record Credito(@NotNull @Positive Long vendedorId,
        @NotBlank @Pattern(regexp="[A-Z]{3}") String moneda,
        @NotNull @DecimalMin("0") @Digits(integer=12,fraction=2) BigDecimal limite,
        boolean aprobado, @NotBlank @Size(max=500) String motivo) {}
    public record Pago(@NotNull @Positive Long vendedorId,
        @NotBlank @Pattern(regexp="[A-Z]{3}") String moneda,
        @NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal monto,
        @NotBlank @Size(max=100) String metodo, @NotBlank @Size(max=150) String referencia,
        @NotBlank @Size(max=500) String comprobante, @NotNull LocalDate fechaPago) {}
    public record Aplicacion(@NotNull @Positive Long facturaId,
        @NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal monto) {}
    public record Aplicaciones(@NotEmpty @Size(max=200) List<@Valid Aplicacion> detalles) {}
    public record Transferencia(@NotNull @Positive Long vendedorNuevoId,
        @NotBlank @Size(max=500) String motivo, @Size(max=500) String motivoExcepcion) {}
    public record Retorno(@NotNull @Positive Long detalleFacturaId,
        @Min(1) @Max(1000000) int cantidad, @Min(0) int apta) {}
    public record Devolucion(@NotNull @Positive Long facturaId,
        @NotEmpty @Size(max=200) List<@Valid Retorno> detalles,
        @NotBlank @Pattern(regexp="REDUCIR_DEUDA|SALDO_FAVOR|REEMBOLSO") String tratamiento,
        @NotBlank @Size(max=500) String motivo) {}
    public record Reembolso(@NotBlank @Size(max=150) String referencia,
        @NotBlank @Size(max=500) String comprobante) {}
}
