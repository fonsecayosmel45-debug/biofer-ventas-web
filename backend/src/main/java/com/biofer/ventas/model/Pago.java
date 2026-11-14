package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "pagos")
public class Pago extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="vendedor_id", nullable=false)
    private Long vendedorId;
    @Column(length = 3)
    private String moneda;
    @Column(precision = 19, scale = 4)
    private BigDecimal monto;
    @Column(precision = 19, scale = 4)
    private BigDecimal disponible = BigDecimal.ZERO;
    @Column(length = 100)
    private String metodo;
    @Column(nullable=false, unique=true, length=150)
    private String referencia;
    @Column(length = 500)
    private String comprobante;
    
    private LocalDate fechaPago;
    @Column(length = 2000)
    private String estado = "POR_COMPROBAR";
    @Column(length = 500)
    private String motivo;

    public Long getId() { return id; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getDisponible() { return disponible; }
    public void setDisponible(BigDecimal disponible) { this.disponible = disponible; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getComprobante() { return comprobante; }
    public void setComprobante(String comprobante) { this.comprobante = comprobante; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorId;
}
