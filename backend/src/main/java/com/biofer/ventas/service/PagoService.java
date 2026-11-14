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
public class PagoService {
    private final PagoRepository pagos;
    private final AplicacionPagoRepository aplicaciones;
    private final FondoPedidoRepository fondos;
    private final FacturaRepository facturas;
    private final VendedorRepository vendedores;
    private final SaldoFavorRepository saldos;
    private final AplicacionSaldoRepository aplicacionesSaldo;
    private final DevolucionRepository devoluciones;
    private final ControlOperacionService control;
    public PagoService(PagoRepository pagos,AplicacionPagoRepository aplicaciones,FondoPedidoRepository fondos,
        FacturaRepository facturas,VendedorRepository vendedores,SaldoFavorRepository saldos,
        AplicacionSaldoRepository aplicacionesSaldo,DevolucionRepository devoluciones,ControlOperacionService control) {
        this.pagos=pagos; this.aplicaciones=aplicaciones; this.fondos=fondos; this.facturas=facturas;
        this.vendedores=vendedores; this.saldos=saldos; this.aplicacionesSaldo=aplicacionesSaldo;
        this.devoluciones=devoluciones; this.control=control;
    }
    public Object registrar(VentaRequest.Pago r) {
        obtener(vendedores,r.vendedorId()); moneda(r.moneda()); exigir(!r.fechaPago().isAfter(hoy()),"Pago no puede tener fecha futura");
        var p=new Pago(); p.setVendedorId(r.vendedorId()); p.setMoneda(r.moneda()); p.setMonto(r.monto());
        p.setMetodo(r.metodo()); p.setReferencia(r.referencia().strip()); p.setComprobante(r.comprobante()); p.setFechaPago(r.fechaPago());
        return pagos.saveAndFlush(p);
    }
    public Object ver(Long id) {
        return Map.of("pago",obtener(pagos,id),"aplicaciones",aplicaciones.findByPagoId(id),"fondosPedidos",fondos.findByPagoId(id));
    }
    public Object verificar(Long id,boolean confirmar,String motivo) {
        var p=obtener(pagos,id); exigir(p.getEstado().equals("POR_COMPROBAR"),"Pago ya verificado");
        if (!confirmar) motivo(motivo);
        p.setEstado(confirmar?"RECIBIDO":"RECHAZADO"); p.setDisponible(confirmar?p.getMonto():CERO); p.setMotivo(motivo); return p;
    }
    public Object aplicar(Long id,VentaRequest.Aplicaciones r) {
        var pago=obtener(pagos,id); exigir(pago.getEstado().equals("RECIBIDO"),"Confirme primero el pago");
        Set<Long> unicos=new HashSet<>();
        for (var a:r.detalles()) {
            exigir(unicos.add(a.facturaId()),"Factura repetida"); var f=obtener(facturas,a.facturaId());
            validarAplicacion(pago.getVendedorId(),pago.getMoneda(),f,a.monto());
            exigir(pago.getDisponible().compareTo(a.monto())>=0,"Saldo disponible del pago insuficiente");
            pago.setDisponible(pago.getDisponible().subtract(a.monto())); aplicarInterno(pago,f,a.monto());
        }
        return ver(id);
    }
    private void validarAplicacion(Long vendedor,String moneda,Factura f,BigDecimal monto) {
        exigir(f.getVendedorId().equals(vendedor) && f.getMoneda().equals(moneda),"Deuda de otro vendedor o moneda");
        exigir(monto.signum()>0 && f.getSaldo().compareTo(monto)>=0,"Importe supera la deuda pendiente");
    }
    private void aplicarInterno(Pago p,Factura f,BigDecimal monto) {
        var a=new AplicacionPago(); a.setPagoId(p.getId()); a.setFacturaId(f.getId()); a.setMonto(monto); aplicaciones.save(a);
        f.setSaldo(f.getSaldo().subtract(monto));
    }
    /** Cash dispatches earmark confirmed money until the corresponding invoice exists. */
    public void apartar(Pedido pedido,String moneda,BigDecimal requerido) {
        var restante=requerido;
        for (var p:pagos.findByVendedorId(pedido.getVendedorId()).stream().sorted(Comparator.comparing(Pago::getId)).toList()) {
            if (!p.getEstado().equals("RECIBIDO") || !p.getMoneda().equals(moneda) || p.getDisponible().signum()==0) continue;
            var monto=p.getDisponible().min(restante); if (monto.signum()<=0) break;
            p.setDisponible(p.getDisponible().subtract(monto));
            var fondo=new FondoPedido(); fondo.setPedidoId(pedido.getId()); fondo.setPagoId(p.getId()); fondo.setMoneda(moneda); fondo.setDisponible(monto); fondos.save(fondo);
            restante=restante.subtract(monto);
        }
        exigir(restante.signum()==0,"Pedido al contado: registre y confirme fondos suficientes antes de despachar");
    }
    public void consumirFondos(Factura factura) {
        var pendiente=factura.getSaldo();
        for (var fondo:fondos.findByPedidoIdAndMoneda(factura.getPedidoId(),factura.getMoneda())) {
            var monto=fondo.getDisponible().min(pendiente); if (monto.signum()<=0) continue;
            var pago=obtener(pagos,fondo.getPagoId());
            exigir(pago.getEstado().equals("RECIBIDO"),"Fondo de un pago no confirmado");
            fondo.setDisponible(fondo.getDisponible().subtract(monto)); aplicarInterno(pago,factura,monto); pendiente=pendiente.subtract(monto);
        }
    }
    public Object revertir(Long id,String motivo) {
        motivo(motivo); var p=obtener(pagos,id); exigir(p.getEstado().equals("RECIBIDO"),"Solo se revierte un pago recibido");
        exigir(fondos.findByPagoId(id).stream().allMatch(f->f.getDisponible().signum()==0),"El pago respalda despachos pendientes de facturar");
        for (var a:aplicaciones.findByPagoId(id)) {
            if (a.getRevertida()) continue;
            var factura=obtener(facturas,a.getFacturaId());
            exigir(factura.getVendedorId().equals(p.getVendedorId()),"La deuda fue transferida: concilie antes de revertir");
            exigir(devoluciones.findByFacturaId(factura.getId()).stream().allMatch(d->d.getTratamiento().equals("REDUCIR_DEUDA")),"Factura con devolución monetaria: concilie antes de revertir");
            factura.setSaldo(factura.getSaldo().add(a.getMonto())); a.setRevertida(true);
        }
        p.setDisponible(CERO); p.setEstado("REVERTIDO"); p.setMotivo(motivo); control.evento("REVERTIR_PAGO",id.toString(),motivo); return ver(id);
    }
    public Object aplicarSaldo(Long id,VentaRequest.Aplicaciones r) {
        var saldo=obtener(saldos,id);
        for (var a:r.detalles()) {
            var f=obtener(facturas,a.facturaId()); validarAplicacion(saldo.getVendedorId(),saldo.getMoneda(),f,a.monto());
            exigir(saldo.getDisponible().compareTo(a.monto())>=0,"Saldo a favor insuficiente");
            saldo.setDisponible(saldo.getDisponible().subtract(a.monto())); f.setSaldo(f.getSaldo().subtract(a.monto()));
            var aplicacion=new AplicacionSaldo(); aplicacion.setFacturaId(f.getId()); aplicacion.setSaldoFavorId(id); aplicacion.setMonto(a.monto()); aplicacionesSaldo.save(aplicacion);
        }
        return saldo;
    }
    public Object saldos(Long vendedor) {
        obtener(vendedores,vendedor); Map<String,BigDecimal> totales=new TreeMap<>();
        var anticipos=pagos.findByVendedorId(vendedor).stream().filter(p->p.getEstado().equals("RECIBIDO")).toList();
        anticipos.forEach(p->totales.merge(p.getMoneda(),p.getDisponible(),BigDecimal::add));
        var creditos=saldos.findByVendedorId(vendedor); creditos.forEach(s->totales.merge(s.getMoneda(),s.getDisponible(),BigDecimal::add));
        return Map.of("porMoneda",totales,"pagosConSaldo",anticipos,"saldosDeDevoluciones",creditos);
    }
}
