package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "facturas")
public class Factura extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="pedido_id", nullable=false)
    private Long pedidoId;
    
    @Column(name="despacho_id", nullable=false)
    private Long despachoId;
    
    @Column(name="cliente_id", nullable=false)
    private Long clienteId;
    @Column(length = 250)
    private String clienteNombre;
    @Column(length = 60)
    private String clienteDocumento;
    
    @Column(name="vendedor_id", nullable=false)
    private Long vendedorId;
    @Column(length = 3)
    private String moneda;
    @Column(precision = 19, scale = 4)
    private BigDecimal baseImponible;
    @Column(precision = 19, scale = 4)
    private BigDecimal igv;
    @Column(precision = 19, scale = 4)
    private BigDecimal total;
    @Column(precision = 19, scale = 4)
    private BigDecimal saldo;
    
    private LocalDate fechaEmision;
    
    private LocalDate fechaVencimiento;
    @Column(length = 2000)
    private String estado = "REGISTRADA";
    @Column(length = 20)
    private String serieOficial;
    @Column(length = 30)
    private String numeroOficial;
    @Column(length = 500)
    private String archivoOficial;

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getDespachoId() { return despachoId; }
    public void setDespachoId(Long despachoId) { this.despachoId = despachoId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSerieOficial() { return serieOficial; }
    public void setSerieOficial(String serieOficial) { this.serieOficial = serieOficial; }
    public String getNumeroOficial() { return numeroOficial; }
    public void setNumeroOficial(String numeroOficial) { this.numeroOficial = numeroOficial; }
    public String getArchivoOficial() { return archivoOficial; }
    public void setArchivoOficial(String archivoOficial) { this.archivoOficial = archivoOficial; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pedido_id", insertable=false, updatable=false)
    private Pedido relacionPedidoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="despacho_id", insertable=false, updatable=false)
    private Despacho relacionDespachoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="cliente_id", insertable=false, updatable=false)
    private Cliente relacionClienteId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorId;
}
