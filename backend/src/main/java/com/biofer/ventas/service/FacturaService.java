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
public class FacturaService {
    private final FacturaRepository facturas;
    private final DetalleFacturaRepository detalles;
    private final DespachoRepository despachos;
    private final DetalleDespachoRepository despachados;
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository lineas;
    private final GuiaRemisionRepository guias;
    private final PagoService pagos;
    private final CreditoService creditos;
    private final TransferenciaDeudaRepository transferencias;
    private final AplicacionPagoRepository aplicaciones;
    private final AplicacionSaldoRepository aplicacionesSaldo;
    private final DevolucionRepository devoluciones;
    private final ControlOperacionService control;
    public FacturaService(FacturaRepository facturas,DetalleFacturaRepository detalles,DespachoRepository despachos,
        DetalleDespachoRepository despachados,PedidoRepository pedidos,DetallePedidoRepository lineas,GuiaRemisionRepository guias,
        PagoService pagos,CreditoService creditos,TransferenciaDeudaRepository transferencias,
        AplicacionPagoRepository aplicaciones,AplicacionSaldoRepository aplicacionesSaldo,DevolucionRepository devoluciones,ControlOperacionService control) {
        this.facturas=facturas; this.detalles=detalles; this.despachos=despachos; this.despachados=despachados;
        this.pedidos=pedidos; this.lineas=lineas; this.guias=guias; this.pagos=pagos; this.creditos=creditos;
        this.transferencias=transferencias; this.aplicaciones=aplicaciones; this.aplicacionesSaldo=aplicacionesSaldo;
        this.devoluciones=devoluciones; this.control=control;
    }
    public Object ver(Long id) {
        return Map.of("factura",obtener(facturas,id),"detalles",detalles.findByFacturaIdOrderByIdAsc(id),
            "pagos",aplicaciones.findByFacturaId(id),"aplicacionesSaldo",aplicacionesSaldo.findByFacturaId(id),"devoluciones",devoluciones.findByFacturaId(id));
    }
    public Object crear(VentaRequest.Factura r) {
        moneda(r.moneda()); var despacho=obtener(despachos,r.despachoId()); var pedido=obtener(pedidos,despacho.getPedidoId());
        exigir(!r.fechaEmision().isBefore(despacho.getFechaDespacho()) && !r.fechaEmision().isAfter(hoy()),"Fecha de factura fuera del periodo entre despacho y hoy");
        if (pedido.getFechaInicioCredito()!=null) exigir(!r.fechaEmision().isBefore(pedido.getFechaInicioCredito()),"Factura anterior al inicio de crédito del pedido");
        if (pedido.getFechaInicioCredito()==null) {
            pedido.setFechaInicioCredito(r.fechaEmision()); pedido.setFechaVencimiento(r.fechaEmision().plusDays(pedido.getCredito()?pedido.getDiasCredito():0));
        }
        var factura=new Factura(); factura.setPedidoId(pedido.getId()); factura.setDespachoId(despacho.getId());
        factura.setClienteId(pedido.getClienteId()); factura.setClienteNombre(pedido.getClienteNombre()); factura.setClienteDocumento(pedido.getClienteDocumento());
        factura.setVendedorId(pedido.getVendedorId()); factura.setMoneda(r.moneda()); factura.setFechaEmision(r.fechaEmision()); factura.setFechaVencimiento(pedido.getFechaVencimiento());
        factura.setBaseImponible(CERO); factura.setIgv(CERO); factura.setTotal(CERO); factura.setSaldo(CERO); facturas.saveAndFlush(factura);
        Set<Long> unicos=new HashSet<>();
        for (var item:r.detalles()) {
            exigir(unicos.add(item.detalleId()),"Detalle repetido"); var dd=obtener(despachados,item.detalleId());
            exigir(dd.getDespachoId().equals(despacho.getId()) && dd.getCantidad()-dd.getFacturada()>=item.cantidad(),"Cantidad no despachada o ya facturada");
            var linea=obtener(lineas,dd.getDetallePedidoId()); exigir(linea.getMoneda().equals(r.moneda()),"Separe documentos por moneda");
            var acumulado=detalles.findByDetallePedidoId(linea.getId());
            var baseAnterior=acumulado.stream().map(DetalleFactura::getBaseImponible).reduce(CERO,BigDecimal::add);
            var igvAnterior=acumulado.stream().map(DetalleFactura::getIgv).reduce(CERO,BigDecimal::add);
            var baseAcumulada=base(linea.getPrecioSinIgv(),linea.getFacturada()+item.cantidad());
            var base=baseAcumulada.subtract(baseAnterior);
            var impuesto=dinero(baseAcumulada.multiply(pedido.getTasaIgv())).subtract(igvAnterior);
            var d=new DetalleFactura(); d.setFacturaId(factura.getId()); d.setDetalleDespachoId(dd.getId()); d.setDetallePedidoId(linea.getId());
            d.setCantidad(item.cantidad()); d.setBaseImponible(base); d.setIgv(impuesto); d.setTotal(base.add(impuesto)); detalles.save(d);
            dd.setFacturada(dd.getFacturada()+item.cantidad()); linea.setFacturada(linea.getFacturada()+item.cantidad());
            factura.setBaseImponible(factura.getBaseImponible().add(base)); factura.setIgv(factura.getIgv().add(impuesto));
        }
        factura.setTotal(factura.getBaseImponible().add(factura.getIgv())); factura.setSaldo(factura.getTotal());
        pagos.consumirFondos(factura); detalles.flush(); return ver(factura.getId());
    }
    public Object documento(Long id,VentaRequest.Documento r) {
        var f=obtener(facturas,id); exigir(f.getNumeroOficial()==null,"Documento oficial ya registrado");
        exigir(!facturas.existsBySerieOficialAndNumeroOficial(r.serie(),r.numero()),"Factura oficial duplicada");
        f.setSerieOficial(r.serie()); f.setNumeroOficial(r.numero()); f.setArchivoOficial(r.archivo()); return f;
    }
    public Object guia(VentaRequest.Guia r) {
        var d=obtener(despachos,r.despachoId()); exigir(!r.fechaTraslado().isBefore(d.getFechaDespacho()),"Traslado anterior al despacho");
        exigir(!guias.existsByDespachoId(d.getId()),"Despacho ya tiene guía"); var g=new GuiaRemision();
        g.setDespachoId(d.getId()); g.setFechaTraslado(r.fechaTraslado()); return guias.saveAndFlush(g);
    }
    public Object documentoGuia(Long id,VentaRequest.Documento r) {
        var g=obtener(guias,id); exigir(g.getNumeroOficial()==null,"Documento oficial ya registrado");
        exigir(!guias.existsBySerieOficialAndNumeroOficial(r.serie(),r.numero()),"Guía oficial duplicada");
        g.setSerieOficial(r.serie()); g.setNumeroOficial(r.numero()); g.setArchivoOficial(r.archivo()); return g;
    }
    public Object transferir(Long id,VentaRequest.Transferencia r) {
        var f=obtener(facturas,id); exigir(f.getSaldo().signum()>0,"Factura sin deuda pendiente");
        exigir(!f.getVendedorId().equals(r.vendedorNuevoId()),"La deuda ya pertenece al vendedor");
        creditos.validar(r.vendedorNuevoId(),Map.of(f.getMoneda(),f.getSaldo()),r.motivoExcepcion());
        var t=new TransferenciaDeuda(); t.setFacturaId(id); t.setVendedorAnteriorId(f.getVendedorId()); t.setVendedorNuevoId(r.vendedorNuevoId());
        t.setSaldoTransferido(f.getSaldo()); t.setMotivo(r.motivo()); transferencias.save(t); f.setVendedorId(r.vendedorNuevoId());
        control.evento("TRANSFERIR_DEUDA",id.toString(),r.motivo()); return ver(id);
    }
}
