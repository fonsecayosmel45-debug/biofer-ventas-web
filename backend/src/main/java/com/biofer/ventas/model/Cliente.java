package com.biofer.ventas.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "clientes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cliente_documento",
        columnNames = {"tipo_documento", "numero_documento"}
    )
)
public class Cliente extends PersonaComercial {

    @Column(length = 30)
    private String genero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Vendedor vendedor;

    @Column(length = 100)
    private String metodoEnvio;

    @Column(length = 150)
    private String transportePreferido;

    public Cliente() {
    }

    public String getGenero() { return genero; }
    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public String getMetodoEnvio() { return metodoEnvio; }
    public void setMetodoEnvio(String metodoEnvio) {
        this.metodoEnvio = metodoEnvio;
    }

    public String getTransportePreferido() {
        return transportePreferido;
    }
    public void setTransportePreferido(String transportePreferido) {
        this.transportePreferido = transportePreferido;
    }
}
