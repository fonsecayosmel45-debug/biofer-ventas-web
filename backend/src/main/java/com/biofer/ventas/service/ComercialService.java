package com.biofer.ventas.service;

import com.biofer.ventas.dto.ComercialRequest;
import com.biofer.ventas.dto.ComercialResponse;
import com.biofer.ventas.model.*;
import com.biofer.ventas.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Currency;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ComercialService {

    private final VendedorRepository vendedores;
    private final ClienteRepository clientes;
    private final CuentaBancariaVendedorRepository cuentas;
    private final DireccionClienteRepository direcciones;
    private final HistorialAsignacionClienteRepository historial;

    public ComercialService(
            VendedorRepository vendedores,
            ClienteRepository clientes,
            CuentaBancariaVendedorRepository cuentas,
            DireccionClienteRepository direcciones,
            HistorialAsignacionClienteRepository historial) {

        this.vendedores = vendedores;
        this.clientes = clientes;
        this.cuentas = cuentas;
        this.direcciones = direcciones;
        this.historial = historial;
    }

    public Page<ComercialResponse.Vendedor> listarVendedores(
            String busqueda,
            Pageable pageable) {

        return vendedores.buscar(texto(busqueda), pageable)
                .map(this::respuestaVendedor);
    }

    public ComercialResponse.Vendedor buscarVendedor(Long id) {
        return respuestaVendedor(obtenerVendedor(id));
    }

    @Transactional
    public ComercialResponse.Vendedor guardarVendedor(
            Long id,
            ComercialRequest.Vendedor request) {

        validarPersona(request.datos());
        validarMoneda(request.monedaPago());

        String documento = texto(
            request.datos().numeroDocumento()
        );

        boolean repetido = id == null
                ? vendedores.existsByTipoDocumentoAndNumeroDocumento(
                    request.datos().tipoDocumento(), documento)
                : vendedores.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
                    request.datos().tipoDocumento(), documento, id);

        if (repetido) {
            throw conflicto("Ya existe un vendedor con ese documento");
        }

        if (request.datos().tipoPersona() == TipoPersona.JURIDICA
                && request.fechaNacimiento() != null) {
            throw conflicto(
                "La fecha de nacimiento corresponde a personas naturales"
            );
        }

        Vendedor vendedor = id == null
                ? new Vendedor()
                : obtenerVendedor(id);

        asignarPersona(vendedor, request.datos());

        vendedor.setFechaNacimiento(request.fechaNacimiento());
        vendedor.setTelefonoAlternativo(
            textoNullable(request.telefonoAlternativo())
        );
        vendedor.setDireccion(textoNullable(request.direccion()));
        vendedor.setDepartamento(
            textoNullable(request.departamento())
        );
        vendedor.setProvincia(textoNullable(request.provincia()));
        vendedor.setCiudad(textoNullable(request.ciudad()));
        vendedor.setMonedaPago(request.monedaPago());
        vendedor.setFechaCese(request.fechaCese());

        return respuestaVendedor(
            vendedores.saveAndFlush(vendedor)
        );
    }

    @Transactional
    public void desactivarVendedor(Long id) {
        Vendedor vendedor = obtenerVendedor(id);
        vendedor.setActivo(false);
        vendedores.save(vendedor);
    }

    public List<ComercialResponse.Cuenta> listarCuentas(Long vendedorId) {
        obtenerVendedor(vendedorId);

        return cuentas.listarPorVendedor(vendedorId).stream()
                .map(this::respuestaCuenta)
                .toList();
    }

    @Transactional
    public ComercialResponse.Cuenta guardarCuenta(
            Long vendedorId,
            Long cuentaId,
            ComercialRequest.Cuenta request) {

        Vendedor vendedor = obtenerVendedor(vendedorId);
        validarMoneda(request.moneda());

        CuentaBancariaVendedor cuenta = cuentaId == null
                ? new CuentaBancariaVendedor()
                : cuentas.findById(cuentaId)
                    .orElseThrow(() -> noEncontrado("Cuenta bancaria"));

        if (cuentaId != null
                && !cuenta.getVendedor().getId().equals(vendedorId)) {
            throw conflicto("La cuenta pertenece a otro vendedor");
        }

        cuenta.setVendedor(vendedor);
        cuenta.setBanco(texto(request.banco()).toUpperCase(
            java.util.Locale.ROOT
        ));
        cuenta.setNumeroCuenta(texto(request.numeroCuenta()));
        cuenta.setCci(request.cci());
        cuenta.setTitular(texto(request.titular()));
        cuenta.setMoneda(request.moneda());
        cuenta.setActiva(request.activa());

        return respuestaCuenta(cuentas.saveAndFlush(cuenta));
    }

    public Page<ComercialResponse.Cliente> listarClientes(
            String busqueda,
            Pageable pageable) {

        return clientes.buscar(texto(busqueda), pageable)
                .map(this::respuestaCliente);
    }

    public ComercialResponse.Cliente buscarCliente(Long id) {
        return respuestaCliente(obtenerCliente(id));
    }

    @Transactional
    public ComercialResponse.Cliente guardarCliente(
            Long id,
            ComercialRequest.Cliente request) {

        validarPersona(request.datos());

        String documento = texto(
            request.datos().numeroDocumento()
        );

        boolean repetido = id == null
                ? clientes.existsByTipoDocumentoAndNumeroDocumento(
                    request.datos().tipoDocumento(), documento)
                : clientes.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
                    request.datos().tipoDocumento(), documento, id);

        if (repetido) {
            throw conflicto("Ya existe un cliente con ese documento");
        }

        Vendedor nuevo = obtenerVendedor(request.vendedorId());

        if (!nuevo.isVigente()) {
            throw conflicto("El vendedor no está vigente");
        }

        Cliente cliente = id == null
                ? new Cliente()
                : bloquearCliente(id);

        Vendedor anterior = cliente.getVendedor();

        boolean cambiaVendedor = anterior != null
                && !anterior.getId().equals(nuevo.getId());

        if (cambiaVendedor
                && texto(request.motivoCambioVendedor()).isEmpty()) {
            throw conflicto("Indique el motivo del cambio de vendedor");
        }

        asignarPersona(cliente, request.datos());
        cliente.setGenero(textoNullable(request.genero()));
        cliente.setVendedor(nuevo);
        cliente.setMetodoEnvio(textoNullable(request.metodoEnvio()));
        cliente.setTransportePreferido(
            textoNullable(request.transportePreferido())
        );

        clientes.saveAndFlush(cliente);

        if (anterior == null || cambiaVendedor) {
            historial.save(new HistorialAsignacionCliente(
                cliente,
                anterior,
                nuevo,
                anterior == null
                    ? "Asignación inicial"
                    : request.motivoCambioVendedor().strip()
            ));
        }

        return respuestaCliente(cliente);
    }

    @Transactional
    public void desactivarCliente(Long id) {
        Cliente cliente = bloquearCliente(id);
        cliente.setActivo(false);
        clientes.save(cliente);
    }

    public List<ComercialResponse.Direccion> listarDirecciones(
            Long clienteId) {

        obtenerCliente(clienteId);

        return direcciones.listarPorCliente(clienteId).stream()
                .map(this::respuestaDireccion)
                .toList();
    }

    @Transactional
    public ComercialResponse.Direccion guardarDireccion(
            Long clienteId,
            Long direccionId,
            ComercialRequest.Direccion request) {

        Cliente cliente = bloquearCliente(clienteId);

        DireccionCliente direccion = direccionId == null
                ? new DireccionCliente()
                : direcciones.findById(direccionId)
                    .orElseThrow(() -> noEncontrado("Dirección"));

        if (direccionId != null
                && !direccion.getCliente().getId().equals(clienteId)) {
            throw conflicto("La dirección pertenece a otro cliente");
        }

        List<DireccionCliente> otras =
                direcciones.listarPorCliente(clienteId).stream()
                    .filter(d -> !Objects.equals(d.getId(), direccionId))
                    .toList();

        boolean hayOtraActiva = otras.stream()
                .anyMatch(DireccionCliente::isActiva);

        boolean hayOtraPrincipal = otras.stream()
                .anyMatch(d -> d.isActiva() && d.isPrincipal());

        if (!request.activa()
                && direccion.isPrincipal()
                && hayOtraActiva) {
            throw conflicto(
                "Seleccione otra dirección principal antes de desactivar esta"
            );
        }

        if (request.principal() && !request.activa()) {
            throw conflicto(
                "Una dirección principal debe estar activa"
            );
        }

        boolean principal = request.activa()
                && (request.principal() || !hayOtraPrincipal);

        if (principal) {
            otras.forEach(d -> d.setPrincipal(false));
        }

        direccion.setCliente(cliente);
        direccion.setDestinatario(texto(request.destinatario()));
        direccion.setDocumentoDestinatario(
            textoNullable(request.documentoDestinatario())
        );
        direccion.setDireccion(texto(request.direccion()));
        direccion.setDepartamento(texto(request.departamento()));
        direccion.setProvincia(texto(request.provincia()));
        direccion.setDistrito(texto(request.distrito()));
        direccion.setCiudad(textoNullable(request.ciudad()));
        direccion.setReferencia(textoNullable(request.referencia()));
        direccion.setPrincipal(principal);
        direccion.setActiva(request.activa());

        return respuestaDireccion(
            direcciones.saveAndFlush(direccion)
        );
    }

    public List<ComercialResponse.Asignacion> historialCliente(
            Long clienteId) {

        obtenerCliente(clienteId);

        return historial.listarPorCliente(clienteId).stream()
                .map(h -> new ComercialResponse.Asignacion(
                    h.getId(),
                    h.getVendedorAnterior() == null
                        ? null
                        : h.getVendedorAnterior().getId(),
                    h.getVendedorNuevo().getId(),
                    h.getMotivo(),
                    h.getFecha()
                ))
                .toList();
    }

    private void validarPersona(ComercialRequest.Persona datos) {
        if (datos.tipoPersona() == TipoPersona.NATURAL) {
            if (texto(datos.nombres()).isEmpty()
                    || texto(datos.apellidos()).isEmpty()) {
                throw conflicto(
                    "Una persona natural necesita nombres y apellidos"
                );
            }
        } else if (texto(datos.razonSocial()).isEmpty()) {
            throw conflicto(
                "Una persona jurídica necesita razón social"
            );
        }

        String documento = texto(datos.numeroDocumento());

        if (datos.tipoDocumento() == TipoDocumento.DNI
                && !documento.matches("[0-9]{8}")) {
            throw conflicto("El DNI debe tener 8 dígitos");
        }

        if (datos.tipoDocumento() == TipoDocumento.RUC
                && !documento.matches("[0-9]{11}")) {
            throw conflicto("El RUC debe tener 11 dígitos");
        }
    }

    private void validarMoneda(String moneda) {
        try {
            Currency.getInstance(moneda);
        } catch (IllegalArgumentException exception) {
            throw conflicto("Código de moneda no reconocido");
        }
    }

    private void asignarPersona(
            PersonaComercial entidad,
            ComercialRequest.Persona datos) {

        entidad.setTipoPersona(datos.tipoPersona());
        entidad.setNombres(textoNullable(datos.nombres()));
        entidad.setApellidos(textoNullable(datos.apellidos()));
        entidad.setRazonSocial(textoNullable(datos.razonSocial()));
        entidad.setNombreComercial(
            textoNullable(datos.nombreComercial())
        );
        entidad.setTipoDocumento(datos.tipoDocumento());
        entidad.setNumeroDocumento(texto(datos.numeroDocumento()));
        entidad.setTelefono(textoNullable(datos.telefono()));
        entidad.setEmail(textoNullable(datos.email()));
        entidad.setActivo(datos.activo());
    }

    private ComercialResponse.Persona respuestaPersona(
            PersonaComercial p) {

        return new ComercialResponse.Persona(
            p.getTipoPersona(),
            p.getNombres(),
            p.getApellidos(),
            p.getRazonSocial(),
            p.getNombreComercial(),
            p.getTipoDocumento(),
            p.getNumeroDocumento(),
            p.getTelefono(),
            p.getEmail(),
            p.isActivo()
        );
    }

    private ComercialResponse.Vendedor respuestaVendedor(Vendedor v) {
        return new ComercialResponse.Vendedor(
            v.getId(),
            "VEN%06d".formatted(v.getId()),
            respuestaPersona(v),
            v.getFechaNacimiento(),
            v.getTelefonoAlternativo(),
            v.getDireccion(),
            v.getDepartamento(),
            v.getProvincia(),
            v.getCiudad(),
            v.getMonedaPago(),
            v.getFechaCese(),
            v.isVigente(),
            v.getFechaCreacion(),
            v.getFechaActualizacion()
        );
    }

    private ComercialResponse.Cliente respuestaCliente(Cliente c) {
        Vendedor v = c.getVendedor();

        String nombre = v.getTipoPersona() == TipoPersona.JURIDICA
                ? v.getRazonSocial()
                : texto(v.getNombres()) + " " + texto(v.getApellidos());

        return new ComercialResponse.Cliente(
            c.getId(),
            "CLI%06d".formatted(c.getId()),
            respuestaPersona(c),
            c.getGenero(),
            v.getId(),
            nombre,
            c.getMetodoEnvio(),
            c.getTransportePreferido(),
            c.getFechaCreacion(),
            c.getFechaActualizacion()
        );
    }

    private ComercialResponse.Cuenta respuestaCuenta(
            CuentaBancariaVendedor c) {

        return new ComercialResponse.Cuenta(
            c.getId(),
            c.getVendedor().getId(),
            c.getBanco(),
            c.getNumeroCuenta(),
            c.getCci(),
            c.getTitular(),
            c.getMoneda(),
            c.isActiva()
        );
    }

    private ComercialResponse.Direccion respuestaDireccion(
            DireccionCliente d) {

        return new ComercialResponse.Direccion(
            d.getId(),
            d.getCliente().getId(),
            d.getDestinatario(),
            d.getDocumentoDestinatario(),
            d.getDireccion(),
            d.getDepartamento(),
            d.getProvincia(),
            d.getDistrito(),
            d.getCiudad(),
            d.getReferencia(),
            d.isPrincipal(),
            d.isActiva()
        );
    }

    private Vendedor obtenerVendedor(Long id) {
        return vendedores.findById(id)
                .orElseThrow(() -> noEncontrado("Vendedor"));
    }

    private Cliente obtenerCliente(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> noEncontrado("Cliente"));
    }

    private Cliente bloquearCliente(Long id) {
        return clientes.bloquear(id)
                .orElseThrow(() -> noEncontrado("Cliente"));
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.strip();
    }

    private String textoNullable(String valor) {
        String resultado = texto(valor);
        return resultado.isEmpty() ? null : resultado;
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(
            HttpStatus.CONFLICT,
            mensaje
        );
    }

    private ResponseStatusException noEncontrado(String entidad) {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            entidad + " no encontrado"
        );
    }
}