package com.biofer.ventas.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class SeguridadConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    private SecretKeySpec clave(String valor) {
        if (valor.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalStateException("BIOFER_JWT_SECRET necesita al menos 32 bytes");
        return new SecretKeySpec(valor.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
    }
    @Bean JwtEncoder jwtEncoder(@Value("${biofer.jwt-secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(secret)));
    }
    @Bean JwtDecoder jwtDecoder(@Value("${biofer.jwt-secret}") String secret) {
        var decoder=NimbusJwtDecoder.withSecretKey(clave(secret)).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("biofer-ventas")); return decoder;
    }
    @Bean SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        return http.csrf(c->c.disable()).cors(c->{})
            .sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(c->c.requestMatchers("/api/auth/login","/api/estado","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .anyRequest().hasAuthority("SCOPE_admin"))
            .oauth2ResourceServer(c->c.jwt(j->{})).build();
    }
    @Bean CorsConfigurationSource cors(@Value("${biofer.cors-origins:http://localhost:4200}") String origenes) {
        var config=new CorsConfiguration(); config.setAllowedOrigins(Arrays.asList(origenes.split(",")));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Idempotency-Key"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        var source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",config); return source;
    }
}
