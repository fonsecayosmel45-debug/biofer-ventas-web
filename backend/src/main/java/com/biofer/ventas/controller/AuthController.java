package com.biofer.ventas.controller;

import com.biofer.ventas.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.security.Principal;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    public record Login(@NotBlank String usuario,@NotBlank String clave) {}
    public record CambioClave(@NotBlank String claveActual,@NotBlank @Size(min=12,max=72) String claveNueva) {}
    private final UsuarioRepository usuarios;
    private final PasswordEncoder claves;
    private final JwtEncoder jwt;
    public AuthController(UsuarioRepository usuarios,PasswordEncoder claves,JwtEncoder jwt) { this.usuarios=usuarios; this.claves=claves; this.jwt=jwt; }
    @PostMapping("/login") public Object login(@Valid @RequestBody Login r) {
        var usuario=usuarios.findByNombre(r.usuario()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas"));
        if (!usuario.getActivo() || !claves.matches(r.clave(),usuario.getClaveHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas");
        var now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer("biofer-ventas").subject(usuario.getNombre()).issuedAt(now).expiresAt(now.plusSeconds(3600)).claim("scope","admin").build();
        var token=jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return Map.of("token",token,"tipo","Bearer","expiraEnSegundos",3600,"usuario",usuario.getNombre());
    }
    @PostMapping("/cambiar-clave") @Transactional public Object cambiar(@Valid @RequestBody CambioClave r,Principal principal) {
        var usuario=usuarios.findByNombre(principal.getName()).orElseThrow();
        if (!claves.matches(r.claveActual(),usuario.getClaveHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Clave actual incorrecta");
        usuario.setClaveHash(claves.encode(r.claveNueva())); return Map.of("mensaje","Clave actualizada");
    }
}
