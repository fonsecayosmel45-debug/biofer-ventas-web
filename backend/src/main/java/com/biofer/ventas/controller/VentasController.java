package com.biofer.ventas.controller;

import com.biofer.ventas.dto.VentaRequest;
import com.biofer.ventas.service.*;
import com.biofer.ventas.repository.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Supplier;

@RestController @RequestMapping("/api")
public class VentasController {
    private final PedidoService pedidos;
    private final DespachoService despachos;
    private final FacturaService facturas;
    private final PagoService pagos;
    private final CreditoService creditos;
    private final DevolucionService devoluciones;
    private final ControlOperacionService control;
    private final DespachoRepository despachosRepo;
    private final FacturaRepository facturasRepo;
    private final PagoRepository pagosRepo;
    private final GuiaRemisionRepository guias;
    private final DevolucionRepository devolucionesRepo;
    private final CreditoVendedorRepository creditosRepo;
    public VentasController(PedidoService pedidos,DespachoService despachos,FacturaService facturas,PagoService pagos,
        CreditoService creditos,DevolucionService devoluciones,ControlOperacionService control,
        DespachoRepository despachosRepo,FacturaRepository facturasRepo,PagoRepository pagosRepo,
        GuiaRemisionRepository guias,DevolucionRepository devolucionesRepo,CreditoVendedorRepository creditosRepo) {
        this.pedidos=pedidos; this.despachos=despachos; this.facturas=facturas; this.pagos=pagos; this.creditos=creditos;
        this.devoluciones=devoluciones; this.control=control; this.despachosRepo=despachosRepo; this.facturasRepo=facturasRepo;
        this.pagosRepo=pagosRepo; this.guias=guias; this.devolucionesRepo=devolucionesRepo; this.creditosRepo=creditosRepo;
    }
    private Object ejecutar(String key,String operacion,Object request,Supplier<Object> accion) { return control.ejecutar(key,operacion,request,accion); }

