package com.biofer.ventas.controller;

import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import static com.biofer.ventas.service.ReglasVenta.*;

@RestController @RequestMapping("/api") @Transactional(readOnly=true)
public class ReporteController {
    private final FacturaRepository facturas;
    private final ClienteRepository clientes;
    private final PedidoRepository pedidos;
    private final PresentacionRepository presentaciones;
    private final DetalleDevolucionRepository retornos;
    private final TransferenciaDeudaRepository transferencias;
    private final EventoAuditoriaRepository eventos;
    public ReporteController(FacturaRepository facturas,ClienteRepository clientes,PedidoRepository pedidos,
        PresentacionRepository presentaciones,DetalleDevolucionRepository retornos,TransferenciaDeudaRepository transferencias,EventoAuditoriaRepository eventos) {
        this.facturas=facturas; this.clientes=clientes; this.pedidos=pedidos; this.presentaciones=presentaciones;
        this.retornos=retornos; this.transferencias=transferencias; this.eventos=eventos;
    }
    @GetMapping("/clientes/{id}/cuenta-corriente") public Object cliente(@PathVariable Long id) {
        obtener(clientes,id); var deuda=facturas.findAll().stream().filter(f->f.getClienteId().equals(id)).toList();
        Map<String,BigDecimal> saldos=new TreeMap<>(); deuda.forEach(f->saldos.merge(f.getMoneda(),f.getSaldo(),BigDecimal::add));
        return Map.of("clienteId",id,"saldosPorMoneda",saldos,"facturas",deuda);
    }
    @GetMapping("/reportes/vencimientos") public Object vencimientos(@RequestParam(defaultValue="7") int diasAviso) {
        exigir(diasAviso>=0 && diasAviso<=365,"diasAviso debe estar entre 0 y 365");
        return facturas.findAll().stream().filter(f->f.getSaldo().signum()>0 && !f.getFechaVencimiento().isAfter(hoy().plusDays(diasAviso)))
            .map(f->{
                var p=obtener(pedidos,f.getPedidoId());
                return Map.of("factura",f,"diasDesdeInicioCredito",ChronoUnit.DAYS.between(p.getFechaInicioCredito(),hoy()),
                    "diasMora",Math.max(0,ChronoUnit.DAYS.between(f.getFechaVencimiento(),hoy())),
                    "diasParaVencer",Math.max(0,ChronoUnit.DAYS.between(hoy(),f.getFechaVencimiento())));
            }).toList();
    }
    @GetMapping("/reportes/stock") public Object stock() {
        return presentaciones.findAll().stream().map(p->Map.of("presentacionId",p.getId(),"codigo",p.getCodigo(),
            "stockActual",p.getStockActual(),"reservado",p.getStockReservado(),"disponible",p.getStockActual()-p.getStockReservado(),
            "stockMinimo",p.getStockMinimo(),"stockAlto",p.getStockAlto(),"alerta",p.getStockActual()<p.getStockMinimo()?"BAJO":p.getStockActual()>p.getStockAlto()?"ALTO":"ESTABLE")).toList();
    }
    @GetMapping("/reportes/productos-danados") public Object danados(Pageable pageable) { return retornos.findAll(pageable).map(d->Map.of("detalleDevolucionId",d.getId(),"presentacionId",d.getPresentacionId(),"cantidadDanada",d.getDanada())); }
    @GetMapping("/auditoria") public Object auditoria(Pageable pageable) { return eventos.findAll(pageable); }
    @GetMapping("/transferencias-deuda") public Object transferencias(Pageable pageable) { return transferencias.findAll(pageable); }
    @GetMapping("/configuracion") public Object configuracion() {
        return Map.of("versionContrato","biofer-1","bloques",List.of("A: categorías","B: productos e inventario","C: vendedores y clientes","D: pedidos y reservas","E: despachos y documentos","F: crédito y cobros","G: devoluciones y reportes"),
            "emisionSUNAT",false,"igv","Configurable por pedido; 0.18 representa 18%","almacenes",1);
    }
}
