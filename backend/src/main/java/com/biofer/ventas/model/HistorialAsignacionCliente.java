package com.biofer.ventas.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "historial_asignacion_cliente")
public class HistorialAsignacionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_anterior_id")
    private Vendedor vendedorAnterior;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_nuevo_id", nullable = false)
    private Vendedor vendedorNuevo;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    public HistorialAsignacionCliente() {
    }

    public HistorialAsignacionCliente(
            Cliente cliente,
            Vendedor vendedorAnterior,
            Vendedor vendedorNuevo,
            String motivo) {

        this.cliente = cliente;
        this.vendedorAnterior = vendedorAnterior;
        this.vendedorNuevo = vendedorNuevo;
        this.motivo = motivo;
    }

    @PrePersist
    private void alCrear() {
        fecha = Instant.now();
    }

    public Long getId() { return id; }

    public Vendedor getVendedorAnterior() {
        return vendedorAnterior;
    }

    public Vendedor getVendedorNuevo() {
        return vendedorNuevo;
    }

    public String getMotivo() { return motivo; }

    public Instant getFecha() { return fecha; }
}