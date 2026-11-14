package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "fondos_pedido")
public class FondoPedido extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="pedido_id", nullable=false)
    private Long pedidoId;
    
    @Column(name="pago_id", nullable=false)
    private Long pagoId;
    @Column(length = 3)
    private String moneda;
    @Column(precision = 19, scale = 4)
    private BigDecimal disponible;

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getPagoId() { return pagoId; }
    public void setPagoId(Long pagoId) { this.pagoId = pagoId; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getDisponible() { return disponible; }
    public void setDisponible(BigDecimal disponible) { this.disponible = disponible; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pedido_id", insertable=false, updatable=false)
    private Pedido relacionPedidoId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pago_id", insertable=false, updatable=false)
    private Pago relacionPagoId;
}
