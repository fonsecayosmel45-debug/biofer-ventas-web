package com.biofer.ventas;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.*;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static com.biofer.ventas.service.ReglasVenta.hoy;

/** HTTP against a random local port and an in-memory H2 database only. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProcesoVentasTest {
    @LocalServerPort int port;
    @Autowired JsonMapper json;
    @Autowired com.biofer.ventas.repository.PedidoRepository pedidosRepo;
    @Autowired com.biofer.ventas.repository.FacturaRepository facturasRepo;
    @Autowired org.springframework.transaction.support.TransactionTemplate transacciones;
    private final HttpClient http=HttpClient.newHttpClient();
    private static final AtomicInteger secuencia=new AtomicInteger(10000000);
    private String token;
    private long vendedor,cliente,articulo;
    private String sku;

    @BeforeEach void preparar() throws Exception {
        token=solicitar("POST","/auth/login",Map.of("usuario","admin-pruebas","clave","ClavePruebas2026!"),200,null).get("token").asText();
        vendedor=vendedor();
        cliente=solicitar("POST","/clientes",Map.of("datos",persona(),"vendedorId",vendedor,"metodoEnvio","AGENCIA","transportePreferido","AGENCIA DEMO"),201).get("id").asLong();
        var cat=solicitar("POST","/categorias",Map.of("nombre","Categoría "+UUID.randomUUID(),"activa",true),201).get("id").asLong();
        var producto=solicitar("POST","/productos",Map.of("nombreComercial","Producto "+UUID.randomUUID(),"nombreProducto","Fertilizante","procedencia","NACIONAL","activo",true,"categoriasIds",List.of(cat)),201).get("id").asLong();
        sku="SKU-"+UUID.randomUUID().toString().substring(0,12);
        articulo=solicitar("POST","/presentaciones",Map.of("productoId",producto,"codigo",sku,"capacidad",1,"unidad","LT","precioSinIgv",100,"moneda","PEN","stockMinimo",2,"stockAlto",100,"activa",true),201).get("id").asLong();
        solicitar("PUT","/creditos",Map.of("vendedorId",vendedor,"moneda","PEN","limite",100000,"aprobado",true,"motivo","Prueba aislada"),200);
    }
    private Map<String,Object> persona() { return Map.of("tipoPersona","NATURAL","nombres","Persona","apellidos","Prueba","tipoDocumento","DNI","numeroDocumento",String.valueOf(secuencia.incrementAndGet()),"activo",true); }
    private long vendedor() throws Exception { return solicitar("POST","/vendedores",Map.of("datos",persona(),"monedaPago","PEN"),201).get("id").asLong(); }
    private JsonNode solicitar(String method,String path,Object body,int status) throws Exception { return solicitar(method,path,body,status,UUID.randomUUID().toString()); }
    private JsonNode solicitar(String method,String path,Object body,int status,String key) throws Exception {
        var respuesta=enviar(method,path,body,key);
        assertEquals(status,respuesta.statusCode(),method+" "+path+" => "+respuesta.body());
        return respuesta.body().isBlank()?json.readTree("{}"):json.readTree(respuesta.body());
    }
    private HttpResponse<String> enviar(String method,String path,Object body,String key) throws Exception {
        var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api"+path));
        if(token!=null)b.header("Authorization","Bearer "+token);
        if(key!=null)b.header("Idempotency-Key",key);
        if(body!=null)b.header("Content-Type","application/json");
        b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return http.send(b.build(),HttpResponse.BodyHandlers.ofString());
    }
    private void entrada(int cantidad) throws Exception { solicitar("POST","/presentaciones/"+articulo+"/movimientos",Map.of("tipo","ENTRADA","cantidad",cantidad,"motivo","Prueba aislada","referencia",UUID.randomUUID().toString()),201); }
    private Map<String,Object> pedidoBody(int cantidad,boolean credito,String precio) { return Map.of("clienteId",cliente,"credito",credito,"diasCredito",credito?30:0,"tasaIgv",new BigDecimal("0.18"),"detalles",List.of(Map.of("presentacionId",articulo,"cantidad",cantidad,"precioSinIgv",new BigDecimal(precio),"moneda","PEN","bonificacion",false))); }
    private long pedido(int cantidad,boolean credito) throws Exception { return solicitar("POST","/pedidos",pedidoBody(cantidad,credito,"100"),200).get("pedido").get("id").asLong(); }
    private void aprobar(long pedido) throws Exception { solicitar("POST","/pedidos/"+pedido+"/aprobar",Map.of(),200); }
    private JsonNode pedido(long id) throws Exception { return solicitar("GET","/pedidos/"+id,null,200); }
    private Map<String,Object> despachoBody(long pedido,int cantidad) throws Exception {
        return Map.of("pedidoId",pedido,"fechaDespacho",hoy().toString(),"destinatario","Cliente Prueba","documentoDestinatario","12345678","direccion","Dirección de prueba","recoge","Persona","costoEnvio",0,"monedaEnvio","PEN","pagaEnvio","CLIENTE","detalles",List.of(Map.of("detalleId",pedido(pedido).get("detalles").get(0).get("id").asLong(),"cantidad",cantidad)));
    }
    private long despacho(long pedido,int cantidad) throws Exception { return solicitar("POST","/despachos",despachoBody(pedido,cantidad),200).get("despacho").get("id").asLong(); }
    private JsonNode factura(long despacho,int cantidad) throws Exception {
        long detalle=solicitar("GET","/despachos/"+despacho,null,200).get("detalles").get(0).get("id").asLong();
        return solicitar("POST","/facturas",Map.of("despachoId",despacho,"moneda","PEN","fechaEmision",hoy().toString(),"detalles",List.of(Map.of("detalleId",detalle,"cantidad",cantidad))),200);
    }
    private long pago(String monto) throws Exception {
        long id=solicitar("POST","/pagos",Map.of("vendedorId",vendedor,"moneda","PEN","monto",new BigDecimal(monto),"metodo","TRANSFERENCIA","referencia",UUID.randomUUID().toString(),"comprobante","Referencia de prueba aislada","fechaPago",hoy().toString()),200).get("id").asLong();
        solicitar("POST","/pagos/"+id+"/confirmar",null,200); return id;
    }
    private JsonNode articulo() throws Exception { return solicitar("GET","/presentaciones/"+articulo,null,200); }
    private void importe(String esperado,JsonNode actual) { assertEquals(0,new BigDecimal(esperado).compareTo(actual.decimalValue()),actual.toString()); }

    @Test void reservaFifoFaltantesYDespachoParcial() throws Exception {
        entrada(5); long p1=pedido(7,true); aprobar(p1); long p2=pedido(4,true); aprobar(p2);
        solicitar("DELETE","/productos/"+articulo().get("productoId").asLong(),null,409);
        assertEquals(5,pedido(p1).get("detalles").get(0).get("reservada").asInt());
        assertEquals(0,pedido(p2).get("detalles").get(0).get("reservada").asInt());
        entrada(4); assertEquals(7,pedido(p1).get("detalles").get(0).get("reservada").asInt());
        assertEquals(2,pedido(p2).get("detalles").get(0).get("reservada").asInt());
        despacho(p1,3); assertEquals(6,articulo().get("stockActual").asInt()); assertEquals(6,articulo().get("stockReservado").asInt());
        solicitar("POST","/pedidos/"+p1+"/anular",Map.of("motivo","Prueba"),409);
        solicitar("POST","/pedidos/"+p1+"/cerrar-pendientes",Map.of("motivo","Cliente ya no requiere"),200);
        assertEquals(4,pedido(p2).get("detalles").get(0).get("reservada").asInt());
        solicitar("GET","/presentaciones/"+articulo+"/kardex",null,200);
        solicitar("GET","/presentaciones/"+articulo+"/precios",null,200);
    }
    @Test void facturasParcialesConservanVencimientoYNoDuplicanCupo() throws Exception {
        entrada(10); long p=pedido(10,true); aprobar(p);
        var f1=factura(despacho(p,4),4); var f2=factura(despacho(p,6),6);
        assertEquals(f1.get("factura").get("fechaVencimiento"),f2.get("factura").get("fechaVencimiento"));
        importe("472",f1.get("factura").get("total")); importe("708",f2.get("factura").get("total"));
        var cuenta=solicitar("GET","/vendedores/"+vendedor+"/cuenta-corriente",null,200);
        importe("0",cuenta.get("compromisosPorMoneda").get("PEN"));
    }
    @Test void contadoRequiereFondosYFalloRevierteStock() throws Exception {
        entrada(4); long p=pedido(4,false); aprobar(p); pago("100");
        solicitar("POST","/despachos",despachoBody(p,4),409);
        assertEquals(4,articulo().get("stockActual").asInt()); assertEquals(4,articulo().get("stockReservado").asInt());
        pago("372"); var factura=factura(despacho(p,4),4); importe("0",factura.get("factura").get("saldo"));
    }
    @Test void pagoParcialReversionYAdelanto() throws Exception {
        entrada(2); long p=pedido(2,true); aprobar(p); var f=factura(despacho(p,2),2); long id=f.get("factura").get("id").asLong();
        long pago=pago("300"); solicitar("POST","/pagos/"+pago+"/aplicaciones",Map.of("detalles",List.of(Map.of("facturaId",id,"monto",100))),200);
        importe("136",solicitar("GET","/facturas/"+id,null,200).get("factura").get("saldo"));
        importe("200",solicitar("GET","/pagos/"+pago,null,200).get("pago").get("disponible"));
        solicitar("POST","/pagos/"+pago+"/revertir",Map.of("motivo","Depósito incorrecto"),200);
        importe("236",solicitar("GET","/facturas/"+id,null,200).get("factura").get("saldo"));
    }
    @Test void devolucionDistingueAptoDanadoYEvitaDuplicados() throws Exception {
        entrada(4); long p=pedido(4,true); aprobar(p); var f=factura(despacho(p,4),4);
        var r=Map.of("facturaId",f.get("factura").get("id").asLong(),"tratamiento","REDUCIR_DEUDA","motivo","Prueba","detalles",List.of(Map.of("detalleFacturaId",f.get("detalles").get(0).get("id").asLong(),"cantidad",3,"apta",2)));
        solicitar("POST","/devoluciones",r,200); assertEquals(2,articulo().get("stockActual").asInt()); solicitar("POST","/devoluciones",r,409);
        assertEquals(2,articulo().get("stockActual").asInt());
    }
    @Test void idempotenciaReintentosYContenidoDiferente() throws Exception {
        String key=UUID.randomUUID().toString(); var r=pedidoBody(2,true,"100");
        var primero=solicitar("POST","/pedidos",r,200,key); var segundo=solicitar("POST","/pedidos",r,200,key);
        assertEquals(primero.get("pedido").get("id"),segundo.get("pedido").get("id"));
        solicitar("POST","/pedidos",pedidoBody(3,true,"100"),409,key);
    }
    @Test void creditoInsuficienteYExcepcionConMotivo() throws Exception {
        solicitar("PUT","/creditos",Map.of("vendedorId",vendedor,"moneda","PEN","limite",100,"aprobado",true,"motivo","Límite"),200);
        long p=pedido(2,true); solicitar("POST","/pedidos/"+p+"/aprobar",Map.of(),409);
        solicitar("POST","/pedidos/"+p+"/aprobar",Map.of("motivoExcepcion","Autorización excepcional"),200);
    }
    @Test void documentosPdfYTransferenciaConHistorial() throws Exception {
        entrada(2); long p=pedido(2,true); aprobar(p); long d=despacho(p,2); var f=factura(d,2); long id=f.get("factura").get("id").asLong();
        long guia=solicitar("POST","/guias-remision",Map.of("despachoId",d,"fechaTraslado",hoy().toString()),200).get("id").asLong();
        for (String ruta:List.of("/facturas/"+id+"/pdf","/guias-remision/"+guia+"/pdf")) {
            var respuesta=enviar("GET",ruta,null,null); assertEquals(200,respuesta.statusCode()); assertTrue(respuesta.body().startsWith("%PDF-"));
        }
        long nuevo=vendedor(); solicitar("POST","/facturas/"+id+"/transferir-deuda",Map.of("vendedorNuevoId",nuevo,"motivo","Reasignación","motivoExcepcion","Autorizado por administración"),200);
        assertEquals(nuevo,solicitar("GET","/facturas/"+id,null,200).get("factura").get("vendedorId").asLong());
        assertEquals(f.get("factura").get("fechaVencimiento"),solicitar("GET","/facturas/"+id,null,200).get("factura").get("fechaVencimiento"));
    }
    @Test void redondeoAcumuladoEnParciales() throws Exception {
        entrada(3); long p=solicitar("POST","/pedidos",pedidoBody(3,true,"0.028"),200).get("pedido").get("id").asLong(); aprobar(p);
        BigDecimal total=BigDecimal.ZERO;
        for(int i=0;i<3;i++)total=total.add(factura(despacho(p,1),1).get("factura").get("total").decimalValue());
        assertEquals(0,new BigDecimal("0.09").compareTo(total));
    }
    @Test void noAutenticadoNoPuedeConsultar() throws Exception {
        token=null; solicitar("GET","/pedidos",null,401);
    }

    @Test void dosDespachosConcurrentesNoVendenDosVeces() throws Exception {
        entrada(2); long p=pedido(2,true); aprobar(p); var body=despachoBody(p,2);
        var pool=Executors.newFixedThreadPool(2);
        try {
            var inicio=new CountDownLatch(1);
            Callable<Integer> accion=()->{inicio.await();return enviar("POST","/despachos",body,UUID.randomUUID().toString()).statusCode();};
            var a=pool.submit(accion); var b=pool.submit(accion); inicio.countDown();
            var estados=new ArrayList<>(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS))); Collections.sort(estados);
            assertEquals(List.of(200,409),estados); assertEquals(0,articulo().get("stockActual").asInt());
        } finally {pool.shutdownNow();}
    }

    @Test void bonificacionEsGratuitaYConsumeExistencia() throws Exception {
        entrada(3); var body=new HashMap<>(pedidoBody(2,true,"100"));
        body.put("detalles",List.of(Map.of("presentacionId",articulo,"cantidad",2,"precioSinIgv",100,"moneda","PEN","bonificacion",false),
            Map.of("presentacionId",articulo,"cantidad",1,"precioSinIgv",0,"moneda","PEN","bonificacion",true)));
        long p=solicitar("POST","/pedidos",body,200).get("pedido").get("id").asLong(); aprobar(p);
        var lineas=pedido(p).get("detalles"); var d=new HashMap<>(despachoBody(p,2));
        d.put("detalles",List.of(Map.of("detalleId",lineas.get(0).get("id").asLong(),"cantidad",2),Map.of("detalleId",lineas.get(1).get("id").asLong(),"cantidad",1)));
        long despacho=solicitar("POST","/despachos",d,200).get("despacho").get("id").asLong();
        var dd=solicitar("GET","/despachos/"+despacho,null,200).get("detalles");
        var f=solicitar("POST","/facturas",Map.of("despachoId",despacho,"moneda","PEN","fechaEmision",hoy().toString(),"detalles",List.of(Map.of("detalleId",dd.get(0).get("id").asLong(),"cantidad",2),Map.of("detalleId",dd.get(1).get("id").asLong(),"cantidad",1))),200);
        importe("236",f.get("factura").get("total")); assertEquals(0,articulo().get("stockActual").asInt());
    }

    @Test void saldoFavorPorDevolucionYReembolsoSinDobleBeneficio() throws Exception {
        entrada(4); long p=pedido(4,true); aprobar(p); var f=factura(despacho(p,4),4); long fid=f.get("factura").get("id").asLong();
        long pago=pago("472"); solicitar("POST","/pagos/"+pago+"/aplicaciones",Map.of("detalles",List.of(Map.of("facturaId",fid,"monto",472))),200);
        var retorno=new HashMap<String,Object>(); retorno.put("facturaId",fid); retorno.put("tratamiento","SALDO_FAVOR"); retorno.put("motivo","Prueba");
        retorno.put("detalles",List.of(Map.of("detalleFacturaId",f.get("detalles").get(0).get("id").asLong(),"cantidad",1,"apta",1)));
        solicitar("POST","/devoluciones",retorno,200);
        var saldos=solicitar("GET","/vendedores/"+vendedor+"/saldos-favor",null,200); importe("118",saldos.get("porMoneda").get("PEN"));
        long p2=pedido(1,true); aprobar(p2); var f2=factura(despacho(p2,1),1);
        solicitar("POST","/saldos-favor/"+saldos.get("saldosDeDevoluciones").get(0).get("id").asLong()+"/aplicaciones",Map.of("detalles",List.of(Map.of("facturaId",f2.get("factura").get("id").asLong(),"monto",118))),200);
        importe("0",solicitar("GET","/facturas/"+f2.get("factura").get("id").asLong(),null,200).get("factura").get("saldo"));
        retorno.put("tratamiento","REEMBOLSO"); var dev=solicitar("POST","/devoluciones",retorno,200);
        long did=dev.get("devolucion").get("id").asLong(); solicitar("POST","/devoluciones/"+did+"/reembolso",Map.of("referencia","Reembolso aislado","comprobante","Prueba"),200);
        solicitar("POST","/devoluciones/"+did+"/reembolso",Map.of("referencia","Duplicado","comprobante","Prueba"),409);
        solicitar("POST","/pagos/"+pago+"/revertir",Map.of("motivo","No debe permitirse"),409);
    }

    @Test void preciosCongeladosAunqueCambieCatalogo() throws Exception {
        long p=pedido(2,true); var a=articulo();
        solicitar("PUT","/presentaciones/"+articulo,Map.of("productoId",a.get("productoId").asLong(),"codigo",sku,"capacidad",1,"unidad","LT","precioSinIgv",150,"moneda","PEN","stockMinimo",2,"stockAlto",100,"activa",true,"motivoCambioPrecio","Nueva tarifa"),200);
        importe("100",pedido(p).get("detalles").get(0).get("precioSinIgv"));
        assertEquals(2,solicitar("GET","/presentaciones/"+articulo+"/precios",null,200).get("totalElements").asInt());
        var cambio=new HashMap<>(pedidoBody(2,true,"130")); cambio.put("motivo","Precio negociado");
        solicitar("PUT","/pedidos/"+p,cambio,200); importe("130",pedido(p).get("detalles").get(0).get("precioSinIgv"));
    }

    @Test void monedasSeparadasYTarifasSoloReferenciales() throws Exception {
        var a=articulo();
        long usd=solicitar("POST","/presentaciones",Map.of("productoId",a.get("productoId").asLong(),"codigo",sku+"-USD","capacidad",5,"unidad","LT","precioSinIgv",20,"moneda","USD","stockMinimo",0,"stockAlto",100,"activa",true),201).get("id").asLong();
        solicitar("POST","/presentaciones/"+usd+"/movimientos",Map.of("tipo","ENTRADA","cantidad",1,"motivo","Prueba"),201); entrada(1);
        solicitar("PUT","/creditos",Map.of("vendedorId",vendedor,"moneda","USD","limite",100,"aprobado",true,"motivo","Prueba"),200);
        solicitar("POST","/tarifas",Map.of("vendedorId",vendedor,"presentacionId",articulo,"precioSinIgv",90,"moneda","PEN","vigenteDesde",hoy().toString(),"motivo","Tarifa referencial"),200);
        var body=new HashMap<>(pedidoBody(1,true,"100"));
        body.put("detalles",List.of(Map.of("presentacionId",articulo,"cantidad",1,"precioSinIgv",100,"moneda","PEN","bonificacion",false),Map.of("presentacionId",usd,"cantidad",1,"precioSinIgv",20,"moneda","USD","bonificacion",false)));
        var pedido=solicitar("POST","/pedidos",body,200); long p=pedido.get("pedido").get("id").asLong();
        importe("118",pedido.get("totalesPorMoneda").get("PEN")); importe("23.60",pedido.get("totalesPorMoneda").get("USD")); aprobar(p);
        var lineas=pedido(p).get("detalles"); var despacho=new HashMap<>(despachoBody(p,1));
        despacho.put("detalles",List.of(Map.of("detalleId",lineas.get(0).get("id").asLong(),"cantidad",1),Map.of("detalleId",lineas.get(1).get("id").asLong(),"cantidad",1)));
        long d=solicitar("POST","/despachos",despacho,200).get("despacho").get("id").asLong(); var dd=solicitar("GET","/despachos/"+d,null,200).get("detalles");
        solicitar("POST","/facturas",Map.of("despachoId",d,"moneda","PEN","fechaEmision",hoy().toString(),"detalles",List.of(Map.of("detalleId",dd.get(1).get("id").asLong(),"cantidad",1))),409);
        for(int i=0;i<2;i++)solicitar("POST","/facturas",Map.of("despachoId",d,"moneda",i==0?"PEN":"USD","fechaEmision",hoy().toString(),"detalles",List.of(Map.of("detalleId",dd.get(i).get("id").asLong(),"cantidad",1))),200);
    }

    @Test void moraBloqueaYReporteCuentaDesdeInicioDelCredito() throws Exception {
        entrada(2); long p=pedido(2,true); aprobar(p); var f=factura(despacho(p,1),1); long fid=f.get("factura").get("id").asLong();
        // Historical dates are set directly only in this ephemeral H2 test database.
        transacciones.executeWithoutResult(t->{var pedido=pedidosRepo.findById(p).orElseThrow();
            pedido.setFechaInicioCredito(hoy().minusDays(40)); pedido.setFechaVencimiento(hoy().minusDays(10));
            var factura=facturasRepo.findById(fid).orElseThrow(); factura.setFechaEmision(hoy().minusDays(40)); factura.setFechaVencimiento(hoy().minusDays(10));});
        solicitar("POST","/despachos",despachoBody(p,1),409);
        var autorizado=new HashMap<>(despachoBody(p,1)); autorizado.put("motivoExcepcion","Despacho con mora autorizado");
        long d=solicitar("POST","/despachos",autorizado,200).get("despacho").get("id").asLong();
        assertEquals(hoy().minusDays(10).toString(),factura(d,1).get("factura").get("fechaVencimiento").asText());
        var alertas=solicitar("GET","/reportes/vencimientos?diasAviso=7",null,200); boolean encontrado=false;
        for(var alerta:alertas)if(alerta.get("factura").get("id").asLong()==fid) {
            assertEquals(40,alerta.get("diasDesdeInicioCredito").asInt()); assertEquals(10,alerta.get("diasMora").asInt()); encontrado=true;
        }
        assertTrue(encontrado);
    }

    @Test void devolucionMonetariaInvalidaRevierteEntradaDeStock() throws Exception {
        entrada(2); long p=pedido(2,true); aprobar(p); var f=factura(despacho(p,2),2);
        solicitar("POST","/devoluciones",Map.of("facturaId",f.get("factura").get("id").asLong(),"tratamiento","REEMBOLSO","motivo","No existe cobro",
            "detalles",List.of(Map.of("detalleFacturaId",f.get("detalles").get(0).get("id").asLong(),"cantidad",1,"apta",1))),409);
        assertEquals(0,articulo().get("stockActual").asInt());
        var actual=solicitar("GET","/facturas/"+f.get("factura").get("id").asLong(),null,200);
        assertEquals(0,actual.get("detalles").get(0).get("devuelta").asInt()); importe("236",actual.get("factura").get("saldo"));
    }
}
