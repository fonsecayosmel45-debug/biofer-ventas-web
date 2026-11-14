package com.biofer.ventas.service;

import com.biofer.ventas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import java.io.*;
import java.util.*;
import static com.biofer.ventas.service.ReglasVenta.*;

@Service @Transactional(readOnly=true)
public class DocumentoPdfService {
    private final FacturaRepository facturas;
    private final DetalleFacturaRepository facturados;
    private final GuiaRemisionRepository guias;
    private final DespachoRepository despachos;
    private final DetalleDespachoRepository despachados;
    private final DetallePedidoRepository lineas;
    public DocumentoPdfService(FacturaRepository facturas,DetalleFacturaRepository facturados,GuiaRemisionRepository guias,
        DespachoRepository despachos,DetalleDespachoRepository despachados,DetallePedidoRepository lineas) {
        this.facturas=facturas; this.facturados=facturados; this.guias=guias; this.despachos=despachos; this.despachados=despachados; this.lineas=lineas;
    }
    public byte[] factura(Long id) throws IOException {
        var f=obtener(facturas,id); List<String> texto=new ArrayList<>();
        texto.add("FACTURA INTERNA FAC-"+String.format("%06d",id));
        texto.add("Pedido: "+f.getPedidoId()+" / Despacho: "+f.getDespachoId());
        texto.add("Cliente: "+f.getClienteNombre()); texto.add("Documento del cliente: "+f.getClienteDocumento());
        texto.add("Emisión: "+f.getFechaEmision()+" / Vencimiento: "+f.getFechaVencimiento());
        texto.add("Moneda: "+f.getMoneda()); texto.add("");
        for (var d:facturados.findByFacturaIdOrderByIdAsc(id)) {
            var l=obtener(lineas,d.getDetallePedidoId());
            texto.add(l.getCodigo()+" | "+l.getDescripcion());
            texto.add("Cantidad: "+d.getCantidad()+" | Precio sin IGV: "+l.getPrecioSinIgv()+" | Total: "+d.getTotal()+(l.getBonificacion()?" | BONIFICACIÓN GRATUITA":""));
        }
        texto.add(""); texto.add("Base: "+f.getBaseImponible()+" / IGV: "+f.getIgv()+" / TOTAL: "+f.getTotal()+" "+f.getMoneda());
        texto.add("Referencia oficial: "+Objects.toString(f.getSerieOficial(),"pendiente")+"-"+Objects.toString(f.getNumeroOficial(),"pendiente"));
        return renderizar(texto);
    }
    public byte[] guia(Long id) throws IOException {
        var g=obtener(guias,id); var d=obtener(despachos,g.getDespachoId()); List<String> texto=new ArrayList<>();
        texto.add("GUÍA INTERNA GR-"+String.format("%06d",id));
        texto.add("Pedido: "+d.getPedidoId()+" / Despacho: "+d.getId()+" / Traslado: "+g.getFechaTraslado());
        texto.add("Destinatario: "+d.getDestinatario()+" / Documento: "+Objects.toString(d.getDocumentoDestinatario(),""));
        texto.add("Dirección: "+d.getDireccion()); texto.add("Agencia: "+Objects.toString(d.getAgencia(),""));
        texto.add("Recoge: "+d.getRecoge()+" / Conductor: "+Objects.toString(d.getConductor(),"")+" / Vehículo: "+Objects.toString(d.getVehiculo(),""));
        texto.add("Envío: "+d.getCostoEnvio()+" "+d.getMonedaEnvio()+" / Paga: "+d.getPagaEnvio()); texto.add("");
        for (var item:despachados.findByDespachoIdOrderByIdAsc(d.getId())) {
            var l=obtener(lineas,item.getDetallePedidoId()); texto.add(l.getCodigo()+" | "+l.getDescripcion()+" | Cantidad: "+item.getCantidad());
        }
        texto.add("Referencia oficial: "+Objects.toString(g.getSerieOficial(),"pendiente")+"-"+Objects.toString(g.getNumeroOficial(),"pendiente")); return renderizar(texto);
    }
    private byte[] renderizar(List<String> entradas) throws IOException {
        List<String> renglones=new ArrayList<>();
        for (String entrada:entradas) {
            String texto=entrada.replaceAll("[\\p{Cntrl}]"," ").replaceAll("[^\\x20-\\x7E\\u00A0-\\u00FF]","?");
            while (texto.length()>92) { int corte=texto.lastIndexOf(' ',92); if(corte<1)corte=92; renglones.add(texto.substring(0,corte)); texto=texto.substring(corte).stripLeading(); }
            renglones.add(texto);
        }
        try (var doc=new PDDocument(); var salida=new ByteArrayOutputStream()) {
            var fuente=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            int pagina=0;
            for (int inicio=0;inicio<renglones.size();inicio+=42) {
                var hoja=new PDPage(); doc.addPage(hoja); pagina++;
                try (var contenido=new PDPageContentStream(doc,hoja)) {
                    contenido.beginText(); contenido.setFont(fuente,10); contenido.setLeading(16); contenido.newLineAtOffset(42,745);
                    contenido.showText("BIOFER - DOCUMENTO INTERNO - NO ES UN COMPROBANTE OFICIAL"); contenido.newLine(); contenido.newLine();
                    for (String linea:renglones.subList(inicio,Math.min(inicio+42,renglones.size()))) { contenido.showText(linea); contenido.newLine(); }
                    contenido.endText(); contenido.beginText(); contenido.newLineAtOffset(42,32); contenido.setFont(fuente,9); contenido.showText("Página "+pagina); contenido.endText();
                }
            }
            doc.save(salida); return salida.toByteArray();
        }
    }
}
