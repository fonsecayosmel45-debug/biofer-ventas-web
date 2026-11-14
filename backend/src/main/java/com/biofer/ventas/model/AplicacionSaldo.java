package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "aplicaciones_saldo")
public class AplicacionSaldo extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="saldo_favor_id", nullable=false)
    private Long saldoFavorId;
    
    @Column(name="factura_id", nullable=false)
    private Long facturaId;
    @Column(precision = 19, scale = 4)
    private BigDecimal monto;

    public Long getId() { return id; }
    public Long getSaldoFavorId() { return saldoFavorId; }
    public void setSaldoFavorId(Long saldoFavorId) { this.saldoFavorId = saldoFavorId; }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="saldo_favor_id", insertable=false, updatable=false)
    private SaldoFavor relacionSaldoFavorId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="factura_id", insertable=false, updatable=false)
    private Factura relacionFacturaId;
}
