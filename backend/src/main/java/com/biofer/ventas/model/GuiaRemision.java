package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "guias_remision")
public class GuiaRemision extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    @Column(name="despacho_id", nullable=false, unique=true)
    private Long despachoId;
    @Column(length = 20)
    private String serieOficial;
    @Column(length = 30)
    private String numeroOficial;
    @Column(length = 500)
    private String archivoOficial;
    
    private LocalDate fechaTraslado;

    public Long getId() { return id; }
    public Long getDespachoId() { return despachoId; }
    public void setDespachoId(Long despachoId) { this.despachoId = despachoId; }
    public String getSerieOficial() { return serieOficial; }
    public void setSerieOficial(String serieOficial) { this.serieOficial = serieOficial; }
    public String getNumeroOficial() { return numeroOficial; }
    public void setNumeroOficial(String numeroOficial) { this.numeroOficial = numeroOficial; }
    public String getArchivoOficial() { return archivoOficial; }
    public void setArchivoOficial(String archivoOficial) { this.archivoOficial = archivoOficial; }
    public LocalDate getFechaTraslado() { return fechaTraslado; }
    public void setFechaTraslado(LocalDate fechaTraslado) { this.fechaTraslado = fechaTraslado; }


    // Read-only associations enforce referential integrity without recursive JSON.
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="despacho_id", insertable=false, updatable=false)
    private Despacho relacionDespachoId;
}
