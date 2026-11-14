package com.biofer.ventas.model;
import jakarta.persistence.*;
/** One durable mutex for the administrative sales workflow, across JVMs. */
@Entity @Table(name="control_concurrencia")
public class ControlConcurrencia {
    @Id private Long id;
    public ControlConcurrencia() {}
    public ControlConcurrencia(Long id) { this.id = id; }
}
