package com.biofer.ventas.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    @GetMapping
    public Map<String, String> consultar() {
        return Map.of(
            "aplicacion", "BIOFER Ventas",
            "estado", "Backend funcionando"
        );
    }
}