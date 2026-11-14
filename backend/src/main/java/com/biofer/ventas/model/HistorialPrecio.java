package com.biofer.ventas.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "historial_precios")
public class HistorialPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presentacion_id", nullable = false)
    private Presentacion presentacion;

    @Column(precision = 19, scale = 4)
    private BigDecimal precioAnterior;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal precioNuevo;

    @Column(length = 3)
    private String monedaAnterior;

    @Column(nullable = false, length = 3)
    private String monedaNueva;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    public HistorialPrecio() {
    }

    public HistorialPrecio(
            Presentacion presentacion,
            BigDecimal precioAnterior,
            BigDecimal precioNuevo,
            String monedaAnterior,
            String monedaNueva,
            String motivo) {

        this.presentacion = presentacion;
        this.precioAnterior = precioAnterior;
        this.precioNuevo = precioNuevo;
        this.monedaAnterior = monedaAnterior;
        this.monedaNueva = monedaNueva;
        this.motivo = motivo;
    }

    @PrePersist
    private void alCrear() {
        fecha = Instant.now();
    }

    public Long getId() { return id; }

    public Long getPresentacionId() {
        return presentacion.getId();
    }

    public BigDecimal getPrecioAnterior() {
        return precioAnterior;
    }

    public BigDecimal getPrecioNuevo() { return precioNuevo; }

    public String getMonedaAnterior() { return monedaAnterior; }

    public String getMonedaNueva() { return monedaNueva; }

    public String getMotivo() { return motivo; }

    public Instant getFecha() { return fecha; }
}