    @GetMapping("/pedidos") public Object listarPedidos(Pageable pageable) { return pedidos.listar(pageable); }
    @GetMapping("/pedidos/{id}") public Object pedido(@PathVariable Long id) { return pedidos.ver(id); }
    @PostMapping("/pedidos") public Object crearPedido(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Pedido r) {
        return ejecutar(key,"CREAR_PEDIDO",r,()->pedidos.crear(r));
    }
    @PutMapping("/pedidos/{id}") public Object editarPedido(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Pedido r) {
        return ejecutar(key,"EDITAR_PEDIDO:"+id,r,()->pedidos.editar(id,r));
    }
    @PostMapping("/pedidos/{id}/aprobar") public Object aprobar(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Autorizacion r) {
        return ejecutar(key,"APROBAR_PEDIDO:"+id,r,()->pedidos.aprobar(id,r.motivoExcepcion()));
    }
    @PostMapping("/pedidos/{id}/anular") public Object anular(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Motivo r) {
        return ejecutar(key,"ANULAR_PEDIDO:"+id,r,()->pedidos.anular(id,r.motivo()));
    }
    @PostMapping("/pedidos/{id}/cerrar-pendientes") public Object cerrar(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Motivo r) {
        return ejecutar(key,"CERRAR_PEDIDO:"+id,r,()->pedidos.cerrar(id,r.motivo()));
    }
    @GetMapping("/despachos") public Object listarDespachos(Pageable pageable) { return despachosRepo.findAll(pageable); }
    @GetMapping("/despachos/{id}") public Object despacho(@PathVariable Long id) { return despachos.ver(id); }
    @PostMapping("/despachos") public Object crearDespacho(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Despacho r) {
        return ejecutar(key,"CREAR_DESPACHO",r,()->despachos.crear(r));
    }
    @GetMapping("/facturas") public Object listarFacturas(Pageable pageable) { return facturasRepo.findAll(pageable); }
    @GetMapping("/facturas/{id}") public Object factura(@PathVariable Long id) { return facturas.ver(id); }
    @PostMapping("/facturas") public Object crearFactura(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Factura r) {
        return ejecutar(key,"CREAR_FACTURA",r,()->facturas.crear(r));
    }
    @PostMapping("/facturas/{id}/documento-oficial") public Object documento(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Documento r) {
        return ejecutar(key,"DOCUMENTO_FACTURA:"+id,r,()->facturas.documento(id,r));
    }
    @PostMapping("/facturas/{id}/transferir-deuda") public Object transferir(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Transferencia r) {
        return ejecutar(key,"TRANSFERIR_DEUDA:"+id,r,()->facturas.transferir(id,r));
    }
    @GetMapping("/guias-remision") public Object listarGuias(Pageable pageable) { return guias.findAll(pageable); }
    @GetMapping("/guias-remision/{id}") public Object guia(@PathVariable Long id) { return ReglasVenta.obtener(guias,id); }
    @PostMapping("/guias-remision") public Object crearGuia(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Guia r) {
        return ejecutar(key,"CREAR_GUIA",r,()->facturas.guia(r));
    }
    @PostMapping("/guias-remision/{id}/documento-oficial") public Object documentoGuia(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Documento r) {
        return ejecutar(key,"DOCUMENTO_GUIA:"+id,r,()->facturas.documentoGuia(id,r));
    }
    @GetMapping("/creditos") public Object listarCreditos(Pageable pageable) { return creditosRepo.findAll(pageable); }
    @PutMapping("/creditos") public Object credito(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Credito r) {
        return ejecutar(key,"CONFIGURAR_CREDITO",r,()->creditos.guardar(r));
    }
    @GetMapping("/pagos") public Object listarPagos(Pageable pageable) { return pagosRepo.findAll(pageable); }
    @GetMapping("/pagos/{id}") public Object pago(@PathVariable Long id) { return pagos.ver(id); }
    @PostMapping("/pagos") public Object crearPago(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Pago r) {
        return ejecutar(key,"REGISTRAR_PAGO",r,()->pagos.registrar(r));
    }
    @PostMapping("/pagos/{id}/confirmar") public Object confirmar(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key) {
        return ejecutar(key,"CONFIRMAR_PAGO:"+id,id,()->pagos.verificar(id,true,null));
    }
    @PostMapping("/pagos/{id}/rechazar") public Object rechazar(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Motivo r) {
        return ejecutar(key,"RECHAZAR_PAGO:"+id,r,()->pagos.verificar(id,false,r.motivo()));
    }
    @PostMapping("/pagos/{id}/aplicaciones") public Object aplicar(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Aplicaciones r) {
        return ejecutar(key,"APLICAR_PAGO:"+id,r,()->pagos.aplicar(id,r));
    }
    @PostMapping("/pagos/{id}/revertir") public Object revertir(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Motivo r) {
        return ejecutar(key,"REVERTIR_PAGO:"+id,r,()->pagos.revertir(id,r.motivo()));
    }
    @GetMapping("/vendedores/{id}/cuenta-corriente") public Object cuenta(@PathVariable Long id) { return creditos.cuenta(id); }
    @GetMapping("/vendedores/{id}/saldos-favor") public Object saldos(@PathVariable Long id) { return pagos.saldos(id); }
    @PostMapping("/saldos-favor/{id}/aplicaciones") public Object aplicarSaldo(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Aplicaciones r) {
        return ejecutar(key,"APLICAR_SALDO:"+id,r,()->pagos.aplicarSaldo(id,r));
    }
    @GetMapping("/devoluciones") public Object listarDevoluciones(Pageable pageable) { return devolucionesRepo.findAll(pageable); }
    @GetMapping("/devoluciones/{id}") public Object devolucion(@PathVariable Long id) { return devoluciones.ver(id); }
    @PostMapping("/devoluciones") public Object crearDevolucion(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Devolucion r) {
        return ejecutar(key,"REGISTRAR_DEVOLUCION",r,()->devoluciones.crear(r));
    }
    @PostMapping("/devoluciones/{id}/reembolso") public Object reembolso(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody VentaRequest.Reembolso r) {
        return ejecutar(key,"REGISTRAR_REEMBOLSO:"+id,r,()->devoluciones.reembolsar(id,r));
    }
}
