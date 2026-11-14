package com.biofer.ventas.service;

import com.biofer.ventas.dto.VentaRequest;
import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.util.*;
import static com.biofer.ventas.service.ReglasVenta.*;

@Service @Transactional(readOnly=true)
public class DevolucionService {
    private final DevolucionRepository devoluciones;
    private final DetalleDevolucionRepository detalles;
    private final FacturaRepository facturas;
    private final DetalleFacturaRepository facturados;
    private final DetalleDespachoRepository despachados;
    private final DetallePedidoRepository lineas;
    private final PresentacionRepository presentaciones;
    private final MovimientoInventarioRepository movimientos;
    private final SaldoFavorRepository saldos;
    private final AplicacionPagoRepository aplicaciones;
    private final AplicacionSaldoRepository aplicacionesSaldo;
    private final ReservaService reservas;
    public DevolucionService(DevolucionRepository devoluciones,DetalleDevolucionRepository detalles,FacturaRepository facturas,
        DetalleFacturaRepository facturados,DetalleDespachoRepository despachados,DetallePedidoRepository lineas,
        PresentacionRepository presentaciones,MovimientoInventarioRepository movimientos,SaldoFavorRepository saldos,
        AplicacionPagoRepository aplicaciones,AplicacionSaldoRepository aplicacionesSaldo,ReservaService reservas) {
        this.devoluciones=devoluciones; this.detalles=detalles; this.facturas=facturas; this.facturados=facturados;
        this.despachados=despachados; this.lineas=lineas; this.presentaciones=presentaciones; this.movimientos=movimientos;
        this.saldos=saldos; this.aplicaciones=aplicaciones; this.aplicacionesSaldo=aplicacionesSaldo; this.reservas=reservas;
    }
    public Object ver(Long id) { return Map.of("devolucion",obtener(devoluciones,id),"detalles",detalles.findByDevolucionId(id)); }
    public Object crear(VentaRequest.Devolucion r) {
        var factura=obtener(facturas,r.facturaId()); var retorno=new Devolucion(); retorno.setFacturaId(factura.getId());
        retorno.setVendedorId(factura.getVendedorId()); retorno.setMoneda(factura.getMoneda()); retorno.setMotivo(r.motivo());
        retorno.setTratamiento(r.tratamiento()); retorno.setImporte(CERO); devoluciones.saveAndFlush(retorno);
        Set<Long> unicos=new HashSet<>();
        for (var item:r.detalles()) {
            exigir(unicos.add(item.detalleFacturaId()),"Detalle repetido"); var f=obtener(facturados,item.detalleFacturaId());
            exigir(f.getFacturaId().equals(factura.getId()) && f.getCantidad()-f.getDevuelta()>=item.cantidad(),"Cantidad no facturada o ya devuelta");
            exigir(item.apta()<=item.cantidad(),"Cantidad apta supera la devuelta");
            var linea=obtener(lineas,f.getDetallePedidoId()); var dd=obtener(despachados,f.getDetalleDespachoId());
            var importeAcumulado=dinero(f.getTotal().multiply(BigDecimal.valueOf(f.getDevuelta()+item.cantidad()))
                .divide(BigDecimal.valueOf(f.getCantidad()),12,RoundingMode.HALF_UP));
            var importe=importeAcumulado.subtract(f.getImporteDevuelto()); f.setDevuelta(f.getDevuelta()+item.cantidad()); f.setImporteDevuelto(importeAcumulado);
            dd.setDevuelta(dd.getDevuelta()+item.cantidad());
            var d=new DetalleDevolucion(); d.setDevolucionId(retorno.getId()); d.setDetalleFacturaId(f.getId()); d.setPresentacionId(linea.getPresentacionId());
            d.setCantidad(item.cantidad()); d.setApta(item.apta()); d.setDanada(item.cantidad()-item.apta()); d.setImporte(importe); detalles.save(d);
            if (item.apta()>0) {
                var p=presentaciones.buscarParaActualizar(linea.getPresentacionId()).orElseThrow(); int anterior=p.getStockActual();
                p.setStockActual(Math.addExact(anterior,item.apta()));
                movimientos.save(new MovimientoInventario(p,TipoMovimiento.ENTRADA,item.apta(),anterior,p.getStockActual(),"Devolución apta: "+r.motivo(),"DEV-"+retorno.getId()));
            }
            retorno.setImporte(retorno.getImporte().add(importe));
        }
        var importe=retorno.getImporte();
        if (r.tratamiento().equals("REDUCIR_DEUDA")) {
            exigir(factura.getSaldo().compareTo(importe)>=0,"Devolución supera la deuda; divida el tratamiento entre deuda y saldo/reembolso");
            factura.setSaldo(factura.getSaldo().subtract(importe));
        } else {
            var cobrado=aplicaciones.findByFacturaId(factura.getId()).stream().filter(a->!a.getRevertida()).map(AplicacionPago::getMonto).reduce(CERO,BigDecimal::add)
                .add(aplicacionesSaldo.findByFacturaId(factura.getId()).stream().map(AplicacionSaldo::getMonto).reduce(CERO,BigDecimal::add));
            var devuelto=devoluciones.findByFacturaId(factura.getId()).stream().filter(d->!d.getId().equals(retorno.getId()) && !d.getTratamiento().equals("REDUCIR_DEUDA"))
                .map(Devolucion::getImporte).reduce(CERO,BigDecimal::add);
            exigir(cobrado.subtract(devuelto).compareTo(importe)>=0,"No se puede devolver dinero que no fue cobrado");
            if (r.tratamiento().equals("SALDO_FAVOR")) {
                var s=new SaldoFavor(); s.setVendedorId(factura.getVendedorId()); s.setMoneda(factura.getMoneda()); s.setDisponible(importe); s.setDevolucionId(retorno.getId()); saldos.save(s);
            } else retorno.setEstadoReembolso("PENDIENTE");
        }
        reservas.reasignar(); detalles.flush(); return ver(retorno.getId());
    }
    public Object reembolsar(Long id,VentaRequest.Reembolso r) {
        var d=obtener(devoluciones,id); exigir("PENDIENTE".equals(d.getEstadoReembolso()),"No hay reembolso pendiente");
        d.setReferenciaReembolso(r.referencia()); d.setComprobanteReembolso(r.comprobante()); d.setEstadoReembolso("PAGADO"); return d;
    }
}
