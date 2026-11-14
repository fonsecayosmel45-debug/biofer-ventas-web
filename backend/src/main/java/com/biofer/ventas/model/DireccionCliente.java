package com.biofer.ventas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "direcciones_cliente")
public class DireccionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 200)
    private String destinatario;

    @Column(length = 30)
    private String documentoDestinatario;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 250)
    private String referencia;

    @Column(nullable = false)
    private boolean principal;

    @Column(nullable = false)
    private boolean activa = true;

    public DireccionCliente() {
    }

    public Long getId() { return id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDocumentoDestinatario() {
        return documentoDestinatario;
    }
    public void setDocumentoDestinatario(String documento) {
        this.documentoDestinatario = documento;
    }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}