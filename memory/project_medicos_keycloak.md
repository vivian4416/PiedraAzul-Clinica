---
name: Médicos vinculados a Keycloak por UUID
description: Los médicos del sistema de agendamiento usan el UUID de Keycloak como PK en la tabla medicos
type: project
---

La tabla `medicos` usa `VARCHAR(36)` como PK (UUID de Keycloak). Cuando se crea un usuario con rol MEDICO en Keycloak vía `/api/v1/usuarios`, `UsuarioService` auto-crea el perfil en la tabla `medicos` con el mismo UUID. El `data.sql` tiene los medicos reales: `795ee435-...` (javier gomez) y `bc119041-...` (Javier Gomez/medico3).

**Why:** Antes la tabla `medicos` tenía IDs numéricos auto-incrementales completamente desconectados de Keycloak, causando que el paciente viera médicos ficticios al agendar.

**How to apply:** Si se agregan nuevos médicos, hacerlo vía `POST /api/v1/usuarios` con `rol: "MEDICO"` — el perfil en `medicos` se crea automáticamente. NO insertar directamente en la tabla `medicos`.
