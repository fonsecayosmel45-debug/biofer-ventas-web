package com.biofer.ventas.controller;
import com.biofer.ventas.model.TarifaComercial;
import com.biofer.ventas.repository.*;
import com.biofer.ventas.service.ControlOperacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import static com.biofer.ventas.service.ReglasVenta.*;

@RestController @RequestMapping("/api/tarifas")
public class TarifaController {
    public record Tarifa(Long vendedorId,Long clienteId,@NotNull @Positive Long presentacionId,
        @NotNull @DecimalMin("0.0001") @Digits(integer=12,fraction=4) BigDecimal precioSinIgv,
        @NotBlank @Pattern(regexp="[A-Z]{3}") String moneda,@NotNull LocalDate vigenteDesde,LocalDate vigenteHasta,
        @NotBlank @Size(max=500) String motivo) {}
    private final TarifaComercialRepository tarifas;
    private final VendedorRepository vendedores;
    private final ClienteRepository clientes;
    private final PresentacionRepository presentaciones;
    private final ControlOperacionService control;
    public TarifaController(TarifaComercialRepository tarifas,VendedorRepository vendedores,ClienteRepository clientes,
        PresentacionRepository presentaciones,ControlOperacionService control) {
        this.tarifas=tarifas; this.vendedores=vendedores; this.clientes=clientes; this.presentaciones=presentaciones; this.control=control;
    }
    @GetMapping public Object listar(@RequestParam(required=false) Long vendedorId,@RequestParam(required=false) Long clienteId,
        @RequestParam(required=false) Long presentacionId,@RequestParam(defaultValue="false") boolean soloVigentes) {
        return tarifas.findAll().stream().filter(t->vendedorId==null || vendedorId.equals(t.getVendedorId()))
            .filter(t->clienteId==null || clienteId.equals(t.getClienteId())).filter(t->presentacionId==null || presentacionId.equals(t.getPresentacionId()))
            .filter(t->!soloVigentes || t.getActiva() && !t.getVigenteDesde().isAfter(hoy()) && (t.getVigenteHasta()==null || !t.getVigenteHasta().isBefore(hoy()))).toList();
    }
    @PostMapping public Object crear(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody Tarifa r) {
        return control.ejecutar(key,"CREAR_TARIFA",r,()->{
            exigir((r.vendedorId()==null)!=(r.clienteId()==null),"Seleccione vendedor o cliente para la tarifa");
            if(r.vendedorId()!=null)obtener(vendedores,r.vendedorId()); if(r.clienteId()!=null)obtener(clientes,r.clienteId());
            obtener(presentaciones,r.presentacionId()); moneda(r.moneda());
            exigir(r.vigenteHasta()==null || !r.vigenteHasta().isBefore(r.vigenteDesde()),"Periodo de vigencia inválido");
            var t=new TarifaComercial(); t.setVendedorId(r.vendedorId()); t.setClienteId(r.clienteId()); t.setPresentacionId(r.presentacionId());
            t.setPrecioSinIgv(r.precioSinIgv()); t.setMoneda(r.moneda()); t.setVigenteDesde(r.vigenteDesde()); t.setVigenteHasta(r.vigenteHasta()); t.setMotivo(r.motivo()); return tarifas.saveAndFlush(t);
        });
    }
    @PostMapping("/{id}/desactivar") public Object desactivar(@PathVariable Long id,@RequestHeader("Idempotency-Key") String key,
        @Valid @RequestBody com.biofer.ventas.dto.VentaRequest.Motivo r) {
        return control.ejecutar(key,"DESACTIVAR_TARIFA:"+id,r,()->{var t=obtener(tarifas,id); t.setActiva(false);
            control.evento("DESACTIVAR_TARIFA",id.toString(),r.motivo()); return t;});
    }
}
