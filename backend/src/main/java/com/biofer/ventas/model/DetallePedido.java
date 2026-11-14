package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "detalles_pedido")
public class DetallePedido extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="pedido_id", nullable=false)
    private Long pedidoId;
    
    @Column(name="presentacion_id", nullable=false)
    private Long presentacionId;
    @Column(length = 500)
    private String descripcion;
    @Column(length = 40)
    private String codigo;
    
    private int cantidad;
    
    private int reservada;
    
    private int despachada;
    
    private int facturada;
    
    private int cerrada;
    @Column(precision = 19, scale = 4)
    private BigDecimal precioSinIgv;
    @Column(length = 3)
    private String moneda;
    
    private boolean bonificacion;

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Long presentacionId) { this.presentacionId = presentacionId; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getReservada() { return reservada; }
    public void setReservada(int reservada) { this.reservada = reservada; }
    public int getDespachada() { return despachada; }
    public void setDespachada(int despachada) { this.despachada = despachada; }
    public int getFacturada() { return facturada; }
    public void setFacturada(int facturada) { this.facturada = facturada; }
    public int getCerrada() { return cerrada; }
    public void setCerrada(int cerrada) { this.cerrada = cerrada; }
    public BigDecimal getPrecioSinIgv() { return precioSinIgv; }
    public void setPrecioSinIgv(BigDecimal precioSinIgv) { this.precioSinIgv = precioSinIgv; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public boolean getBonificacion() { return bonificacion; }
    public void setBonificacion(boolean bonificacion) { this.bonificacion = bonificacion; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pedido_id", insertable=false, updatable=false)
    private Pedido relacionPedidoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="presentacion_id", insertable=false, updatable=false)
    private Presentacion relacionPresentacionId;
}
