# ADR-001: Estandarización y redistribución del diagrama de componentes

**Fecha:** 08/04/2026
**Estado:** En desarrollo

---

## Contexto

Para acceder a la documentación completa del proyecto, se dispone del siguiente enlace:
https://docs.google.com/document/d/1hWsUDGVPsr7eGrE3gSW3bwz65zTMG07QYPwxgLFOq4Y/edit?usp=sharing

El sistema cuenta con un diagrama de componentes incluido en el documento C4. Sin embargo, debido a su tamaño y resolución, este no es legible dentro del documento. Esto dificulta la comprensión de la interacción entre módulos como el frontend en Angular, el backend en Spring Boot y las capas internas del sistema.

Como consecuencia, se genera una pérdida de trazabilidad entre la arquitectura documentada y la implementación real, afectando la comprensión del sistema por parte de nuevos desarrolladores y stakeholders.

---

## Decisión

Se decide reemplazar la inclusión de imágenes estáticas de los diagramas dentro del documento por enlaces directos a sus versiones en alta resolución.

Los diagramas serán generados utilizando PlantUML y almacenados en el repositorio del proyecto en formato fuente (.puml) y en formato renderizado (.svg) dentro de la carpeta `/docs/architecture`.

En el documento principal, se incluirán enlaces a los archivos SVG, permitiendo su visualización en alta calidad y la posibilidad de ampliación sin pérdida de resolución. Esta estrategia asegura que los diagramas se mantengan sincronizados con el código fuente y facilita su actualización mediante control de versiones.

---

## Consecuencias

### Positivas

* Mejora la legibilidad de los diagramas de arquitectura
* Permite ampliar los diagramas sin pérdida de calidad (uso de SVG)
* Facilita la trazabilidad entre documentación y código fuente
* Mejora el onboarding de nuevos desarrolladores
* Permite versionamiento y mantenimiento continuo de los diagramas

### Negativas

* Requiere tiempo adicional para mantener actualizados los diagramas
* Dependencia de enlaces externos para la visualización
* Puede generar inconvenientes en entornos sin acceso a internet

---


