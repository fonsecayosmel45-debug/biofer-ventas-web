# Backend BIOFER: bloques A a G

Se conserva el avance original de clase y su estructura `model`, `repository`, `dto`, `service`, `controller`, `exception`. Las entidades nuevas están en archivos separados. El frontend no fue modificado.

| Bloque | Implementación | Rutas principales |
| --- | --- | --- |
| A | Categorías y subcategorías | `/api/categorias` |
| B | Productos, presentaciones, precios, stock y kardex | `/api/productos`, `/api/presentaciones` |
| C | Vendedores, clientes, cuentas, direcciones y asignaciones | `/api/vendedores`, `/api/clientes` |
| D | Pedidos, reservas FIFO, bonificaciones y tarifas referenciales | `/api/pedidos`, `/api/tarifas` |
| E | Despachos parciales, facturas por moneda, guías, archivos y PDF internos | `/api/despachos`, `/api/facturas`, `/api/guias-remision` |
| F | Crédito, vencimientos, pagos, anticipos y aplicaciones | `/api/creditos`, `/api/pagos` |
| G | Devoluciones, saldos, reembolsos, transferencias, reportes y auditoría | `/api/devoluciones`, `/api/reportes`, `/api/auditoria` |

## Arranque local

Abre Docker Desktop y espera a que el motor esté listo. En PowerShell:

```powershell
cd "D:\Nueva carpeta (2)\biofer-ventas-web-main"
docker compose up -d mysql
cd backend
.\mvnw.cmd spring-boot:run
```

La instalación MySQL de Windows ya utiliza 3306. Por eso `.env` publica el MySQL Docker de BIOFER en `localhost:3307` (`MYSQL_PORT=3307`); dentro de Docker se mantiene 3306. No es necesario crear el contenedor manualmente: `docker compose up -d mysql` lo crea y descarga la imagen si falta. El volumen de Compose no importa automáticamente datos de Codespaces.

El archivo `.env` local contiene la conexión proporcionada, el usuario administrador y un secreto JWT aleatorio. Está ignorado por Git. `.env.example` sirve como plantilla para otra instalación. En el primer arranque se crean solo el administrador y un registro técnico de concurrencia, si faltan. No se cargan productos, pedidos ni pagos de ejemplo. Hibernate añade las tablas y columnas con `ddl-auto=update`; haz una copia de una base existente antes de aplicar cambios de estructura.

- Estado: `http://localhost:8080/api/estado`.
- Swagger: `http://localhost:8080/swagger-ui/index.html`.
- Contrato OpenAPI: `/v3/api-docs`.

También puedes ejecutar el backend en Docker, desde la raíz del proyecto:

```powershell
docker compose --profile app up -d --build
```

No ejecutes ambas opciones del backend a la vez en 8080. `docker compose stop` conserva los volúmenes.

## Autenticación

`POST /api/auth/login` recibe:

```json
{"usuario":"admin","clave":"LA_CLAVE_DE_BIOFER_ADMIN_PASSWORD_EN_ENV"}
```

Utiliza el `token` recibido en `Authorization: Bearer TOKEN`. El token dura una hora. Solo Administración accede; los vendedores no tienen cuentas de acceso. `POST /api/auth/cambiar-clave` recibe `claveActual` y `claveNueva` (12–72 caracteres). Las claves se guardan con BCrypt. Cambiar `.env` no modifica la contraseña de un administrador ya creado.

Las rutas originales A–C conservan sus JSON; ahora requieren autenticación. Los listados paginados aceptan `page`, `size` y `sort`, con tamaño máximo 200.

Las escrituras D–G y tarifas requieren la cabecera `Idempotency-Key` con un UUID. Repite la misma clave y JSON cuando reintentes una operación tras perder la respuesta. Para una operación nueva usa otra clave. Reutilizar una clave con otro contenido devuelve 409. La respuesta guardada pertenece al momento de la primera ejecución; un GET permite consultar el estado actual.

## Flujo y ejemplos

Los IDs y fechas de los ejemplos deben sustituirse por los de tu base. Swagger detalla todos los campos y validaciones.

