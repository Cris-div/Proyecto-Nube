# Modelo de datos

```mermaid
erDiagram
    DEPARTAMENTO ||--o{ USUARIO : agrupa
    DEPARTAMENTO ||--o{ DOCUMENTO : clasifica
    ROL ||--o{ USUARIO : asigna
    ROL ||--o{ ROL_PERMISO : incluye
    PERMISO ||--o{ ROL_PERMISO : concede
    USUARIO ||--o{ DOCUMENTO : propietario
    USUARIO ||--o{ AUDITORIA : realiza

    DEPARTAMENTO {
        bigint id PK
        string nombre
    }
    ROL {
        bigint id PK
        string nombre
    }
    PERMISO {
        bigint id PK
        string nombre
    }
    ROL_PERMISO {
        bigint id PK
        bigint rol_id FK
        bigint permiso_id FK
    }
    USUARIO {
        bigint id PK
        string nombre
        string correo UK
        string password
        bigint rol_id FK
        bigint departamento_id FK
        int nivel_seguridad
        string pais
        string tipo_contrato
        string estado
    }
    DOCUMENTO {
        bigint id PK
        string titulo
        string descripcion
        bigint propietario_id FK
        bigint departamento_id FK
        int nivel_confidencialidad
        string estado
        string pais
        datetime fecha_creacion
    }
    POLITICA {
        bigint id PK
        string nombre UK
        string codigo_regla UK
        string descripcion
        string efecto
        boolean activa
    }
    AUDITORIA {
        bigint id PK
        bigint usuario_id FK
        string recurso
        string accion
        datetime fecha
        string resultado
        string motivo
    }
```

Las entidades JPA están en `demo/src/main/java/com/Lab06/demo/entity`. Hibernate actualiza el esquema desde esas entidades con la configuración local actual. `POLITICA` es un catálogo de reglas: la implementación ABAC interpreta `codigo_regla` en `AbacService`; no almacena una expresión arbitraria.

## Relaciones

- Un usuario tiene un rol y un departamento.
- Un rol se vincula a muchos permisos mediante `ROL_PERMISO`.
- Un documento referencia al usuario propietario y a un departamento.
- Un evento de auditoría puede referenciar al usuario que realizó la operación.
- Las políticas están centralizadas como registros activables.
