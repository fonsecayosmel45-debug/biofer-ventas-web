package com.biofer.ventas.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Reference prices for Administration; never overwrite an order's agreed price. */
@Entity @Table(name="tarifas_comerciales")
public class TarifaComercial extends RegistroAuditable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="vendedor_id") private Long vendedorId;
    @Column(name="cliente_id") private Long clienteId;
    @Column(name="presentacion_id",nullable=false) private Long presentacionId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="vendedor_id",insertable=false,updatable=false)
    private Vendedor relacionVendedor;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cliente_id",insertable=false,updatable=false)
    private Cliente relacionCliente;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="presentacion_id",insertable=false,updatable=false)
    private Presentacion relacionPresentacion;
    @Column(precision=19,scale=4) private BigDecimal precioSinIgv;
    @Column(length=3) private String moneda;
    private LocalDate vigenteDesde;
    private LocalDate vigenteHasta;
    private boolean activa=true;
    @Column(length=500) private String motivo;
    public Long getId(){return id;}
    public Long getVendedorId(){return vendedorId;}
    public void setVendedorId(Long v){vendedorId=v;}
    public Long getClienteId(){return clienteId;}
    public void setClienteId(Long v){clienteId=v;}
    public Long getPresentacionId(){return presentacionId;}
    public void setPresentacionId(Long v){presentacionId=v;}
    public BigDecimal getPrecioSinIgv(){return precioSinIgv;}
    public void setPrecioSinIgv(BigDecimal v){precioSinIgv=v;}
    public String getMoneda(){return moneda;}
    public void setMoneda(String v){moneda=v;}
    public LocalDate getVigenteDesde(){return vigenteDesde;}
    public void setVigenteDesde(LocalDate v){vigenteDesde=v;}
    public LocalDate getVigenteHasta(){return vigenteHasta;}
    public void setVigenteHasta(LocalDate v){vigenteHasta=v;}
    public boolean getActiva(){return activa;}
    public void setActiva(boolean v){activa=v;}
    public String getMotivo(){return motivo;}
    public void setMotivo(String v){motivo=v;}
}
