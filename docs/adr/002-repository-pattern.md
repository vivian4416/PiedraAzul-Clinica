# **ADR-002: Uso del patrón Repository con Spring Data JPA para la capa de persistencia**

**Fecha:** 14/04/2026  
**Estado:** Desarrollado

---

## **Contexto**

El sistema Piedrazul requiere gestionar información crítica relacionada con citas médicas, pacientes, médicos, disponibilidad y auditoría. Estas operaciones implican acceso constante a la base de datos y ejecución de consultas que forman parte del flujo central del negocio, como la validación de disponibilidad de horarios y el registro de auditoría.

En un enfoque inicial sin un patrón definido, la lógica de acceso a datos podría mezclarse directamente con la lógica de negocio dentro de los servicios, generando alto acoplamiento, dificultad de mantenimiento y dependencia directa del motor de base de datos.

Adicionalmente, el sistema se encuentra en una fase temprana utilizando una base de datos H2 en memoria, configurada en modo compatible con MySQL, con una proyección futura de migración a un entorno productivo con MySQL. Esto requiere una arquitectura que permita desacoplar la lógica de persistencia del motor de base de datos.

Por lo tanto, se hace necesario adoptar una estrategia que permita:

* Centralizar el acceso a datos  
* Mantener separación de responsabilidades  
* Facilitar la evolución tecnológica del sistema

---

## **Decisión**

Se adopta el patrón Repository mediante el uso de Spring Data JPA como mecanismo de acceso a datos en la capa de persistencia.

Cada entidad del dominio cuenta con una interfaz de repositorio específica, tales como:

* CitaRepository  
* PacienteRepository  
* MedicoRepository  
* MedicoDisponibilidadRepository  
* AuditoriaRepository

Estas interfaces extienden `JpaRepository<Entidad, Long>`, lo que permite a Spring generar automáticamente la implementación en tiempo de ejecución, eliminando la necesidad de escribir código SQL explícito.

Las consultas de negocio se definen mediante métodos derivados por nombre (query methods), lo que permite expresar operaciones como búsquedas por rango de fechas o por entidad relacionada sin necesidad de utilizar anotaciones como `@Query` en esta fase del proyecto.

Esta capa de repositorios se integra directamente con los servicios del backend en Spring Boot, los cuales consumen los repositorios para ejecutar operaciones de negocio. A su vez, estos servicios son consumidos por el frontend desarrollado en Angular mediante APIs REST, manteniendo una arquitectura desacoplada en capas.

---

## **Consecuencias**

### **Positivas**

* Centralización del acceso a datos en una capa dedicada  
* Reducción del acoplamiento entre lógica de negocio y persistencia  
* Eliminación de SQL embebido en servicios  
* Mayor mantenibilidad y claridad en la arquitectura  
* Facilidad para migrar de H2 a MySQL sin cambios significativos en la lógica  
* Aprovechamiento de las capacidades de Spring Data JPA para generación automática de consultas  
* Mejora en la escalabilidad del sistema

### **Negativas o riesgos**

* Dependencia del framework Spring Data JPA  
* Curva de aprendizaje en el uso de métodos derivados por nombre  
* Limitaciones en consultas complejas que pueden requerir el uso de `@Query` en fases futuras  
* Posible pérdida de control fino sobre el rendimiento de consultas específicas  
* Necesidad de buenas prácticas para evitar consultas ineficientes generadas automáticamente