1. Registra categorías, productos, presentaciones, vendedor y cliente con los endpoints originales. El cliente y el vendedor mantienen la propiedad anidada `datos`.
2. Registra una dirección principal del cliente o completa sus datos al despachar.
3. Ingresa stock con `POST /api/presentaciones/{id}/movimientos`, enviando `tipo`, `cantidad`, `motivo` y `referencia`. No se acepta `SALIDA_VENTA` manual.
4. Configura crédito si corresponde, crea el pedido y apruébalo.
5. Despacha cantidades reservadas; luego factura y registra su guía.
6. Confirma pagos y aplícalos; consulta deuda, compromisos y vencimientos.

### Crédito y pedido

`PUT /api/creditos`:

```json
{"vendedorId":1,"moneda":"PEN","limite":10000.00,"aprobado":true,"motivo":"Línea aprobada"}
```

`POST /api/pedidos`:

```json
{
  "clienteId":1,"direccionId":1,"observaciones":"Entrega parcial permitida",
  "credito":true,"diasCredito":30,"tasaIgv":0.18,
  "detalles":[
    {"presentacionId":1,"cantidad":10,"precioSinIgv":100.0000,"moneda":"PEN","bonificacion":false},
    {"presentacionId":1,"cantidad":1,"precioSinIgv":0,"moneda":"PEN","bonificacion":true}
  ]
}
```

`direccionId` es opcional; por defecto se toma la principal activa. Se copian al pedido los datos del cliente, vendedor y envío. Administración introduce el precio final por línea; el catálogo no reescribe precios acordados. Las bonificaciones deben valer cero y consumen stock.

`POST /api/tarifas` guarda referencias por vendedor **o** cliente: `vendedorId` o `clienteId`, `presentacionId`, `precioSinIgv`, `moneda`, `vigenteDesde`, `vigenteHasta` opcional y `motivo`. No selecciona automáticamente el precio del pedido. `GET /api/tarifas` permite filtrar por esos IDs y `soloVigentes=true`. `POST /api/tarifas/{id}/desactivar` recibe `motivo` y conserva el historial.

`POST /api/pedidos/{id}/aprobar` recibe `{}`. Si el crédito está bloqueado, Administración puede indicar `{"motivoExcepcion":"Justificación autorizada"}`. La excepción queda auditada.

Al aprobar se reserva únicamente lo disponible, manteniendo faltantes. Las entradas posteriores reservan por fecha de aprobación, ID de pedido e ID de línea. La existencia física se descuenta al despachar.

`PUT /api/pedidos/{id}` recibe el pedido completo y `motivo`; libera reservas y vuelve a borrador para aprobar nuevamente. Solo se permite antes del primer despacho. `POST .../anular` también recibe `motivo`. Después de despachar se permite `POST .../cerrar-pendientes` con `motivo`, liberando reservas sin alterar lo enviado ni facturado.

### Despacho

`POST /api/despachos`:

```json
{
  "pedidoId":1,"fechaDespacho":"2026-11-14",
  "detalles":[{"detalleId":1,"cantidad":4}],
  "destinatario":"Nombre de quien recibe","documentoDestinatario":"12345678",
  "direccion":"Dirección de entrega","agencia":"Agencia elegida","recoge":"Persona que recoge",
  "costoEnvio":15.00,"monedaEnvio":"PEN","pagaEnvio":"CLIENTE",
  "conductor":null,"vehiculo":null,"observaciones":"Primer envío"
}
```

`detalleId` pertenece al **detalle del pedido**. Destinatario, documento, dirección y agencia pueden omitirse si ya están en el pedido, pero deben estar completos al despachar. `pagaEnvio` admite `CLIENTE`, `EMPRESA` o `VENDEDOR`. El costo de transporte se guarda para seguimiento; no se suma automáticamente a la factura de productos.

Un pedido al contado exige pagos confirmados con fondos suficientes del vendedor en cada moneda. Los fondos se apartan para ese pedido hasta facturarlo. Un pedido a crédito vuelve a comprobar cupo y mora al despachar; las excepciones requieren motivo.

