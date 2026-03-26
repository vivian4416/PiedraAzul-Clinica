# Backend Piedrazul - Spring Boot

Implementacion del backend en Spring Boot para el primer corte.

## Cobertura del primer corte

- RF1: Listar citas por medico y fecha.
- RF2: Crear cita manual con upsert de paciente.
- Seguridad JWT con control por roles.
- Concurrencia por restriccion unica de base de datos en (medico, fecha_hora).
- Auditoria desacoplada por eventos de dominio.
- Pruebas unitarias de dominio.

## Arquitectura

Monolito modular por paquetes:

- modules/auth
- modules/medicos
- modules/pacientes
- modules/citas
- core y security para componentes transversales

Patrones aplicados:

- MVC: controladores, servicios y repositorios.
- Repository: persistencia desacoplada por interfaces JPA.
- Strategy: SlotService encapsula el algoritmo de slots.
- Observer: CitaCreadaEvent + AuditoriaService.
- DIP: servicios dependen de abstracciones/repositorios inyectados.

## Requisitos

- Java 21+
- Maven 3.9+

## Ejecutar

Desde la carpeta springboot-api:

mvn test
mvn spring-boot:run

Servidor por defecto: http://localhost:8090

## Credenciales semilla

Password para todos: Admin123!

- admin / ADMIN
- agendadora / AGENDADOR
- dr.mora / MEDICO

## Flujo Postman

1. Login

POST /api/v1/auth/login

{
  "login": "agendadora",
  "password": "Admin123!"
}

Copiar token de la respuesta.

2. Listar medicos

GET /api/v1/medicos
Authorization: Bearer <token>

3. Listar citas por fecha

GET /api/v1/citas?medicoId=1&fecha=2026-03-27
Authorization: Bearer <token>

4. Listar slots

GET /api/v1/citas/slots?medicoId=1&fecha=2026-03-27
Authorization: Bearer <token>

5. Crear cita manual

POST /api/v1/citas
Authorization: Bearer <token>

{
  "numDocumento": "90012345",
  "nombres": "Laura",
  "apellidos": "Martinez",
  "celular": "3110000000",
  "genero": "MUJER",
  "fechaNacimiento": "1992-02-10",
  "email": "laura@mail.com",
  "medicoId": 1,
  "fecha": "2026-03-27",
  "hora": "08:00"
}

6. Buscar paciente por documento

GET /api/v1/pacientes?documento=90012345
Authorization: Bearer <token>

## Nota de base de datos

- Por defecto usa H2 en memoria para pruebas rapidas.
- Para MySQL, configurar DB_URL, DB_USER, DB_PASSWORD y DB_DRIVER.
