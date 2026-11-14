package com.biofer.ventas.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "presentaciones")
public class Presentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadMedida unidad;

    @Column(length = 50)
    private String otraUnidad;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal precioSinIgv;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(nullable = false)
    private int stockActual = 0;

    @Column(nullable = false)
    private int stockReservado = 0;

    @Column(nullable = false)
    private int stockMinimo;

    @Column(nullable = false)
    private int stockAlto;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(nullable = false)
    private Instant fechaActualizacion;

    public Presentacion() {
    }

    @PrePersist
    private void alCrear() {
        Instant ahora = Instant.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
    }

    @PreUpdate
    private void alActualizar() {
        fechaActualizacion = Instant.now();
    }

    public Long getId() { return id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getCapacidad() { return capacidad; }
    public void setCapacidad(BigDecimal capacidad) {
        this.capacidad = capacidad;
    }

    public UnidadMedida getUnidad() { return unidad; }
    public void setUnidad(UnidadMedida unidad) {
        this.unidad = unidad;
    }

    public String getOtraUnidad() { return otraUnidad; }
    public void setOtraUnidad(String otraUnidad) {
        this.otraUnidad = otraUnidad;
    }

    public BigDecimal getPrecioSinIgv() { return precioSinIgv; }
    public void setPrecioSinIgv(BigDecimal precioSinIgv) {
        this.precioSinIgv = precioSinIgv;
    }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockReservado() { return stockReservado; }
    public void setStockReservado(int stockReservado) {
        this.stockReservado = stockReservado;
    }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getStockAlto() { return stockAlto; }
    public void setStockAlto(int stockAlto) {
        this.stockAlto = stockAlto;
    }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Instant getFechaCreacion() { return fechaCreacion; }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }
}