package com.biofer.ventas.controller;

import com.biofer.ventas.dto.ComercialRequest;
import com.biofer.ventas.dto.ComercialResponse;
import com.biofer.ventas.service.ComercialService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComercialController {

    private final ComercialService service;

    public ComercialController(ComercialService service) {
        this.service = service;
    }

    @GetMapping("/vendedores")
    public Page<ComercialResponse.Vendedor> vendedores(
            @RequestParam(defaultValue = "") String busqueda,
            @PageableDefault(size = 10, sort = "id")
            Pageable pageable) {

        return service.listarVendedores(busqueda, pageable);
    }

    @GetMapping("/vendedores/{id}")
    public ComercialResponse.Vendedor vendedor(
            @PathVariable Long id) {

        return service.buscarVendedor(id);
    }

    @PostMapping("/vendedores")
    @ResponseStatus(HttpStatus.CREATED)
    public ComercialResponse.Vendedor crearVendedor(
            @Valid @RequestBody ComercialRequest.Vendedor request) {

        return service.guardarVendedor(null, request);
    }

    @PutMapping("/vendedores/{id}")
    public ComercialResponse.Vendedor editarVendedor(
            @PathVariable Long id,
            @Valid @RequestBody ComercialRequest.Vendedor request) {

        return service.guardarVendedor(id, request);
    }

    @DeleteMapping("/vendedores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarVendedor(@PathVariable Long id) {
        service.desactivarVendedor(id);
    }

    @GetMapping("/vendedores/{id}/cuentas-bancarias")
    public List<ComercialResponse.Cuenta> cuentas(
            @PathVariable Long id) {

        return service.listarCuentas(id);
    }

    @PostMapping("/vendedores/{id}/cuentas-bancarias")
    @ResponseStatus(HttpStatus.CREATED)
    public ComercialResponse.Cuenta crearCuenta(
            @PathVariable Long id,
            @Valid @RequestBody ComercialRequest.Cuenta request) {

        return service.guardarCuenta(id, null, request);
    }

    @PutMapping("/vendedores/{id}/cuentas-bancarias/{cuentaId}")
    public ComercialResponse.Cuenta editarCuenta(
            @PathVariable Long id,
            @PathVariable Long cuentaId,
            @Valid @RequestBody ComercialRequest.Cuenta request) {

        return service.guardarCuenta(id, cuentaId, request);
    }

    @GetMapping("/clientes")
    public Page<ComercialResponse.Cliente> clientes(
            @RequestParam(defaultValue = "") String busqueda,
            @PageableDefault(size = 10, sort = "id")
            Pageable pageable) {

        return service.listarClientes(busqueda, pageable);
    }

    @GetMapping("/clientes/{id}")
    public ComercialResponse.Cliente cliente(
            @PathVariable Long id) {

        return service.buscarCliente(id);
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ComercialResponse.Cliente crearCliente(
            @Valid @RequestBody ComercialRequest.Cliente request) {

        return service.guardarCliente(null, request);
    }

    @PutMapping("/clientes/{id}")
    public ComercialResponse.Cliente editarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ComercialRequest.Cliente request) {

        return service.guardarCliente(id, request);
    }

    @DeleteMapping("/clientes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarCliente(@PathVariable Long id) {
        service.desactivarCliente(id);
    }

    @GetMapping("/clientes/{id}/direcciones")
    public List<ComercialResponse.Direccion> direcciones(
            @PathVariable Long id) {

        return service.listarDirecciones(id);
    }

    @PostMapping("/clientes/{id}/direcciones")
    @ResponseStatus(HttpStatus.CREATED)
    public ComercialResponse.Direccion crearDireccion(
            @PathVariable Long id,
            @Valid @RequestBody ComercialRequest.Direccion request) {

        return service.guardarDireccion(id, null, request);
    }

    @PutMapping("/clientes/{id}/direcciones/{direccionId}")
    public ComercialResponse.Direccion editarDireccion(
            @PathVariable Long id,
            @PathVariable Long direccionId,
            @Valid @RequestBody ComercialRequest.Direccion request) {

        return service.guardarDireccion(id, direccionId, request);
    }

    @GetMapping("/clientes/{id}/historial-vendedores")
    public List<ComercialResponse.Asignacion> historial(
            @PathVariable Long id) {

        return service.historialCliente(id);
    }
}