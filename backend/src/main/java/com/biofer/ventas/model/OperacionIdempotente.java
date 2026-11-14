package com.biofer.ventas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "operaciones_idempotentes")
public class OperacionIdempotente extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private Long version;
    @Column(nullable=false,unique=true,length=150)
    private String clave;
    @Column(length = 150)
    private String operacion;
    @Column(length = 64)
    private String huella;
    @Lob
    private String respuesta;

    public Long getId() { return id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }
    public String getHuella() { return huella; }
    public void setHuella(String huella) { this.huella = huella; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

}
