package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "devoluciones")
public class Devolucion extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="factura_id", nullable=false)
    private Long facturaId;
    
    @Column(name="vendedor_id", nullable=false)
    private Long vendedorId;
    @Column(length = 3)
    private String moneda;
    @Column(length = 500)
    private String motivo;
    @Column(length = 30)
    private String tratamiento;
    @Column(precision = 19, scale = 4)
    private BigDecimal importe;
    @Column(length = 20)
    private String estadoReembolso;
    @Column(length = 150)
    private String referenciaReembolso;
    @Column(length = 500)
    private String comprobanteReembolso;

    public Long getId() { return id; }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public String getEstadoReembolso() { return estadoReembolso; }
    public void setEstadoReembolso(String estadoReembolso) { this.estadoReembolso = estadoReembolso; }
    public String getReferenciaReembolso() { return referenciaReembolso; }
    public void setReferenciaReembolso(String referenciaReembolso) { this.referenciaReembolso = referenciaReembolso; }
    public String getComprobanteReembolso() { return comprobanteReembolso; }
    public void setComprobanteReembolso(String comprobanteReembolso) { this.comprobanteReembolso = comprobanteReembolso; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="factura_id", insertable=false, updatable=false)
    private Factura relacionFacturaId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorId;
}
