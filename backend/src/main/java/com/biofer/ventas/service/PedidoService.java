package com.biofer.ventas.service;

import com.biofer.ventas.dto.VentaRequest;
import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static com.biofer.ventas.service.ReglasVenta.*;

@Service @Transactional(readOnly=true)
public class PedidoService {
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final ClienteRepository clientes;
    private final DireccionClienteRepository direcciones;
    private final PresentacionRepository presentaciones;
    private final DespachoRepository despachos;
    private final CreditoService credito;
    private final ReservaService reservas;
    private final ControlOperacionService control;
    public PedidoService(PedidoRepository pedidos,DetallePedidoRepository detalles,ClienteRepository clientes,
        DireccionClienteRepository direcciones,PresentacionRepository presentaciones,DespachoRepository despachos,
        CreditoService credito,ReservaService reservas,ControlOperacionService control) {
        this.pedidos=pedidos; this.detalles=detalles; this.clientes=clientes; this.direcciones=direcciones;
        this.presentaciones=presentaciones; this.despachos=despachos; this.credito=credito; this.reservas=reservas; this.control=control;
    }
    public Page<Pedido> listar(Pageable pageable) { return pedidos.findAll(pageable); }
    public Object ver(Long id) {
        var p=obtener(pedidos,id);
        return Map.of("pedido",p,"numero",String.format("PED-%06d",p.getId()),
            "detalles",detalles.findByPedidoIdOrderByIdAsc(id),"totalesPorMoneda",credito.totales(p),"despachos",despachos.findByPedidoIdOrderByIdAsc(id));
    }
    public Object crear(VentaRequest.Pedido r) { return guardar(new Pedido(),r); }
    public Object editar(Long id,VentaRequest.Pedido r) {
        var p=obtener(pedidos,id); editable(p); motivo(r.motivo());
        reservas.liberar(p,false); detalles.deleteAll(detalles.findByPedidoIdOrderByIdAsc(id)); detalles.flush();
        p.setEstado("BORRADOR"); p.setFechaAprobacion(null);
        control.evento("EDITAR_PEDIDO",id.toString(),r.motivo());
        var respuesta=guardar(p,r); reservas.reasignar(); return respuesta;
    }
    private Object guardar(Pedido p,VentaRequest.Pedido r) {
        var cliente=obtener(clientes,r.clienteId());
        exigir(cliente.isActivo() && cliente.getVendedor().isVigente(),"Cliente o vendedor inactivo");
        exigir(!r.credito() || r.diasCredito()>0,"Crédito requiere días mayores a cero");
        p.setClienteId(cliente.getId()); p.setVendedorId(cliente.getVendedor().getId());
        p.setClienteNombre(cliente.getRazonSocial()!=null?cliente.getRazonSocial():cliente.getNombres()+" "+cliente.getApellidos());
        p.setClienteDocumento(cliente.getTipoDocumento()+":"+cliente.getNumeroDocumento());
        p.setAgencia(cliente.getTransportePreferido()); p.setMetodoEnvio(cliente.getMetodoEnvio());
        var direccion=r.direccionId()!=null ? obtener(direcciones,r.direccionId()) : direcciones.findAll().stream()
            .filter(d->d.getCliente().getId().equals(cliente.getId()) && d.isActiva() && d.isPrincipal()).findFirst().orElse(null);
        if (direccion!=null) {
            exigir(direccion.isActiva() && direccion.getCliente().getId().equals(cliente.getId()),"Dirección ajena al cliente o inactiva");
            p.setDireccionEnvio(direccion.getDireccion()+", "+direccion.getDistrito()+", "+direccion.getProvincia()+", "+direccion.getDepartamento());
            p.setDestinatario(direccion.getDestinatario()); p.setDocumentoDestinatario(direccion.getDocumentoDestinatario());
        }
        p.setObservaciones(r.observaciones()); p.setCredito(r.credito()); p.setDiasCredito(r.diasCredito()); p.setTasaIgv(r.tasaIgv());
        pedidos.saveAndFlush(p);
        Set<String> usados=new HashSet<>();
        for (var linea:r.detalles()) {
            // A SKU may occur once as a paid line and once as a bonus.
            String identidad=linea.presentacionId()+":"+linea.bonificacion();
            exigir(usados.add(identidad),"Presentación repetida con la misma condición");
            var articulo=obtener(presentaciones,linea.presentacionId());
            exigir(articulo.isActiva() && articulo.getProducto().isActivo(),"Producto o presentación inactivos");
            moneda(linea.moneda());
            exigir(linea.bonificacion()?linea.precioSinIgv().signum()==0:linea.precioSinIgv().signum()>0,"Bonificación debe valer cero; venta debe tener precio positivo");
            var d=new DetallePedido(); d.setPedidoId(p.getId()); d.setPresentacionId(articulo.getId());
            d.setDescripcion(articulo.getProducto().getNombreComercial()+" "+articulo.getCapacidad()+" "+articulo.getUnidad());
            d.setCodigo(articulo.getCodigo()); d.setCantidad(linea.cantidad()); d.setPrecioSinIgv(linea.precioSinIgv());
            d.setMoneda(linea.moneda()); d.setBonificacion(linea.bonificacion()); detalles.save(d);
        }
        detalles.flush(); return ver(p.getId());
    }
    public Object aprobar(Long id,String excepcion) {
        var p=obtener(pedidos,id); exigir(p.getEstado().equals("BORRADOR"),"Solo se aprueban borradores");
        exigir(obtener(clientes,p.getClienteId()).isActivo(),"Cliente inactivo");
        if (p.getCredito()) credito.validar(p.getVendedorId(),credito.totales(p),excepcion);
        p.setEstado("APROBADO"); p.setFechaAprobacion(Instant.now()); pedidos.flush(); reservas.reasignar(); return ver(id);
    }
    public Object anular(Long id,String motivo) {
        motivo(motivo); var p=obtener(pedidos,id); editable(p); reservas.liberar(p,false);
        p.setEstado("ANULADO"); p.setMotivoCierre(motivo); reservas.reasignar(); control.evento("ANULAR_PEDIDO",id.toString(),motivo); return ver(id);
    }
    public Object cerrar(Long id,String motivo) {
        motivo(motivo); var p=obtener(pedidos,id); exigir(p.getEstado().equals("APROBADO"),"Pedido sin saldo abierto");
        reservas.liberar(p,true); p.setEstado("CERRADO"); p.setMotivoCierre(motivo); reservas.reasignar();
        control.evento("CERRAR_PENDIENTES",id.toString(),motivo); return ver(id);
    }
    private void editable(Pedido p) {
        exigir(Set.of("BORRADOR","APROBADO").contains(p.getEstado()) && despachos.findByPedidoIdOrderByIdAsc(p.getId()).isEmpty(),"Solo puede editar o anular antes del primer despacho");
    }
}
