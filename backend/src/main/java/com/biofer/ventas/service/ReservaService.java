package com.biofer.ventas.service;

import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.stereotype.Service;
import static com.biofer.ventas.service.ReglasVenta.*;

/** Invoked inside the transaction holding ControlConcurrencia. */
@Service
public class ReservaService {
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final PresentacionRepository presentaciones;
    public ReservaService(PedidoRepository pedidos,DetallePedidoRepository detalles,PresentacionRepository presentaciones) {
        this.pedidos=pedidos; this.detalles=detalles; this.presentaciones=presentaciones;
    }
    public void reasignar() {
        for (var pedido:pedidos.findByEstadoOrderByFechaAprobacionAscIdAsc("APROBADO")) {
            for (var linea:detalles.findByPedidoIdOrderByIdAsc(pedido.getId())) {
                int faltante=linea.getCantidad()-linea.getCerrada()-linea.getDespachada()-linea.getReservada();
                if (faltante<=0) continue;
                var p=presentaciones.buscarParaActualizar(linea.getPresentacionId()).orElseThrow();
                if (!p.isActiva() || !p.getProducto().isActivo()) continue;
                int reserva=Math.min(faltante,p.getStockActual()-p.getStockReservado());
                exigir(reserva>=0,"Stock reservado inconsistente");
                p.setStockReservado(Math.addExact(p.getStockReservado(),reserva));
                linea.setReservada(Math.addExact(linea.getReservada(),reserva));
            }
        }
    }
    public void liberar(Pedido pedido, boolean cerrar) {
        for (var linea:detalles.findByPedidoIdOrderByIdAsc(pedido.getId())) {
            var p=presentaciones.buscarParaActualizar(linea.getPresentacionId()).orElseThrow();
            p.setStockReservado(p.getStockReservado()-linea.getReservada()); linea.setReservada(0);
            if (cerrar) linea.setCerrada(linea.getCantidad()-linea.getDespachada());
        }
    }
}
