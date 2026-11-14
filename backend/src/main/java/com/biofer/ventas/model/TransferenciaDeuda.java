package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "transferencias_deuda")
public class TransferenciaDeuda extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="factura_id", nullable=false)
    private Long facturaId;
    
    @Column(name="vendedor_anterior_id", nullable=false)
    private Long vendedorAnteriorId;
    
    @Column(name="vendedor_nuevo_id", nullable=false)
    private Long vendedorNuevoId;
    @Column(precision = 19, scale = 4)
    private BigDecimal saldoTransferido;
    @Column(length = 500)
    private String motivo;

    public Long getId() { return id; }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public Long getVendedorAnteriorId() { return vendedorAnteriorId; }
    public void setVendedorAnteriorId(Long vendedorAnteriorId) { this.vendedorAnteriorId = vendedorAnteriorId; }
    public Long getVendedorNuevoId() { return vendedorNuevoId; }
    public void setVendedorNuevoId(Long vendedorNuevoId) { this.vendedorNuevoId = vendedorNuevoId; }
    public BigDecimal getSaldoTransferido() { return saldoTransferido; }
    public void setSaldoTransferido(BigDecimal saldoTransferido) { this.saldoTransferido = saldoTransferido; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="factura_id", insertable=false, updatable=false)
    private Factura relacionFacturaId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_anterior_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorAnteriorId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_nuevo_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorNuevoId;
}
