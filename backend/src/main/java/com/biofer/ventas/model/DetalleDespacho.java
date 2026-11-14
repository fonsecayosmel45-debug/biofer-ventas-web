package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "detalles_despacho")
public class DetalleDespacho extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="despacho_id", nullable=false)
    private Long despachoId;
    
    @Column(name="detalle_pedido_id", nullable=false)
    private Long detallePedidoId;
    
    private int cantidad;
    
    private int facturada;
    
    private int devuelta;

    public Long getId() { return id; }
    public Long getDespachoId() { return despachoId; }
    public void setDespachoId(Long despachoId) { this.despachoId = despachoId; }
    public Long getDetallePedidoId() { return detallePedidoId; }
    public void setDetallePedidoId(Long detallePedidoId) { this.detallePedidoId = detallePedidoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getFacturada() { return facturada; }
    public void setFacturada(int facturada) { this.facturada = facturada; }
    public int getDevuelta() { return devuelta; }
    public void setDevuelta(int devuelta) { this.devuelta = devuelta; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="despacho_id", insertable=false, updatable=false)
    private Despacho relacionDespachoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="detalle_pedido_id", insertable=false, updatable=false)
    private DetallePedido relacionDetallePedidoId;
}
