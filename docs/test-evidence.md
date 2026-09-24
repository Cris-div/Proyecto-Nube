# Plan y registro de evidencias

**Estado del documento:** plantilla de ejecución. No se marca ningún caso funcional como aprobado hasta ejecutarlo contra Spring Boot y MySQL y guardar una evidencia verificable (captura, respuesta HTTP o consulta de base de datos).

## Preparación

1. Configurar MySQL y arrancar Spring Boot según el README.
2. Iniciar Angular en `http://localhost:4200`.
3. Usar las cuentas sembradas. Contraseña de laboratorio: `123456`.
4. El inicializador crea una guía publicada de nivel 1 para el Invitado; crear documentos adicionales para los demás escenarios.
5. Por cada caso, guardar captura con rol, acción y resultado. Para denegaciones, conservar además el motivo y el evento de auditoría asociado.

## Casos del enunciado

| # | Escenario | Resultado esperado | Estado actual | Evidencia a guardar |
|---:|---|---|---|---|
| 1 | Empleado consulta un documento de su departamento y con nivel permitido | Permitido | Pendiente de ejecución | Captura de lista/detalle y evento `PERMITIDO` |
| 2 | Empleado consulta documento de otro departamento | Denegado u oculto en el listado | Pendiente de ejecución | Solicitud directa por ID, estado HTTP y auditoría |
| 3 | Supervisor aprueba documento de su área | Permitido | Pendiente de ejecución | Captura del estado aprobado y auditoría |
| 4 | Empleado intenta aprobar | Denegado por RBAC | Pendiente de ejecución | Respuesta 403 y motivo RBAC |
| 5 | Usuario nivel 2 consulta documento nivel 4 | Denegado por ABAC | Pendiente de ejecución | Respuesta y motivo de nivel insuficiente |
| 6 | Gerente elimina documento | Permitido | Pendiente de ejecución | Resultado de eliminación y auditoría |
| 7 | Auditor intenta modificar documento | Denegado por RBAC | Pendiente de ejecución | Respuesta 403 y motivo RBAC |
| 8 | Usuario inactivo inicia sesión | Denegado | Pendiente de ejecución | Mensaje de acceso denegado |
| 9 | Documento nivel 4+ se consulta fuera de horario | Denegado por ABAC | Pendiente de ejecución | Registrar hora, respuesta y auditoría; el caso requiere ejecutar fuera del intervalo |
| 10 | Documento nivel 5 desde dispositivo personal | Denegado por ABAC | Preparado para demostración; evidencia funcional pendiente | En la UI, cambiar el contexto a Personal, consultar un documento nivel 4/5 y guardar respuesta/motivo. El encabezado es simulado y no acredita el hardware real |
| 11 | Invitado externo consulta documento publicado nivel 1 | Permitido | Pendiente de ejecución | Captura del documento visible y auditoría |
| 12 | Invitado consulta documento confidencial | Denegado por ABAC | Pendiente de ejecución | Respuesta 403 y motivo de política de Invitado |

## Cinco casos adicionales propuestos

| # | Caso adicional | Resultado esperado | Estado actual | Evidencia a guardar |
|---:|---|---|---|---|
| 13 | Usuario activo cambia de departamento mientras consulta un documento ajeno | Denegado por ABAC | Pendiente de ejecución | Respuesta y evento con motivo de departamento |
| 14 | Empleado intenta modificar documento de otro propietario en su departamento | Denegado por ABAC | Pendiente de ejecución | Respuesta y motivo de propietario |
| 15 | Invitado externo consulta documento `PENDIENTE` de nivel 1 | Denegado por ABAC | Pendiente de ejecución | Respuesta y motivo de política de Invitado |
| 16 | Administrador cambia rol de un usuario | Permitido | Pendiente de ejecución | Captura de usuario actualizado |
| 17 | Usuario sin permiso para asignar roles intenta cambiar el rol | Denegado por RBAC | Pendiente de ejecución | Respuesta 403 |

## Verificaciones automatizadas locales

| Comando | Resultado observado |
|---|---|
| `npm run build` en `securedocs-frontend` | Correcto en la sesión de desarrollo |
| `npm test -- --watch=false` en `securedocs-frontend` | Correcto: 4 pruebas unitarias pasan; cubren login, compatibilidad de auditoría y encabezado de dispositivo usando HTTP simulado |
| `.\mvnw.cmd clean test` en `demo` | Correcto: 4 pruebas unitarias (3 ABAC, 1 RBAC); no usan MySQL |
| `.\mvnw.cmd -DskipTests compile` en `demo` | Correcto en la sesión de desarrollo |
| Login y `GET /api/auditoria?page=0&size=10` en el backend local | Login correcto como Administrador; endpoint activo devolvió formato antiguo de lista con 76 eventos. Se confirmó la incompatibilidad que ocultaba las filas en Angular. |

Verificación repetida el **24/09/2026**: `npm run build`, `npm test -- --watch=false` (4/4) y `.\mvnw.cmd clean test` (4/4) finalizaron correctamente. Las pruebas backend no conectan a MySQL ni sustituyen los 17 casos manuales.

Los comandos de compilación no cuentan como evidencia de autorización funcional. Agregar aquí fecha, responsable y enlaces a capturas cuando se ejecute la matriz.

