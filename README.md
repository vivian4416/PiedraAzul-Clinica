# PiedraAzul - Sistema de Citas

Sistema web para gestion y agendamiento de citas medicas/terapeuticas.

Incluye:
- Frontend Angular (modulos de citas, crear cita, agendar cita web, clientes, configuracion).
- Backend Spring Boot con seguridad JWT, persistencia JPA y base de datos H2 en memoria por defecto.

## Estado actual del sistema

Se cubren los 5 flujos funcionales principales:

1. RF1 - Listar citas por medico y fecha
- Vista de tabla con filtros por medico y fecha.
- Muestra listado de citas y cantidad por estado.

2. RF2 - Crear cita manual (agendador)
- Formulario para capturar paciente y datos de cita.
- Valida disponibilidad de slot segun agenda del medico.

3. RF3 - Agendar cita por web (paciente)
- Validacion de paciente registrado por documento/celular.
- Consulta de franjas disponibles por medico y semana.
- Creacion de cita con origen web (AUTONOMA).

4. RF4 - Configuracion de agenda (administrador/agendador)
- Configuracion de ventana de agendamiento en semanas.
- Configuracion de dias de atencion por medico/terapista.
- Configuracion de franja horaria por dia para cada medico/terapista.
- Configuracion de intervalo de minutos entre citas por medico/terapista.
- Activacion/inactivacion de medicos o terapistas para agendamiento.

5. RF5 - Exportar citas por medico y fecha (CSV)
- Exportacion desde la vista de citas filtradas.
- Formato CSV compatible con hojas de calculo.

## Documentacion de requisitos funcionales

RF1 - Listar citas por medico y fecha
- Vista: modulo Citas (tabla con filtros de medico y fecha).
- Backend: GET /api/v1/citas?medicoId=&fecha=.
- Resultado: listado de citas con conteo y estado; se muestra en tabla.

RF2 - Crear cita manual (agendador)
- Vista: modulo Nueva Cita (formulario de paciente y cita).
- Backend: POST /api/v1/citas.
- Validaciones: datos minimos del paciente, disponibilidad del slot por intervalo del medico.

RF3 - Agendar cita por web (paciente)
- Vista: modulo Agendar (seleccion de medico, semana y slot).
- Backend: POST /api/v1/citas/autonoma.
- Validaciones: paciente registrado (documento/celular), fecha en ventana, slot disponible.
- Disponibilidad: consulta de slots por medico y fecha (GET /api/v1/citas/slots).

RF4 - Configuracion de agenda (administrador/agendador)
- Vista: modulo Configuracion (ventana, dias, franja e intervalo por medico).
- Backend: GET/PUT /api/v1/medicos/configuracion.
- Validaciones: ventana 1-12 semanas, intervalo 5-120 min, hora inicio menor a hora fin.

RF5 - Exportar citas por medico y fecha (CSV)
- Vista: modulo Citas (boton Exportar CSV sobre la tabla filtrada).
- Origen de datos: listado actual filtrado por medico y fecha.
- Formato: CSV con columnas de hora, paciente, documento, celular, origen y estado.

## Estructura del proyecto

- Front/: Aplicacion Angular.
- Back/: API Spring Boot.

## Tecnologias

Frontend:
- Angular 21
- TypeScript
- RxJS

Backend:
- Java 21
- Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA
- H2 (default) / MySQL (opcional)

## Requisitos

- Node.js 20+
- npm 11+
- Java 21
- Maven 3.9+

## Como ejecutar

### 1) Backend

Desde la carpeta Back:

```bash
mvn spring-boot:run
```

API por defecto en:
- http://localhost:8090

Health check:
- http://localhost:8090/health

### 2) Frontend

Desde la carpeta Front:

```bash
npm install
ng serve -o
```

Frontend por defecto en:
- http://localhost:4200

## Configuracion de base de datos


Por defecto usa H2 en memoria:
- Se inicializa en cada arranque.
- Ejecuta schema.sql y data.sql automaticamente.

Proximamente se usara MySQL

## Endpoints principales

Auth:
- POST /api/v1/auth/login

Citas:
- GET /api/v1/citas?medicoId=&fecha=
- GET /api/v1/citas/slots?medicoId=&fecha=
- POST /api/v1/citas (manual)
- POST /api/v1/citas/autonoma (web)

Pacientes:
- GET /api/v1/pacientes?documento=

Medicos:
- GET /api/v1/medicos
- GET /api/v1/medicos/configuracion
- PUT /api/v1/medicos/configuracion

## Seguridad y autenticacion

- El backend protege endpoints por rol.
- El frontend actual consume la API con un login tecnico interno en CitasService para operar los modulos de citas.
- El endpoint de login (/auth/login) esta disponible para evolucionar a login de usuario completo en UI.

## Problemas comunes

1. Puerto 8090 en uso
- Error tipico: "Port 8090 was already in use".
- Solucion: cerrar el proceso que ocupa el puerto o cambiar server.port en application.yml.

2. Front no conecta con API
- Verificar que backend este corriendo en 8090.
- Verificar consola de red del navegador.

3. No aparecen slots en una fecha
- Revisar disponibilidad del medico.
- Validar que sea dia habil del medico.

4. Paciente no encontrado en agendar web
- Si el documento no existe en base de datos, el flujo muestra mensaje para registro/validacion previa.