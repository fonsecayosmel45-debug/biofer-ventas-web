package com.biofer.ventas.service;

import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Supplier;
import static com.biofer.ventas.service.ReglasVenta.*;

@Service
public class ControlOperacionService {
    private final ControlConcurrenciaRepository control;
    private final OperacionIdempotenteRepository operaciones;
    private final EventoAuditoriaRepository eventos;
    private final JsonMapper json;
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    public ControlOperacionService(ControlConcurrenciaRepository control, OperacionIdempotenteRepository operaciones,
        EventoAuditoriaRepository eventos, JsonMapper json) {
        this.control=control; this.operaciones=operaciones; this.eventos=eventos; this.json=json;
    }
    /** Lock first, then inventory/debt rows. Same order in every financial workflow. */
    public void bloquear() { exigir(control.bloquear()!=null,"Control de concurrencia sin inicializar"); }
    @Transactional
    public Object ejecutar(String clave, String operacion, Object solicitud, Supplier<Object> accion) {
        exigir(clave!=null && clave.matches("[A-Za-z0-9_-]{8,150}"),"Envíe Idempotency-Key (8 a 150 letras, números, _ o -)");
        bloquear();
        String huella;
        try { huella=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(json.writeValueAsString(solicitud).getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("No se pudo identificar la solicitud",e); }
        var previa=operaciones.findByClave(clave);
        if (previa.isPresent()) {
            var o=previa.get();
            exigir(o.getOperacion().equals(operacion) && o.getHuella().equals(huella),"Clave ya usada para otra operación o contenido");
            return json.readerFor(Object.class).with(tools.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(o.getRespuesta());
        }
        Object respuesta=accion.get();
        entityManager.flush();
        var registro=new OperacionIdempotente(); registro.setClave(clave); registro.setOperacion(operacion);
        registro.setHuella(huella); registro.setRespuesta(json.writeValueAsString(respuesta)); operaciones.save(registro);
        evento(operacion,clave,"Operación confirmada");
        return respuesta;
    }
    public void evento(String operacion,String recurso,String motivo) {
        var evento=new EventoAuditoria(); evento.setOperacion(operacion); evento.setRecurso(recurso); evento.setMotivo(motivo); eventos.save(evento);
    }
}
