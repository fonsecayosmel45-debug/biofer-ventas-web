# BIOFER Ventas

Sistema académico de ventas, inventario y cuentas corrientes.

- `backend/`: Spring Boot, código, pruebas, guía y scripts del backend.
- `frontend/`: Angular.
- `.env` y `.env.example`: configuración local y plantilla.
- `compose.yaml`: servicios Docker.
- `.gitignore`: archivos que no se suben a Git.

Desde esta carpeta, con Docker Desktop encendido:

```powershell
docker compose up -d mysql
cd backend
.\mvnw.cmd spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui/index.html

La [guía del backend](backend/README.md) contiene los bloques A–G, autenticación y ejemplos.

Para ejecutar las pruebas aisladas desde `backend/`: `.\mvnw.cmd verify`.
El verificador con informe JSON se conserva en `backend/scripts/verificar_backend.py`.

Para abrir la interfaz, en otra terminal:

```powershell
cd "D:\Nueva carpeta (2)\biofer-ventas-web-main\frontend"
npm install
npm start
```

Interfaz: http://localhost:4200. Consulta la [guía del frontend](frontend/README.md).
