# SecureDocs

SecureDocs es una aplicación web académica para gestionar documentos empresariales y demostrar autorización combinada RBAC (permisos por rol) y ABAC (políticas por atributos del usuario y del documento). Está compuesta por una API REST en Spring Boot, una interfaz Angular y una base de datos MySQL.

## Estructura

- `demo/`: API Spring Boot, autenticación JWT, autorización, entidades JPA y carga de datos iniciales.
- `securedocs-frontend/`: aplicación Angular.
- `docs/`: arquitectura, modelo de datos, matrices de seguridad, plan de evidencias y guion de demostración.

## Requisitos locales

- Java 27, según `demo/pom.xml`.
- MySQL y una base de datos llamada `securedocs`.
- Node.js y npm compatibles con Angular CLI 22.

## Configurar MySQL

Primero crea tu configuración local desde la plantilla y reemplaza las contraseñas de ejemplo por valores propios. `.env` está excluido de Git:

```powershell
Copy-Item .env.example .env
notepad .env
```

Puedes usar MySQL instalado localmente o levantar solo la base de datos con Docker Compose. Compose lee las variables del archivo `.env`:

```powershell
docker compose up -d mysql
```

`MYSQL_ROOT_PASSWORD` y `DB_PASSWORD` deben tener el mismo valor. Spring Boot carga el mismo `.env` al iniciarse desde `demo/`, así no se guardan las credenciales de la base de datos ni la clave JWT en el código. Si usas MySQL local, crea la base `securedocs` y conserva los valores de conexión de `.env`. Para apagar el contenedor:

```powershell
docker compose down
```

Los datos quedan en el volumen `securedocs-mysql-data`; `docker compose down -v` también elimina ese volumen y la base local.

Si no usas Docker, crea la base de datos manualmente:

```sql
CREATE DATABASE securedocs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Hibernate crea/actualiza tablas con `spring.jpa.hibernate.ddl-auto=update`; no se incluye un respaldo de base de datos.

## Iniciar el backend

Desde PowerShell:

```powershell
cd demo
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080/api`. En el primer arranque, `DataInitializer` crea roles, permisos, departamentos, políticas y cuentas de demostración, además de una guía de nivel 1 publicada para demostrar el acceso del Invitado. No crea eventos de auditoría; estos se generan al usar la aplicación. Al aprobar un documento de nivel 1, este pasa a `PUBLICADO`; los demás pasan a `APROBADO`.

## Iniciar el frontend

En otra terminal:

```powershell
cd securedocs-frontend
npm install
npm start
```

Abrir `http://localhost:4200`. El frontend apunta a `http://localhost:8080/api`; el backend permite el origen local de Angular.

## Cuentas locales de demostración

El inicializador configura las cuentas siguientes con contraseña común `123456`:

| Rol | Correo |
|---|---|
| Administrador | `admin@securedocs.com` |
| Gerente | `gerente@securedocs.com` |
| Supervisor | `supervisor@securedocs.com` |
| Empleado | `empleado@securedocs.com` |
| Auditor | `auditor@securedocs.com` |
| Invitado | `invitado@securedocs.com` |

Son credenciales de laboratorio, no de producción. El backend cifra con BCrypt las contraseñas de las cuentas iniciales y de los usuarios creados desde la interfaz.

## Funcionalidades disponibles

- Iniciar/cerrar sesión con JWT.
- Consultar documentos visibles según las políticas aplicables; crear, editar, eliminar y aprobar según los permisos del rol.
- El Invitado consulta documentos publicados de nivel 1, incluso si pertenecen a otro departamento o país. Solo el Administrador puede reducir la confidencialidad; cualquier modificación debe quedar dentro del nivel de seguridad del usuario.
- Filtrar documentos por texto y estado; consultar atributos del documento.
- Consultar y administrar usuarios, roles, departamentos, nivel, contrato y estado (solo usuario autorizado).
- Consultar auditoría con filtros y paginación del lado del servidor.
- Cambiar el contexto simulado del dispositivo entre corporativo y personal para demostrar la política ABAC.

El botón de contexto en la barra superior es solo un simulador educativo del atributo; un cliente puede modificar ese encabezado y no debe tratarse como una prueba real del equipo en un despliegue.

## Verificación local

```powershell
cd securedocs-frontend
npm run build
npm test
```

```powershell
cd demo
.\mvnw.cmd clean test
```

La compilación confirma que el código compila; no sustituye la ejecución con MySQL ni la comprobación de los casos funcionales. El registro de evidencias y sus estados están en [docs/test-evidence.md](docs/test-evidence.md).

## Entregables y pendientes

La documentación del proyecto está en [`docs/`](docs/). El código fuente está en las carpetas indicadas. La carpeta de frontend tiene su propio repositorio Git local; no se detectó un remoto configurado ni un repositorio Git que incluya el backend y los documentos. Para la entrega final, el grupo debe publicar un repositorio accesible con el proyecto completo. También falta grabar y adjuntar el video real de demostración y completar evidencias visuales de los casos ejecutados.

## API de referencia

- `POST /api/auth/login`
- `/api/documentos` (GET/POST), `/api/documentos/{id}` (GET/PUT/DELETE), `/api/documentos/{id}/aprobar` (POST)
- `/api/usuarios` (GET/POST), `/api/usuarios/{id}` (GET/PUT/DELETE), `/api/usuarios/{id}/rol/{rolId}` (PUT)
- `GET /api/catalogos/departamentos`, `GET /api/catalogos/roles`
- `GET /api/auditoria?page=0&size=10` con filtros opcionales `usuario`, `accion`, `resultado`, `desde`, `hasta`
- `/api/politicas` para administración de políticas

La mayoría de endpoints necesitan JWT en `Authorization: Bearer <token>`; la autorización también se valida en el backend.
