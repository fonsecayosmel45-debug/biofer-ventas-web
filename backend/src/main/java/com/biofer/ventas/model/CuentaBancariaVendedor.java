package com.biofer.ventas.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "cuentas_bancarias_vendedor",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cuenta_vendedor",
        columnNames = {
            "vendedor_id", "banco", "numero_cuenta", "moneda"
        }
    )
)
public class CuentaBancariaVendedor extends RegistroAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Vendedor vendedor;

    @Column(nullable = false, length = 100)
    private String banco;

    @Column(name = "numero_cuenta", nullable = false, length = 40)
    private String numeroCuenta;

    @Column(length = 20)
    private String cci;

    @Column(nullable = false, length = 200)
    private String titular;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(nullable = false)
    private boolean activa = true;

    public CuentaBancariaVendedor() {
    }

    public Long getId() { return id; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getCci() { return cci; }
    public void setCci(String cci) { this.cci = cci; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
