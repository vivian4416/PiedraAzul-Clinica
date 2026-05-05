# PiedraAzul - Sistema de Citas

Sistema web para gestión y agendamiento de citas médicas/terapéuticas.

El proyecto está dividido en dos aplicaciones:

- **Front/**: aplicación Angular para usuarios administradores/agendadores, médicos y pacientes.
- **Back/**: API REST con Spring Boot, seguridad JWT/OIDC con Keycloak, persistencia JPA y base de datos H2 en memoria por defecto.

## Estado actual del sistema

El sistema implementa los flujos principales de agenda médica:

1. **RF1 - Listar citas por médico y fecha**
   - Vista de tabla con filtros por médico y fecha.
   - Carga citas paginadas desde backend.
   - Muestra totales y conteos por estado.

2. **RF2 - Crear cita manual**
   - Flujo usado por administrador/agendador.
   - Captura datos del paciente y de la cita.
   - Valida disponibilidad del slot según la agenda del médico.

3. **RF3 - Agendar cita por web**
   - Flujo usado por paciente autenticado, administrador o agendador.
   - Consulta médicos, semanas, días disponibles y slots.
   - Crea citas con origen `AUTONOMA`.

4. **RF4 - Configuración de agenda**
   - Configuración de ventana de agendamiento en semanas.
   - Activación/inactivación de médicos.
   - Configuración de días de atención, franjas horarias e intervalo por médico.

5. **RF5 - Exportar citas a CSV**
   - Exportación desde la vista de citas filtradas.
   - Genera un archivo CSV con hora, paciente, documento, celular, origen y estado.

## Tecnologías

### Frontend

- Angular 21.2
- TypeScript 5.9
- RxJS 7.8
- Keycloak JS 26.2
- Angular SSR habilitado en la configuración del proyecto

### Backend

- Java 21
- Spring Boot 3.5.6
- Spring Web
- Spring Security
- Spring OAuth2 Resource Server JWT
- Spring Data JPA
- Bean Validation
- H2 Database por defecto
- MySQL opcional mediante variables de entorno
- Maven

## Requisitos

- Node.js 20+
- npm 11+
- Angular CLI, opcional si se usa `npx ng`
- Java 21
- Maven 3.9+
- Keycloak corriendo localmente para autenticación

## Estructura del proyecto

```text
PiedraAzul/
├── Back/
│   ├── pom.xml
│   ├── postman/
│   │   ├── PiedraAzul-API.postman_collection.json
│   │   └── PiedraAzul-Local.postman_environment.json
│   └── src/
│       ├── main/java/co/piedrazul/api/
│       │   ├── config/
│       │   ├── core/
│       │   ├── integrations/keycloak/
│       │   ├── modules/
│       │   │   ├── auth/
│       │   │   ├── citas/
│       │   │   ├── medicos/
│       │   │   ├── pacientes/
│       │   │   └── usuarios/
│       │   └── security/
│       └── main/resources/
│           ├── application.yml
│           ├── schema.sql
│           └── data.sql
└── Front/
    ├── package.json
    ├── angular.json
    └── src/app/
        ├── citas/
        │   ├── agendar-cita/
        │   ├── configuracion/
        │   └── crear-cita/
        ├── cliente/
        ├── guards/
        ├── header/
        ├── login/
        ├── models/
        ├── redirect/
        └── services/
```

## Cómo ejecutar

### 1. Levantar Keycloak

El frontend y el backend esperan Keycloak en:

```text
http://localhost:8080
```

Configuración usada por el frontend:

```text
Realm: PiedraAzul_Realm
Client ID: FrontAngular
```

Roles esperados en `realm_access.roles`:

- `administrador`
- `agendador`
- `medico`
- `paciente`

El frontend agrupa `administrador` y `agendador` como perfil visual `ADMIN`.

### 2. Backend

Desde la carpeta `Back`:

```bash
mvn spring-boot:run
```

API por defecto:

```text
http://localhost:8090
```

Health check público:

```text
GET http://localhost:8090/health
```

### 3. Frontend

Desde la carpeta `Front`:

```bash
npm install
npm start
```

También se puede ejecutar con:

```bash
npx ng serve -o
```

Frontend por defecto:

```text
http://localhost:4200
```

## Configuración del backend

Archivo principal:

```text
Back/src/main/resources/application.yml
```

Configuración por defecto:

```yaml
server:
  port: 8090

spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:piedrazul;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}
    username: ${DB_USER:sa}
    password: ${DB_PASSWORD:}
    driver-class-name: ${DB_DRIVER:org.h2.Driver}
  jpa:
    hibernate:
      ddl-auto: none
  sql:
    init:
      mode: always
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/PiedraAzul_Realm
```

### Variables de entorno soportadas

Base de datos:

```bash
DB_URL=jdbc:h2:mem:piedrazul;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DB_USER=sa
DB_PASSWORD=
DB_DRIVER=org.h2.Driver
```

Keycloak Admin Client:

```bash
KEYCLOAK_BASE_URL=http://localhost:8080
KEYCLOAK_REALM=PiedraAzul_Realm
KEYCLOAK_ADMIN_CLIENT_ID=PiedraAzul_Postman
KEYCLOAK_ADMIN_CLIENT_SECRET=<secret-del-cliente>
```

JWT legacy local:

```bash
JWT_SECRET=dev-secret-para-cambio-obligatorio-en-prod-2026
JWT_EXP_MIN=120
```

> Nota: el login local está deshabilitado. La autenticación principal se realiza con Keycloak/OIDC.

## Configuración del frontend

El frontend consume la API desde:

```text
http://localhost:8090/api/v1
```

La configuración de Keycloak está en:

```text
Front/src/app/services/auth.service.ts
```

Valores actuales:

```ts
url = 'http://localhost:8080';
realm = 'PiedraAzul_Realm';
clientId = 'FrontAngular';
```

## Seguridad y autenticación

- El backend funciona como Resource Server JWT.
- Todos los endpoints `/api/v1/**` requieren autenticación.
- `/health` está permitido sin token.
- Los roles se leen desde `realm_access.roles`.
- El backend agrega aliases internos con prefijo `ROLE_`.
- `administrador` obtiene permisos equivalentes a `ADMIN` y `AGENDADOR`.
- CORS permite por defecto:
  - `http://localhost:4200`
  - `http://127.0.0.1:4200`

### Roles y permisos principales

| Rol | Permisos principales |
|---|---|
| `administrador` | Listar citas, crear citas manuales, agendar, configurar agenda, administrar usuarios |
| `agendador` | Listar citas, crear citas manuales, agendar, configurar agenda |
| `medico` | Listar citas y consultar disponibilidad |
| `paciente` | Agendar cita autónoma y consultar disponibilidad |

## Endpoints principales

### Health

| Método | Endpoint | Autenticación | Descripción |
|---|---|---|---|
| GET | `/health` | No | Verifica que el backend esté activo |

### Auth

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/auth/login` | Login local deshabilitado. Responde `410 GONE` |

### Usuarios

| Método | Endpoint | Roles | Descripción |
|---|---|---|---|
| GET | `/api/v1/usuarios` | ADMIN | Lista usuarios desde Keycloak |
| GET | `/api/v1/usuarios/{id}` | ADMIN | Obtiene un usuario |
| GET | `/api/v1/usuarios/me` | Autenticado | Obtiene el perfil del usuario actual |
| POST | `/api/v1/usuarios` | ADMIN | Crea usuario en Keycloak |
| PUT | `/api/v1/usuarios/{id}` | ADMIN | Actualiza usuario |
| DELETE | `/api/v1/usuarios/{id}` | ADMIN | Desactiva usuario |

### Citas

| Método | Endpoint | Roles | Descripción |
|---|---|---|---|
| GET | `/api/v1/citas?medicoId=&fecha=&page=&size=` | ADMIN, AGENDADOR, MEDICO | Lista citas por médico y fecha con paginación |
| GET | `/api/v1/citas/slots?medicoId=&fecha=` | Autenticado | Consulta slots disponibles y ocupados |
| POST | `/api/v1/citas` | ADMIN, AGENDADOR | Crea cita manual |
| POST | `/api/v1/citas/autonoma` | ADMIN, AGENDADOR, PACIENTE | Crea cita autónoma desde flujo web |

### Pacientes

| Método | Endpoint | Roles | Descripción |
|---|---|---|---|
| GET | `/api/v1/pacientes?documento=` | Autenticado | Busca paciente por documento |
| GET | `/api/v1/pacientes/sugerencias?documento=` | Autenticado | Autocompleta pacientes por documento |

### Médicos

| Método | Endpoint | Roles | Descripción |
|---|---|---|---|
| GET | `/api/v1/medicos` | Autenticado | Lista médicos activos |
| GET | `/api/v1/medicos/configuracion` | Autenticado | Consulta configuración de agenda |
| PUT | `/api/v1/medicos/configuracion` | ADMIN, AGENDADOR | Guarda configuración de agenda |

## Modelos principales

### Crear cita

`POST /api/v1/citas` y `POST /api/v1/citas/autonoma` reciben:

```json
{
  "numDocumento": "79453201",
  "nombres": "Carlos",
  "apellidos": "Rios Vargas",
  "celular": "3112345678",
  "genero": "HOMBRE",
  "fechaNacimiento": "1990-01-15",
  "email": "paciente@example.com",
  "medicoId": "795ee435-a5d2-4817-87b0-11632b46ff4c",
  "fecha": "2026-05-04",
  "hora": "08:30"
}
```

Validaciones principales:

- `numDocumento`: obligatorio y solo numérico.
- `nombres`: obligatorio.
- `apellidos`: obligatorio.
- `celular`: obligatorio, 10 dígitos.
- `genero`: `HOMBRE`, `MUJER` u `OTRO`.
- `medicoId`: obligatorio.
- `fecha`: obligatoria.
- `hora`: obligatoria.

### Configuración de agenda

`PUT /api/v1/medicos/configuracion` recibe:

```json
{
  "ventanaSemanas": 4,
  "medicos": [
    {
      "id": "795ee435-a5d2-4817-87b0-11632b46ff4c",
      "intervaloMin": 30,
      "activo": true,
      "disponibilidad": [
        {
          "diaSemana": 1,
          "horaInicio": "08:00",
          "horaFin": "12:00"
        }
      ]
    }
  ]
}
```

Validaciones principales:

- `ventanaSemanas`: entre 1 y 12.
- `medicos`: mínimo 1 elemento.
- `intervaloMin`: entre 5 y 120 minutos.
- `diaSemana`: entre 1 y 7.
- `horaInicio`: obligatoria.
- `horaFin`: obligatoria y debe ser mayor que `horaInicio`.

## Base de datos

Por defecto se usa H2 en memoria:

- La base se crea en cada arranque.
- `schema.sql` define las tablas.
- `data.sql` carga datos iniciales.

Tablas principales:

- `medicos`
- `medico_disponibilidad`
- `configuracion_citas`
- `pacientes`
- `citas`
- `auditoria`

Para usar MySQL, configurar las variables `DB_URL`, `DB_USER`, `DB_PASSWORD` y `DB_DRIVER`.

Ejemplo:

```bash
DB_URL=jdbc:mysql://localhost:3306/piedrazul
DB_USER=root
DB_PASSWORD=tu_password
DB_DRIVER=com.mysql.cj.jdbc.Driver
```

## Postman

El backend incluye una colección y ambiente local en:

```text
Back/postman/PiedraAzul-API.postman_collection.json
Back/postman/PiedraAzul-Local.postman_environment.json
```

## Pruebas

Desde `Back`:

```bash
mvn test
```

Desde `Front`:

```bash
npm test
```

## Problemas comunes

### Puerto 8090 en uso

Error típico:

```text
Port 8090 was already in use
```

Solución:

- Cerrar el proceso que usa el puerto 8090.
- O cambiar `server.port` en `Back/src/main/resources/application.yml`.

### El frontend no conecta con la API

Verificar:

- Backend activo en `http://localhost:8090`.
- Keycloak activo en `http://localhost:8080`.
- Token Bearer presente en las peticiones.
- CORS permitido para `http://localhost:4200`.

### Respuesta 401

Posibles causas:

- Usuario no autenticado.
- Token expirado.
- Issuer del token diferente a `http://localhost:8080/realms/PiedraAzul_Realm`.
- Frontend apuntando a un realm o client distinto.

### Respuesta 403

Posibles causas:

- El usuario está autenticado, pero no tiene el rol necesario.
- El rol no está en `realm_access.roles`.
- El rol está escrito diferente a lo esperado: `administrador`, `agendador`, `medico`, `paciente`.

### No aparecen slots

Revisar:

- Que el médico esté activo.
- Que la fecha esté dentro de la ventana de agendamiento.
- Que el día esté configurado en `medico_disponibilidad`.
- Que la franja horaria tenga slots según `intervaloMin`.
- Que no exista una cita ocupando el mismo médico y fecha/hora.

### Paciente no encontrado

El endpoint `/api/v1/pacientes?documento=` busca pacientes existentes por documento. Si no existe, el flujo de creación de cita puede crear o asociar el paciente según las validaciones del servicio.

## Notas de implementación

- Las citas se guardan con `estado = CONFIRMADA` por defecto.
- El campo `origen` distingue entre citas `MANUAL` y `AUTONOMA`.
- Existe restricción única para evitar doble reserva del mismo médico en la misma fecha/hora.
- El backend registra auditoría de acciones relevantes en la tabla `auditoria`.
- El listado de citas usa paginación con `page` y `size`.
- La exportación CSV se hace en el frontend usando la tabla filtrada visible.
