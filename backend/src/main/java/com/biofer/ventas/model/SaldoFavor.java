package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "saldos_favor")
public class SaldoFavor extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="vendedor_id", nullable=false)
    private Long vendedorId;
    @Column(length = 3)
    private String moneda;
    @Column(precision = 19, scale = 4)
    private BigDecimal disponible;
    
    @Column(name="devolucion_id", nullable=false)
    private Long devolucionId;

    public Long getId() { return id; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getDisponible() { return disponible; }
    public void setDisponible(BigDecimal disponible) { this.disponible = disponible; }
    public Long getDevolucionId() { return devolucionId; }
    public void setDevolucionId(Long devolucionId) { this.devolucionId = devolucionId; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="devolucion_id", insertable=false, updatable=false)
    private Devolucion relacionDevolucionId;
}
