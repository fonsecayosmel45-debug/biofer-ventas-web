package com.biofer.ventas.service;

import java.math.*;
import java.time.*;
import java.util.Currency;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ReglasVenta {
    private ReglasVenta() {}
    public static final BigDecimal CERO = new BigDecimal("0.00");
    public static LocalDate hoy() { return LocalDate.now(ZoneId.of("America/Lima")); }
    public static BigDecimal dinero(BigDecimal importe) { return importe.setScale(2,RoundingMode.HALF_UP); }
    public static BigDecimal base(BigDecimal precio, int cantidad) { return dinero(precio.multiply(BigDecimal.valueOf(cantidad))); }
    public static BigDecimal total(BigDecimal precio, int cantidad, BigDecimal tasa) {
        var base = base(precio,cantidad); return base.add(dinero(base.multiply(tasa)));
    }
    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new ResponseStatusException(HttpStatus.CONFLICT,mensaje);
    }
    public static void moneda(String codigo) {
        try { Currency.getInstance(codigo); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Moneda no reconocida"); }
    }
    public static void motivo(String motivo) { exigir(motivo != null && !motivo.isBlank(),"Indique el motivo"); }
    public static <T> T obtener(org.springframework.data.jpa.repository.JpaRepository<T,Long> repo, Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado: " + id));
    }
}