### Factura, guía y archivos

`POST /api/facturas`:

```json
{"despachoId":1,"moneda":"PEN","fechaEmision":"2026-11-14","detalles":[{"detalleId":1,"cantidad":4}]}
```

Aquí `detalleId` pertenece al **detalle del despacho**, consultado con `GET /api/despachos/{id}`. Se impiden cantidades superiores a las enviadas, facturación duplicada y mezcla de monedas. Cada despacho y factura pertenecen a un pedido. El redondeo acumulado conserva el total de las líneas al facturar en partes.

La primera factura fija el inicio del crédito y el vencimiento de todo el pedido. Las siguientes conservan ese vencimiento, aunque ya esté pasado. La deuda nace al facturar; el monto aprobado aún no facturado es un compromiso separado. El cupo cuenta ambos sin duplicarlos.

`POST /api/guias-remision` recibe `{"despachoId":1,"fechaTraslado":"2026-11-14"}`. Se permite una guía por despacho.

- `GET /api/facturas/{id}/pdf`: factura interna.
- `GET /api/guias-remision/{id}/pdf`: guía interna.
- `POST /api/facturas/{id}/documento-oficial` y `POST /api/guias-remision/{id}/documento-oficial`: reciben `serie`, `numero`, `archivo`.

Los documentos oficiales se emiten fuera del sistema. Aquí se registran sus referencias sin reemplazar las ya guardadas. Los PDF se marcan como **internos**; no hay emisión ni validación SUNAT.

`POST /api/archivos` recibe `form-data`, campo `archivo` de tipo File. Admite PDF, PNG o JPEG de hasta 10 MB, guardados con nombres aleatorios fuera del directorio público. Devuelve el identificador para usarlo como `archivo` o `comprobante`. También puedes registrar una referencia externa. `GET /api/archivos/{id}` descarga con autenticación.

## Pagos y devoluciones

`POST /api/pagos`:

```json
{"vendedorId":1,"moneda":"PEN","monto":300.00,"metodo":"TRANSFERENCIA","referencia":"OPERACION-UNICA-001","comprobante":"ARCHIVO_O_REFERENCIA_DEL_SOPORTE","fechaPago":"2026-11-14"}
```

Empieza `POR_COMPROBAR`. `POST /api/pagos/{id}/confirmar` no necesita Body. `POST .../rechazar` requiere `motivo`. Solo `RECIBIDO` tiene fondos utilizables. La referencia es única para impedir registrar dos veces un depósito.

`POST /api/pagos/{id}/aplicaciones`:

```json
{"detalles":[{"facturaId":1,"monto":200.00},{"facturaId":2,"monto":100.00}]}
```

Las facturas deben pertenecer al mismo vendedor y moneda del pago. El excedente queda como anticipo. No hay conversiones entre monedas.

`POST /api/pagos/{id}/revertir` recibe `motivo`. Revierte aplicaciones y repone deudas de forma atómica. Se bloquea si el pago respalda despachos sin facturar, si una deuda fue transferida o si una devolución produjo saldo/reembolso. Esos casos requieren conciliación administrativa previa para no recuperar fondos ya usados.

`POST /api/devoluciones`:

```json
{"facturaId":1,"tratamiento":"REDUCIR_DEUDA","motivo":"Devolución aceptada","detalles":[{"detalleFacturaId":1,"cantidad":2,"apta":1}]}
```

La cantidad dañada es `cantidad - apta`. Solo lo apto vuelve al stock vendible y puede reservarse para pendientes. No se vuelve a abrir automáticamente el pedido original. Las devoluciones corresponden a cantidades facturadas, con valor histórico.

- `REDUCIR_DEUDA`: disminuye deuda pendiente, sin excederla.
- `SALDO_FAVOR`: genera saldo del vendedor sobre dinero previamente cobrado.
- `REEMBOLSO`: deja pendiente una devolución de dinero previamente cobrado.

Si una devolución abarca deuda y parte pagada, divídela por tratamiento. La misma unidad no puede devolverse dos veces. Las bonificaciones tienen valor cero.

