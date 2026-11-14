package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "despachos")
public class Despacho extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="pedido_id", nullable=false)
    private Long pedidoId;
    @Column(length = 2000)
    private String estado = "CONFIRMADO";
    
    private LocalDate fechaDespacho;
    @Column(length = 250)
    private String destinatario;
    @Column(length = 30)
    private String documentoDestinatario;
    @Column(length = 500)
    private String direccion;
    @Column(length = 150)
    private String agencia;
    @Column(length = 150)
    private String recoge;
    @Column(precision = 19, scale = 4)
    private BigDecimal costoEnvio = BigDecimal.ZERO;
    @Column(length = 3)
    private String monedaEnvio;
    @Column(length = 20)
    private String pagaEnvio;
    @Column(length = 150)
    private String conductor;
    @Column(length = 30)
    private String vehiculo;
    @Column(length = 1000)
    private String observaciones;

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDate fechaDespacho) { this.fechaDespacho = fechaDespacho; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDocumentoDestinatario() { return documentoDestinatario; }
    public void setDocumentoDestinatario(String documentoDestinatario) { this.documentoDestinatario = documentoDestinatario; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getRecoge() { return recoge; }
    public void setRecoge(String recoge) { this.recoge = recoge; }
    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }
    public String getMonedaEnvio() { return monedaEnvio; }
    public void setMonedaEnvio(String monedaEnvio) { this.monedaEnvio = monedaEnvio; }
    public String getPagaEnvio() { return pagaEnvio; }
    public void setPagaEnvio(String pagaEnvio) { this.pagaEnvio = pagaEnvio; }
    public String getConductor() { return conductor; }
    public void setConductor(String conductor) { this.conductor = conductor; }
    public String getVehiculo() { return vehiculo; }
    public void setVehiculo(String vehiculo) { this.vehiculo = vehiculo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="pedido_id", insertable=false, updatable=false)
    private Pedido relacionPedidoId;
}
