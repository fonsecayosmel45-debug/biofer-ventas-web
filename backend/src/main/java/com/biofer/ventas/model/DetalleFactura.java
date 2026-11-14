package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "detalles_factura")
public class DetalleFactura extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="factura_id", nullable=false)
    private Long facturaId;
    
    @Column(name="detalle_despacho_id", nullable=false)
    private Long detalleDespachoId;
    
    @Column(name="detalle_pedido_id", nullable=false)
    private Long detallePedidoId;
    
    private int cantidad;
    
    private int devuelta;
    @Column(precision = 19, scale = 4)
    private BigDecimal baseImponible;
    @Column(precision = 19, scale = 4)
    private BigDecimal igv;
    @Column(precision = 19, scale = 4)
    private BigDecimal total;
    @Column(precision = 19, scale = 4)
    private BigDecimal importeDevuelto = BigDecimal.ZERO;

    public Long getId() { return id; }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public Long getDetalleDespachoId() { return detalleDespachoId; }
    public void setDetalleDespachoId(Long detalleDespachoId) { this.detalleDespachoId = detalleDespachoId; }
    public Long getDetallePedidoId() { return detallePedidoId; }
    public void setDetallePedidoId(Long detallePedidoId) { this.detallePedidoId = detallePedidoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getDevuelta() { return devuelta; }
    public void setDevuelta(int devuelta) { this.devuelta = devuelta; }
    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getImporteDevuelto() { return importeDevuelto; }
    public void setImporteDevuelto(BigDecimal importeDevuelto) { this.importeDevuelto = importeDevuelto; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="factura_id", insertable=false, updatable=false)
    private Factura relacionFacturaId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="detalle_despacho_id", insertable=false, updatable=false)
    private DetalleDespacho relacionDetalleDespachoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="detalle_pedido_id", insertable=false, updatable=false)
    private DetallePedido relacionDetallePedidoId;
}
