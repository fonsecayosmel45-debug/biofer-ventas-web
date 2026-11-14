package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "pedidos")
public class Pedido extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="cliente_id", nullable=false)
    private Long clienteId;
    
    @Column(name="vendedor_id", nullable=false)
    private Long vendedorId;
    @Column(length = 250)
    private String clienteNombre;
    @Column(length = 60)
    private String clienteDocumento;
    @Column(length = 500)
    private String direccionEnvio;
    @Column(length = 250)
    private String destinatario;
    @Column(length = 30)
    private String documentoDestinatario;
    @Column(length = 150)
    private String agencia;
    @Column(length = 100)
    private String metodoEnvio;
    @Column(length = 1000)
    private String observaciones;
    @Column(length = 2000)
    private String estado = "BORRADOR";
    
    private boolean credito;
    
    private int diasCredito;
    @Column(precision = 19, scale = 4)
    private BigDecimal tasaIgv;
    
    private Instant fechaAprobacion;
    
    private LocalDate fechaInicioCredito;
    
    private LocalDate fechaVencimiento;
    @Column(length = 500)
    private String motivoCierre;

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDocumentoDestinatario() { return documentoDestinatario; }
    public void setDocumentoDestinatario(String documentoDestinatario) { this.documentoDestinatario = documentoDestinatario; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getMetodoEnvio() { return metodoEnvio; }
    public void setMetodoEnvio(String metodoEnvio) { this.metodoEnvio = metodoEnvio; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean getCredito() { return credito; }
    public void setCredito(boolean credito) { this.credito = credito; }
    public int getDiasCredito() { return diasCredito; }
    public void setDiasCredito(int diasCredito) { this.diasCredito = diasCredito; }
    public BigDecimal getTasaIgv() { return tasaIgv; }
    public void setTasaIgv(BigDecimal tasaIgv) { this.tasaIgv = tasaIgv; }
    public Instant getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(Instant fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
    public LocalDate getFechaInicioCredito() { return fechaInicioCredito; }
    public void setFechaInicioCredito(LocalDate fechaInicioCredito) { this.fechaInicioCredito = fechaInicioCredito; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public String getMotivoCierre() { return motivoCierre; }
    public void setMotivoCierre(String motivoCierre) { this.motivoCierre = motivoCierre; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="cliente_id", insertable=false, updatable=false)
    private Cliente relacionClienteId;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorId;
}
