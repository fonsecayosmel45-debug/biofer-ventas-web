package com.biofer.ventas.controller;
import com.biofer.ventas.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.core.io.Resource;
import java.io.IOException;

@RestController @RequestMapping("/api")
public class DocumentoController {
    private final DocumentoPdfService pdf;
    private final ArchivoService archivos;
    public DocumentoController(DocumentoPdfService pdf,ArchivoService archivos) { this.pdf=pdf; this.archivos=archivos; }
    @GetMapping(value="/facturas/{id}/pdf",produces="application/pdf") public ResponseEntity<byte[]> factura(@PathVariable Long id) throws IOException { return descargar(pdf.factura(id),"factura-interna-"+id+".pdf"); }
    @GetMapping(value="/guias-remision/{id}/pdf",produces="application/pdf") public ResponseEntity<byte[]> guia(@PathVariable Long id) throws IOException { return descargar(pdf.guia(id),"guia-interna-"+id+".pdf"); }
    private ResponseEntity<byte[]> descargar(byte[] bytes,String nombre) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+nombre+"\"").body(bytes);
    }
    @PostMapping(value="/archivos",consumes="multipart/form-data") public Object subir(@RequestParam MultipartFile archivo) throws IOException { return archivos.guardar(archivo); }
    @GetMapping("/archivos/{id}") public ResponseEntity<Resource> archivo(@PathVariable String id) {
        var recurso=archivos.obtener(id); String tipo=id.endsWith("pdf")?"application/pdf":id.endsWith("png")?"image/png":"image/jpeg";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(tipo)).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+id+"\"").body(recurso);
    }
}
