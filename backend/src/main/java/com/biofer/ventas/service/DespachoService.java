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
public class DespachoService {
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository lineas;
    private final DespachoRepository despachos;
    private final DetalleDespachoRepository detalles;
    private final PresentacionRepository presentaciones;
    private final MovimientoInventarioRepository movimientos;
    private final CreditoService creditos;
    private final PagoService pagos;
    private final ReservaService reservas;
    public DespachoService(PedidoRepository pedidos,DetallePedidoRepository lineas,DespachoRepository despachos,
        DetalleDespachoRepository detalles,PresentacionRepository presentaciones,MovimientoInventarioRepository movimientos,
        CreditoService creditos,PagoService pagos,ReservaService reservas) {
        this.pedidos=pedidos; this.lineas=lineas; this.despachos=despachos; this.detalles=detalles;
        this.presentaciones=presentaciones; this.movimientos=movimientos; this.creditos=creditos; this.pagos=pagos; this.reservas=reservas;
    }
    public Object ver(Long id) { return Map.of("despacho",obtener(despachos,id),"detalles",detalles.findByDespachoIdOrderByIdAsc(id)); }
    public Object crear(VentaRequest.Despacho r) {
        var pedido=obtener(pedidos,r.pedidoId()); exigir(pedido.getEstado().equals("APROBADO"),"Pedido no aprobado o cerrado");
        exigir(!r.fechaDespacho().isAfter(hoy()),"Despacho no puede tener fecha futura"); moneda(r.monedaEnvio());
        reservas.reasignar();
        if (pedido.getCredito()) {
            Map<String,BigDecimal> incremento=new TreeMap<>(); creditos.totales(pedido).keySet().forEach(m->incremento.put(m,CERO));
            creditos.validar(pedido.getVendedorId(),incremento,r.motivoExcepcion());
        }
        var d=new Despacho(); d.setPedidoId(pedido.getId()); d.setFechaDespacho(r.fechaDespacho());
        d.setDestinatario(valor(r.destinatario(),pedido.getDestinatario()));
        d.setDocumentoDestinatario(valor(r.documentoDestinatario(),pedido.getDocumentoDestinatario()));
        d.setDireccion(valor(r.direccion(),pedido.getDireccionEnvio())); d.setAgencia(valor(r.agencia(),pedido.getAgencia()));
        exigir(d.getDestinatario()!=null && !d.getDestinatario().isBlank() && d.getDireccion()!=null && !d.getDireccion().isBlank(),"Complete destinatario y dirección de envío");
        exigir(d.getDocumentoDestinatario()!=null && !d.getDocumentoDestinatario().isBlank() && d.getAgencia()!=null && !d.getAgencia().isBlank(),"Complete documento del destinatario y agencia de transporte");
        d.setRecoge(r.recoge()); d.setCostoEnvio(r.costoEnvio()); d.setMonedaEnvio(r.monedaEnvio()); d.setPagaEnvio(r.pagaEnvio());
        d.setConductor(r.conductor()); d.setVehiculo(r.vehiculo()); d.setObservaciones(r.observaciones()); despachos.saveAndFlush(d);
        Set<Long> unicos=new HashSet<>(); Map<String,BigDecimal> cobros=new TreeMap<>();
        for (var item:r.detalles()) {
            exigir(unicos.add(item.detalleId()),"Detalle repetido"); var l=obtener(lineas,item.detalleId());
            exigir(l.getPedidoId().equals(pedido.getId()),"Detalle ajeno al pedido");
            exigir(item.cantidad()<=l.getReservada(),"Cantidad superior a la reservada; ingrese existencias primero");
            var p=presentaciones.buscarParaActualizar(l.getPresentacionId()).orElseThrow();
            exigir(p.isActiva() && p.getProducto().isActivo(),"Artículo inactivo");
            int anterior=p.getStockActual();
            exigir(anterior>=item.cantidad() && p.getStockReservado()>=item.cantidad(),"Existencia insuficiente");
            p.setStockActual(anterior-item.cantidad()); p.setStockReservado(p.getStockReservado()-item.cantidad());
            cobros.merge(l.getMoneda(),total(l.getPrecioSinIgv(),l.getDespachada()+item.cantidad(),pedido.getTasaIgv())
                .subtract(total(l.getPrecioSinIgv(),l.getDespachada(),pedido.getTasaIgv())),BigDecimal::add);
            l.setReservada(l.getReservada()-item.cantidad()); l.setDespachada(l.getDespachada()+item.cantidad());
            var detalle=new DetalleDespacho(); detalle.setDespachoId(d.getId()); detalle.setDetallePedidoId(l.getId()); detalle.setCantidad(item.cantidad()); detalles.save(detalle);
            movimientos.save(new MovimientoInventario(p,TipoMovimiento.SALIDA_VENTA,-item.cantidad(),anterior,p.getStockActual(),"Despacho confirmado","DES-"+d.getId()));
        }
        if (!pedido.getCredito()) cobros.forEach((m,i)->pagos.apartar(pedido,m,i));
        if (lineas.findByPedidoIdOrderByIdAsc(pedido.getId()).stream().allMatch(l->l.getDespachada()+l.getCerrada()==l.getCantidad())) pedido.setEstado("DESPACHADO");
        detalles.flush(); return ver(d.getId());
    }
    private String valor(String nuevo,String previo) { return nuevo==null || nuevo.isBlank()?previo:nuevo; }
}
