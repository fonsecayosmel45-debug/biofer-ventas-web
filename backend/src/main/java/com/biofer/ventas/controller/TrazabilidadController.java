package com.biofer.ventas.controller;

import com.biofer.ventas.model.RegistroAuditable;
import com.biofer.ventas.repository.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import static com.biofer.ventas.service.ReglasVenta.*;

@RestController @RequestMapping("/api/auditoria/entidades") @Transactional(readOnly=true)
public class TrazabilidadController {
    public record Actor(String usuarioCreador,String usuarioModificador,Instant creadoEn,Instant modificadoEn) {}
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final PresentacionRepository presentaciones;
    private final VendedorRepository vendedores;
    private final ClienteRepository clientes;
    public TrazabilidadController(ProductoRepository productos,CategoriaRepository categorias,PresentacionRepository presentaciones,VendedorRepository vendedores,ClienteRepository clientes) {
        this.productos=productos; this.categorias=categorias; this.presentaciones=presentaciones; this.vendedores=vendedores; this.clientes=clientes;
    }
    @GetMapping("/{tipo}/{id}") public Actor ver(@PathVariable String tipo,@PathVariable Long id) {
        RegistroAuditable registro=switch(tipo) {
            case "productos"->obtener(productos,id); case "categorias"->obtener(categorias,id);
            case "presentaciones"->obtener(presentaciones,id); case "vendedores"->obtener(vendedores,id); case "clientes"->obtener(clientes,id);
            default->throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Tipo de entidad no reconocido");
        };
        return new Actor(registro.getUsuarioCreador(),registro.getUsuarioModificador(),registro.getCreadoEn(),registro.getModificadoEn());
    }
}
