# BIOFER — Frontend Angular

Interfaz de Administración conectada al backend real. Conserva Angular 21 y los componentes standalone del proyecto original, sin bibliotecas visuales adicionales.

## Identidad visual

La apariencia toma como referencia `D:\Biofer\frontend-biofer`: el mismo logo y fondo agrícola, login central translúcido, paneles verdes oscuros, navegación flotante desplegable, acentos verdes y tablas con texto legible. Los recursos visuales están en `public/images/`.

Los formularios compartidos muestran secciones de identificación, contacto, datos comerciales, documentos y estado según los campos de cada módulo. En personas naturales y empresas se muestran los datos de identificación que corresponden. Clientes y vendedores incluyen filtros, paginación y fichas de consulta.

El proyecto de referencia permanece independiente; no se incorporan sus módulos de producción o formulaciones ni su backend.

### Filtros y tablas de catálogo

- Productos: búsqueda por palabras sin distinguir tildes, nombre comercial, descripción y códigos de sus presentaciones; categoría con subcategorías, procedencia, estado, existencia y rango de fecha de creación o actualización.
- Categorías: nombre, estado, nivel principal/subcategoría y categoría superior.
- Inventario: producto, categoría y subcategorías, unidad, moneda, procedencia, estado, stock, fechas y rango de precio. Se exige escoger una moneda al comparar precios.
- Se pueden combinar criterios, aplicar/limpiar, quitar filtros por separado y usar indicadores como filtros rápidos. Los indicadores del catálogo completo no se recalculan con la búsqueda.
- Las tablas incluyen orden, selección por página, tamaño de página, columnas opcionales y exportación CSV de resultados o selección. La exportación respeta los filtros aplicados.
- Kardex e historial de precios incluyen búsqueda y rango de fechas; el kardex permite filtrar por tipo de movimiento.

Los filtros usan los datos completos que devuelve la API paginada. Las fechas se comparan por día en hora de Lima. Las categorías no ofrecen filtros de fecha porque su respuesta actual no contiene fechas. Los indicadores de stock describen unidades por presentación, no litros ni kilos. Un producto sin presentaciones se distingue de uno con presentaciones sin disponibilidad.

## Ejecutar tú mismo

Mantén MySQL y el backend encendidos. En otra terminal de VS Code:

```powershell
cd "D:\Nueva carpeta (2)\biofer-ventas-web-main\frontend"
npm install
npm start
```

Abre `http://localhost:4200`. El proxy de Angular envía `/api/**` a `http://localhost:8080`. No cambies la URL del frontend al puerto MySQL 3307: ese puerto es exclusivamente para la base de datos.

Inicia sesión con `BIOFER_ADMIN_USER` y la contraseña actual del administrador. La contraseña inicial figura en `BIOFER_ADMIN_PASSWORD` del `.env` de la raíz. Las claves no se copian al código Angular. La sesión se guarda en `sessionStorage`; al vencer el token se solicita ingresar de nuevo.

No se ejecutaron instalaciones, compilaciones, servidores ni inserciones para esta entrega del frontend: esas ejecuciones las realiza el usuario.

## Pantallas incluidas

| Pantalla | Funciones |
| --- | --- |
| Inicio | Resumen real, últimos pedidos, deuda separada por moneda y alertas de vencimientos. |
| Productos y categorías | Crear y editar productos, seleccionar categorías, mantener jerarquías y estados. |
| Inventario | Presentaciones, precios, umbrales, movimientos manuales, kardex e historial de precios. |
| Clientes | Crear/editar personas y empresas, asignar vendedor, direcciones y cuenta corriente. |
| Vendedores | Crear/editar vendedores, cuentas bancarias y cuenta corriente. |
| Pedidos | Crear/editar borradores, precio por línea, bonificaciones, monedas, condiciones de crédito, aprobar, reservar, anular y cerrar pendientes. |
| Despacho desde el pedido | Cantidades reservadas, entregas parciales, datos del destinatario, agencia, transporte y costo. |
| Documentos | Facturar cantidades despachadas por moneda, crear guías, descargar PDF internos y registrar documentos oficiales externos. |
| Crédito y cobranzas | Límites por moneda, registro y verificación de pagos, aplicaciones a varias facturas, reversión, anticipos, saldos a favor y transferencia de deuda. |
| Devoluciones | Cantidades aptas/dañadas, reducción de deuda, saldo a favor y constancia de reembolso. |
| Reportes | Stock, vencimientos, daños, auditoría, transferencias y exportación CSV. |

Los archivos de soporte pueden adjuntarse (PDF/PNG/JPEG de hasta 10 MB) o registrarse mediante referencia externa. El backend aplica las reglas definitivas y calcula los importes. Los totales mostrados antes de guardar el pedido son estimados; las respuestas del backend son las definitivas.

## Recorrido recomendado para presentar

1. Inicia sesión y revisa las existencias cargadas en Inventario.
2. Crea un pedido desde Pedidos, elige un cliente y agrega las presentaciones.
3. Guarda y aprueba el pedido para reservar stock.
4. Desde su detalle registra un despacho; puedes enviar solo una parte.
5. En Documentos, factura ese despacho en una moneda y repite para otra si corresponde. Crea la guía y descarga los PDF.
6. En Cobranzas, registra el pago, confirma su recepción y aplícalo a las facturas.
7. Consulta el saldo y los vencimientos; registra una devolución si necesitas mostrar ese flujo.

Las operaciones financieras usan claves de idempotencia generadas con `crypto.randomUUID()`. Los botones de envío se bloquean durante la solicitud y no hay reintentos automáticos. Un reintento manual tras un fallo de red conserva la clave mientras la solicitud permanezca en esta sesión de la aplicación. Los movimientos manuales de inventario del backend original no implementan idempotencia: ante una respuesta incierta revisa el kardex antes de repetirlos.

Las tablas se cargan desde los endpoints existentes; no contienen datos comerciales simulados en el navegador. Las operaciones solo se guardan al confirmar sus formularios.

## Comprobación y distribución

Puedes comprobar la compilación sin iniciar el servidor con:

```powershell
npm run build
```

El resultado queda en `dist/frontend/browser`. Para publicarlo se necesita configurar el servidor web para devolver `index.html` en rutas Angular y reenviar `/api` al backend. El proxy de desarrollo de Angular no se incluye en el resultado de producción.

La interfaz cubre el flujo principal para la presentación. La administración de tarifas referenciales sigue disponible en el backend/Postman y no tiene pantalla propia en esta versión.

## Estructura

```text
src/app/
  core/       API, autenticación, guard e interceptor
  layout/     Navegación y estructura de la aplicación
  shared/     Formularios, iconos y estado de las pantallas
  features/   Pantallas por módulo
```

El diseño se adapta a escritorio y móvil. No necesita Dev Container ni una nueva carpeta fuera de `frontend/`.
