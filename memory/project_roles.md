---
name: Roles del sistema - ADMIN = AGENDADOR
description: El rol administrador en Keycloak otorga permisos de ADMIN y AGENDADOR en Spring Security
type: project
---

El rol `administrador` en Keycloak realm `PiedraAzul_Realm` es el único rol para administradores/agendadores. `SecurityConfig.extractAuthorities()` asigna tanto `ROLE_ADMIN` como `ROLE_AGENDADOR` a usuarios con este rol. No existe un rol `agendador` separado en Keycloak — ambas funciones son el mismo perfil. `UsuarioService` convierte ADMIN → `administrador` y AGENDADOR → `administrador` al asignar roles en Keycloak.

**Why:** Requisito del cliente: administrador y agendador son el mismo rol.

**How to apply:** En los endpoints usar `hasAnyRole('ADMIN','AGENDADOR')` (ya implementado así). No crear rol `agendador` separado en Keycloak.
