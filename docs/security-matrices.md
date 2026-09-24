# Matrices RBAC y ABAC

Estas matrices describen el inicializador y el código backend actual. Si difieren de la tabla del enunciado, debe alinearse el código o explicarse la decisión antes de presentar.

## Matriz RBAC implementada

| Permiso | Administrador | Gerente | Supervisor | Empleado | Auditor | Invitado |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Crear documento | ✓ | ✓ | ✓ | ✓ | — | — |
| Consultar documento | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Modificar documento | ✓ | ✓ | ✓ | ✓ | — | — |
| Eliminar documento | ✓ | ✓ | — | — | — | — |
| Aprobar documento | ✓ | ✓ | ✓ | — | — | — |
| Ver auditoría | ✓ | — | — | — | ✓ | — |
| Gestionar usuarios | ✓ | — | — | — | — | — |
| Asignar roles | ✓ | — | — | — | — | — |
| Gestionar estado documento | ✓ | — | — | — | — | — |

El inicializador revoca también la concesión previa al Gerente para que una base de datos ya existente quede alineada con esta matriz al reiniciar Spring Boot.

## Políticas ABAC implementadas

Todas las siguientes se inicializan con efecto `DENY` y activas. Si una política no se cumple, se rechaza la operación.

| Código | Regla evaluada |
|---|---|
| `USUARIO_ACTIVO` | El estado del usuario debe ser `ACTIVO`. |
| `DEPARTAMENTO_IGUAL` | El departamento debe coincidir, excepto para Invitado al consultar documentos públicos. |
| `NIVEL_SEGURIDAD` | `usuario.nivelSeguridad >= documento.nivelConfidencialidad`. |
| `PROPIETARIO_MODIFICAR` | Para modificar, el usuario debe ser propietario; Administrador y Gerente están exceptuados. Solo se aplica a la acción de modificación. |
| `HORARIO_CONFIDENCIAL` | Si el nivel es 4 o 5, la consulta debe ocurrir de 08:00 a 18:00 inclusive. |
| `PAIS_IGUAL` | El país debe coincidir, excepto para Invitado al consultar documentos públicos. |
| `DISPOSITIVO_CORPORATIVO` | Para nivel 4 o 5, requiere dispositivo corporativo. La interfaz de demostración envía `X-Device-Type` y permite cambiar entre `CORPORATIVO` y `PERSONAL`. Es un atributo autodeclarado para pruebas, no una verificación confiable de hardware. |
| `INVITADO_RESTRINGIDO` | Un Invitado debe ser `EXTERNO`; el documento debe tener nivel máximo 1 y estado `PUBLICADO`. |

En una modificación se autoriza el documento con los atributos nuevos antes de guardarlo. El nuevo nivel no puede superar `usuario.nivelSeguridad`, debe estar entre 1 y 5, y solo Administrador puede reducir el nivel existente. Propietario y fecha de creación no se cambian mediante la edición normal. Aprobar un documento de nivel 1 lo publica; aprobar niveles superiores solo cambia el estado a `APROBADO`.

Solo Administrador puede publicar un documento de nivel 1 ya aprobado, retirar una publicación (vuelve a `APROBADO`) o devolver un documento aprobado a revisión (`PENDIENTE`). Cada transición se registra en auditoría. La carga inicial crea una guía pública de nivel 1 para la demostración del Invitado.

## Contexto de autorización

RBAC se comprueba primero. Si falta el permiso, ABAC no concede la operación. Para lecturas y cambios de documentos, luego se evalúan atributos. Las decisiones relevantes de documentos se registran con resultado y motivo.
