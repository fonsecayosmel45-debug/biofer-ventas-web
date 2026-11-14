package com.biofer.ventas.model;
import jakarta.persistence.*;
import java.time.Instant;
import org.springframework.security.core.context.SecurityContextHolder;

/** Actor authenticated by Spring Security; existing records may have a null actor. */
@MappedSuperclass
public abstract class RegistroAuditable {
    private String usuarioCreador;
    private String usuarioModificador;
    private Instant creadoEn;
    private Instant modificadoEn;
    @PrePersist protected void registrarCreacion() {
        usuarioCreador = actor(); usuarioModificador = usuarioCreador;
        creadoEn = Instant.now(); modificadoEn = creadoEn;
    }
    @PreUpdate protected void registrarCambio() { usuarioModificador = actor(); modificadoEn = Instant.now(); }
    private static String actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "sistema" : auth.getName();
    }
    public String getUsuarioCreador() { return usuarioCreador; }
    public String getUsuarioModificador() { return usuarioModificador; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getModificadoEn() { return modificadoEn; }
}
