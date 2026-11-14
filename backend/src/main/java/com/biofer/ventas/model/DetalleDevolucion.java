package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "detalles_devolucion")
public class DetalleDevolucion extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="devolucion_id", nullable=false)
    private Long devolucionId;
    
    @Column(name="detalle_factura_id", nullable=false)
    private Long detalleFacturaId;
    
    @Column(name="presentacion_id", nullable=false)
    private Long presentacionId;
    
    private int cantidad;
    
    private int apta;
    
    private int danada;
    @Column(precision = 19, scale = 4)
    private BigDecimal importe;

    public Long getId() { return id; }
    public Long getDevolucionId() { return devolucionId; }
    public void setDevolucionId(Long devolucionId) { this.devolucionId = devolucionId; }
    public Long getDetalleFacturaId() { return detalleFacturaId; }
    public void setDetalleFacturaId(Long detalleFacturaId) { this.detalleFacturaId = detalleFacturaId; }
    public Long getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Long presentacionId) { this.presentacionId = presentacionId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getApta() { return apta; }
    public void setApta(int apta) { this.apta = apta; }
    public int getDanada() { return danada; }
    public void setDanada(int danada) { this.danada = danada; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="devolucion_id", insertable=false, updatable=false)
    private Devolucion relacionDevolucionId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="detalle_factura_id", insertable=false, updatable=false)
    private DetalleFactura relacionDetalleFacturaId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="presentacion_id", insertable=false, updatable=false)
    private Presentacion relacionPresentacionId;
}
