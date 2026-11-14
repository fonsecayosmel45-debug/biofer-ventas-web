package com.biofer.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import static com.biofer.ventas.service.ReglasVenta.*;

@Service
public class ArchivoService {
    private final Path carpeta;
    public ArchivoService(@Value("${biofer.archivos:../.local/archivos}") String ruta) { carpeta=Path.of(ruta).toAbsolutePath().normalize(); }
    public Object guardar(MultipartFile archivo) throws IOException {
        exigir(!archivo.isEmpty() && archivo.getSize()<=10*1024*1024,"Archivo vacío o mayor a 10 MB");
        byte[] bytes=archivo.getBytes();
        String extension;
        if (bytes.length>=5 && bytes[0]=='%' && bytes[1]=='P' && bytes[2]=='D' && bytes[3]=='F' && bytes[4]=='-') extension="pdf";
        else if (bytes.length>=8 && bytes[0]==(byte)137 && bytes[1]=='P' && bytes[2]=='N' && bytes[3]=='G'
            && bytes[4]==13 && bytes[5]==10 && bytes[6]==26 && bytes[7]==10) extension="png";
        else if (bytes.length>=3 && bytes[0]==(byte)255 && bytes[1]==(byte)216 && bytes[2]==(byte)255) extension="jpg";
        else throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Solo se aceptan PDF, PNG o JPEG");
        Files.createDirectories(carpeta); String id=UUID.randomUUID()+"."+extension;
        Files.write(carpeta.resolve(id),bytes,StandardOpenOption.CREATE_NEW);
        return Map.of("archivo",id,"nombreOriginal",Objects.toString(archivo.getOriginalFilename(),id),"bytes",bytes.length,"url","/api/archivos/"+id);
    }
    public Resource obtener(String id) {
        exigir(id.matches("[a-f0-9-]{36}\\.(pdf|png|jpg)"),"Identificador de archivo inválido");
        var ruta=carpeta.resolve(id).normalize(); exigir(ruta.startsWith(carpeta),"Ruta inválida");
        if (!Files.isRegularFile(ruta)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Archivo no encontrado");
        return new FileSystemResource(ruta);
    }
}