`POST /api/saldos-favor/{id}/aplicaciones` recibe la misma lista de aplicaciones a facturas. `POST /api/devoluciones/{id}/reembolso` recibe `referencia` y `comprobante`, y registra un reembolso hecho por fuera del sistema. No ejecuta transferencias bancarias.

`POST /api/facturas/{id}/transferir-deuda` recibe `vendedorNuevoId`, `motivo` y `motivoExcepcion` opcional. Transfiere la responsabilidad del saldo pendiente, verificando el crédito nuevo. Conserva cliente, vencimiento y pagos anteriores. Cambiar el vendedor del cliente no transfiere deudas automáticamente.

## Consultas de seguimiento

- `/api/pedidos/{id}`: cabecera, líneas, reservas, cantidades enviadas/facturadas/cerradas, totales por moneda y despachos.
- `/api/facturas/{id}`: líneas, aplicaciones y devoluciones.
- `/api/pagos/{id}`: aplicaciones y fondos apartados.
- `/api/vendedores/{id}/cuenta-corriente`: deuda, compromisos y límites.
- `/api/clientes/{id}/cuenta-corriente`: saldos por moneda y facturas.
- `/api/vendedores/{id}/saldos-favor`: anticipos, saldos por devoluciones y total por moneda.
- `/api/reportes/vencimientos?diasAviso=7`: próximos vencimientos y mora; días transcurridos desde el inicio del crédito.
- `/api/reportes/stock`: existencias, reservas, disponibles y umbrales.
- `/api/reportes/productos-danados`: daños registrados en devoluciones.
- `/api/transferencias-deuda`: historial de responsables.
- `/api/auditoria`: operaciones y excepciones con actor y fecha.
- `/api/auditoria/entidades/{tipo}/{id}`: creador y último modificador de `categorias`, `productos`, `presentaciones`, `vendedores` o `clientes`.

Los registros antiguos sin auditoría conservan campos nulos hasta una modificación. Los módulos nuevos exponen sus campos de auditoría. Las alertas son consultas; no envían correos ni mensajes externos.

## Pruebas sin llenar MySQL

```powershell
# Desde backend/:
.\mvnw.cmd verify
# Alternativa con informe JSON, también desde backend/:
py -3 scripts/verificar_backend.py
```

Las pruebas usan `application-test.properties`, H2 en memoria y un puerto HTTP aleatorio. No llaman a Codespaces ni usan MySQL. La opción Python recopila pruebas y errores en `backend/.local/verificacion/resultado.json`, con el log de Maven al lado. El JAR queda en `backend/target`. Los archivos adjuntos locales se guardan en `backend/.local/archivos` al iniciar desde `backend/`; Docker utiliza su volumen de documentos.

Se verifican FIFO, faltantes, despachos parciales, rollback, contado, crédito y excepciones, precios congelados, bonificaciones, monedas separadas, pagos/reversión, devoluciones, saldo/reembolso, PDF, transferencias, idempotencia, autenticación y concurrencia.

## Alcance técnico

Java 17, Spring Boot 4.1.2, Maven, MVC, Validation, JPA/Hibernate, MySQL, Spring Security JWT, OpenAPI/Swagger y PDFBox. Docker Compose sirve para MySQL y opcionalmente el backend. No se utiliza Dev Container.

Las operaciones de ventas se serializan mediante bloqueo de fila en la base y transacciones. Las presentaciones tienen bloqueo de escritura y versión. Es una solución para Administración con volumen académico; algunos reportes agregan datos en memoria y pueden optimizarse al crecer.

Las pruebas H2 no sustituyen comprobar MySQL y construir las imágenes cuando el motor Docker esté disponible. Quedan fuera del alcance acordado: frontend, emisión SUNAT, varios almacenes, lotes y regalos ajenos al catálogo.

Esta descarga ZIP no contiene `.git`: los cambios son locales y todavía no se han subido a GitHub. La copia anterior y los auxiliares retirados de la raíz se conservaron fuera del proyecto, en `D:\Nueva carpeta (2)\Respaldo BIOFER organizacion`.
