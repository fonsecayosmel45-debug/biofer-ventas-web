package com.biofer.ventas.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario extends RegistroAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presentacion_id", nullable = false)
    private Presentacion presentacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int saldoAnterior;

    @Column(nullable = false)
    private int saldoPosterior;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(length = 100)
    private String referencia;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    public MovimientoInventario() {
    }

    public MovimientoInventario(
            Presentacion presentacion,
            TipoMovimiento tipo,
            int cantidad,
            int saldoAnterior,
            int saldoPosterior,
            String motivo,
            String referencia) {

        this.presentacion = presentacion;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.motivo = motivo;
        this.referencia = referencia;
    }

    @PrePersist
    private void alCrear() {
        fecha = Instant.now();
    }

    public Long getId() { return id; }

    public Long getPresentacionId() {
        return presentacion.getId();
    }

    public TipoMovimiento getTipo() { return tipo; }

    public int getCantidad() { return cantidad; }

    public int getSaldoAnterior() { return saldoAnterior; }

    public int getSaldoPosterior() { return saldoPosterior; }

    public String getMotivo() { return motivo; }

    public String getReferencia() { return referencia; }

    public Instant getFecha() { return fecha; }
}
