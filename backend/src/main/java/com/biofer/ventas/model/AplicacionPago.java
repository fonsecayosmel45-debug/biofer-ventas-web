package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "aplicaciones_pago")
public class AplicacionPago extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="pago_id", nullable=false)
    private Long pagoId;
    
    @Column(name="factura_id", nullable=false)
    private Long facturaId;
    @Column(precision = 19, scale = 4)
    private BigDecimal monto;
    
    private boolean revertida;

    public Long getId() { return id; }
    public Long getPagoId() { return pagoId; }
    public void setPagoId(Long pagoId) { this.pagoId = pagoId; }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public boolean getRevertida() { return revertida; }
    public void setRevertida(boolean revertida) { this.revertida = revertida; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pago_id", insertable=false, updatable=false)
    private Pago relacionPagoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="factura_id", insertable=false, updatable=false)
    private Factura relacionFacturaId;
}
