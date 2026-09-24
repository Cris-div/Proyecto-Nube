# Registro de auditoría

## Qué registra la aplicación

Los eventos se guardan en la entidad/tabla `Auditoria` (`auditoria`) con usuario, recurso, acción, fecha, resultado y motivo. `DocumentoController` registra consultas, creación, modificación, eliminación y aprobación, incluyendo los intentos de documento denegados que llegan a la autorización. La interfaz consulta `GET /api/auditoria` y ahora solicita páginas de 10 registros con filtros.

El inicializador no crea eventos de auditoría. Iniciar sesión tampoco se registra. Las operaciones de gestión de usuarios y políticas no están conectadas actualmente al servicio de auditoría. Por eso, una base recién creada puede mostrar cero filas hasta que se realicen operaciones documentales.

Durante la revisión local, el login de Administrador funcionó y `GET /api/auditoria?page=0&size=10` devolvió 76 eventos en el formato antiguo de lista. Esto confirma que había datos en la base activa y que la pantalla estaba interpretando mal la respuesta. No se copiaron datos personales de las filas a esta documentación.

## Consultar los eventos guardados

Desde la UI, inicia como Administrador o Auditor y abre **Auditoría**. Se puede filtrar por usuario, acción, resultado y rango de fechas. Para adjuntar evidencia, captura la tabla con el filtro utilizado y guarda los datos del mismo evento.

Consulta SQL para una exportación o comprobación directa:

```sql
SELECT
  a.id,
  u.nombre AS usuario,
  u.correo,
  a.recurso,
  a.accion,
  a.fecha,
  a.resultado,
  a.motivo
FROM auditoria a
LEFT JOIN usuarios u ON u.id = a.usuario_id
ORDER BY a.fecha DESC;
```

## Ejemplo de formato

Este es solo un ejemplo de estructura, no un evento observado ni evidencia de ejecución:

| Usuario | Recurso | Acción | Fecha | Resultado | Motivo |
|---|---|---|---|---|---|
| Ejemplo: usuario de laboratorio | DOCUMENTO 501 | CONSULTAR | Fecha/hora de la ejecución | DENEGADO | Motivo devuelto por autorización |

Para la entrega, reemplazarlo por una captura o exportación de la base que corresponda a una ejecución real.
