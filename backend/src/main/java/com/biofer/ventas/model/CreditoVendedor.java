package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "creditos_vendedor", uniqueConstraints=@UniqueConstraint(columnNames={"vendedor_id","moneda"}))
public class CreditoVendedor extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    
    @Column(name="vendedor_id", nullable=false)
    private Long vendedorId;
    @Column(length = 3)
    private String moneda;
    @Column(precision = 19, scale = 4)
    private BigDecimal limite;
    
    private boolean aprobado;
    @Column(length = 500)
    private String motivo;

    public Long getId() { return id; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getLimite() { return limite; }
    public void setLimite(BigDecimal limite) { this.limite = limite; }
    public boolean getAprobado() { return aprobado; }
    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendedor_id", insertable=false, updatable=false)
    private Vendedor relacionVendedorId;
}
