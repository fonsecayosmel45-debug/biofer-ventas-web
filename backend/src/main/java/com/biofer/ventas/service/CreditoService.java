package com.biofer.ventas.service;

import com.biofer.ventas.dto.VentaRequest;
import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import static com.biofer.ventas.service.ReglasVenta.*;

@Service @Transactional(readOnly=true)
public class CreditoService {
    private final CreditoVendedorRepository creditos;
    private final VendedorRepository vendedores;
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final FacturaRepository facturas;
    private final ControlOperacionService control;
    public CreditoService(CreditoVendedorRepository creditos,VendedorRepository vendedores,PedidoRepository pedidos,
        DetallePedidoRepository detalles,FacturaRepository facturas,ControlOperacionService control) {
        this.creditos=creditos; this.vendedores=vendedores; this.pedidos=pedidos;
        this.detalles=detalles; this.facturas=facturas; this.control=control;
    }
    public CreditoVendedor guardar(VentaRequest.Credito r) {
        exigir(obtener(vendedores,r.vendedorId()).isVigente(),"Vendedor inactivo o cesado"); moneda(r.moneda());
        var c=creditos.findByVendedorIdAndMoneda(r.vendedorId(),r.moneda()).orElse(new CreditoVendedor());
        c.setVendedorId(r.vendedorId()); c.setMoneda(r.moneda()); c.setLimite(r.limite()); c.setAprobado(r.aprobado()); c.setMotivo(r.motivo());
        control.evento("CREDITO",r.vendedorId().toString(),r.motivo()); return creditos.save(c);
    }
    public Map<String,BigDecimal> totales(Pedido p) {
        Map<String,BigDecimal> resultado=new TreeMap<>();
        for (var l:detalles.findByPedidoIdOrderByIdAsc(p.getId())) {
            resultado.merge(l.getMoneda(),total(l.getPrecioSinIgv(),l.getCantidad()-l.getCerrada(),p.getTasaIgv()),BigDecimal::add);
        }
        return resultado;
    }
    public Map<String,BigDecimal> compromisos(Long vendedorId) {
        Map<String,BigDecimal> resultado=new TreeMap<>();
        for (var p:pedidos.findByVendedorId(vendedorId)) {
            if (!Set.of("APROBADO","CERRADO","DESPACHADO").contains(p.getEstado())) continue;
            Map<String,BigDecimal> pendiente=new TreeMap<>(totales(p));
            for (var f:facturas.findByPedidoIdOrderByIdAsc(p.getId())) pendiente.merge(f.getMoneda(),f.getTotal().negate(),BigDecimal::add);
            pendiente.forEach((m,i)->resultado.merge(m,i.max(CERO),BigDecimal::add));
        }
        return resultado;
    }
    public BigDecimal deuda(Long vendedor,String moneda) {
        return facturas.findByVendedorId(vendedor).stream().filter(f->f.getMoneda().equals(moneda))
            .map(Factura::getSaldo).reduce(CERO,BigDecimal::add);
    }
    public void validar(Long vendedor,Map<String,BigDecimal> incremento,String excepcion) {
        exigir(obtener(vendedores,vendedor).isVigente(),"Vendedor inactivo o cesado");
        var compromiso=compromisos(vendedor);
        for (var entrada:incremento.entrySet()) {
            if (entrada.getValue().signum()==0 && deuda(vendedor,entrada.getKey()).signum()==0
                && compromiso.getOrDefault(entrada.getKey(),CERO).signum()==0) continue;
            var c=creditos.findByVendedorIdAndMoneda(vendedor,entrada.getKey()).orElse(null);
            boolean mora=facturas.findByVendedorId(vendedor).stream().anyMatch(f->f.getMoneda().equals(entrada.getKey())
                && f.getSaldo().signum()>0 && f.getFechaVencimiento().isBefore(hoy()));
            BigDecimal exposicion=deuda(vendedor,entrada.getKey()).add(compromiso.getOrDefault(entrada.getKey(),CERO)).add(entrada.getValue());
            if (c==null || !c.getAprobado() || mora || exposicion.compareTo(c.getLimite())>0) {
                exigir(excepcion!=null && !excepcion.isBlank(),
                    "Crédito bloqueado en "+entrada.getKey()+": "+(c==null || !c.getAprobado()?"no aprobado":mora?"deuda vencida":"cupo excedido")
                    +". Exposición: "+exposicion+". Indique motivoExcepcion para una autorización administrativa");
                control.evento("EXCEPCION_CREDITO",vendedor+":"+entrada.getKey(),excepcion);
            }
        }
    }
    public Object cuenta(Long vendedor) {
        obtener(vendedores,vendedor);
        return Map.of("vendedorId",vendedor,"deudas",facturas.findByVendedorId(vendedor),
            "compromisosPorMoneda",compromisos(vendedor),"limites",creditos.findAll().stream().filter(c->c.getVendedorId().equals(vendedor)).toList());
    }
}
