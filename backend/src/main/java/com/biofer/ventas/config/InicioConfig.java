package com.biofer.ventas.config;

import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InicioConfig implements ApplicationRunner {
    private final ControlConcurrenciaRepository control;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String nombre;
    private final String clave;
    public InicioConfig(ControlConcurrenciaRepository control,UsuarioRepository usuarios,PasswordEncoder encoder,
        @Value("${biofer.admin-user:admin}") String nombre,@Value("${biofer.admin-password}") String clave) {
        this.control=control; this.usuarios=usuarios; this.encoder=encoder; this.nombre=nombre; this.clave=clave;
    }
    @Override @Transactional public void run(ApplicationArguments args) {
        if (!control.existsById(1L)) control.saveAndFlush(new ControlConcurrencia(1L));
        if (usuarios.findByNombre(nombre).isEmpty()) {
            if (clave.length()<12) throw new IllegalStateException("BIOFER_ADMIN_PASSWORD necesita al menos 12 caracteres");
            var u=new Usuario(); u.setNombre(nombre); u.setClaveHash(encoder.encode(clave)); usuarios.save(u);
        }
    }
}
