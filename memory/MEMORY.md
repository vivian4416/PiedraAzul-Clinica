# Project Memory

## Médicos vinculados a Keycloak por UUID
- **File:** `project_medicos_keycloak.md`
- **Description:** Los médicos del sistema de agendamiento usan el UUID de Keycloak como PK en la tabla medicos

La tabla `medicos` usa `VARCHAR(36)` como PK (UUID de Keycloak). Cuando se crea un usuario con rol MEDICO en Keycloak vía `/api/v1/usuarios`, `UsuarioService` auto-crea el perfil en la tabla `medicos` con el mismo UUID. El `data.sql` tiene los medicos reales: `795ee435-...` (javier gomez) y `bc119041-...` (Javier Gomez/medico3).

**Why:** Antes la tabla `medicos` tenía IDs numéricos auto-incrementales completamente desconectados de Keycloak, causando que el paciente viera médicos ficticios al agendar.

**How to apply:** Si se agregan nuevos médicos, hacerlo vía `POST /api/v1/usuarios` con `rol: "MEDICO"` — el perfil en `medicos` se crea automáticamente. NO insertar directamente en la tabla `medicos`.

---

## Roles del sistema - ADMIN = AGENDADOR
- **File:** `project_roles.md`
- **Description:** El rol administrador en Keycloak otorga permisos de ADMIN y AGENDADOR en Spring Security

El rol `administrador` en Keycloak realm `PiedraAzul_Realm` es el único rol para administradores/agendadores. `SecurityConfig.extractAuthorities()` asigna tanto `ROLE_ADMIN` como `ROLE_AGENDADOR` a usuarios con este rol. No existe un rol `agendador` separado en Keycloak — ambas funciones son el mismo perfil. `UsuarioService` convierte ADMIN → `administrador` y AGENDADOR → `administrador` al asignar roles en Keycloak.

**Why:** Requisito del cliente: administrador y agendador son el mismo rol.

**How to apply:** En los endpoints usar `hasAnyRole('ADMIN','AGENDADOR')` (ya implementado así). No crear rol `agendador` separado en Keycloak.
