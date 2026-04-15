
# **ADR-003: Implementación del patrón Observer mediante eventos para desacoplar la auditoría del agendamiento de citas**

**Fecha:** 14/04/2026  
**Estado:** Desarrollado

---

## **Contexto**

El sistema Piedrazul requiere registrar acciones críticas del negocio, como la creación de citas médicas, con fines de auditoría. Este registro debe realizarse de manera confiable cada vez que ocurre un evento relevante en el sistema.

En un enfoque tradicional, la lógica de auditoría podría implementarse directamente dentro del servicio de citas (`CitaService`), generando una dependencia directa entre el módulo de agendamiento y el módulo de auditoría. Esto provocaría un alto acoplamiento, dificultando el mantenimiento del sistema y limitando su capacidad de evolución.

Adicionalmente, el sistema podría requerir en el futuro nuevas funcionalidades que reaccionen ante la creación de una cita, como el envío de notificaciones o integraciones con sistemas externos.

Por lo tanto, se requiere una solución que permita:

* Desacoplar la lógica de auditoría del proceso de creación de citas  
* Permitir la extensión del sistema mediante nuevos comportamientos reactivos  
* Mantener una arquitectura flexible y escalable

---

## **Decisión**

Se implementa el patrón Observer utilizando el sistema de eventos proporcionado por Spring Boot.

El servicio `CitaService` publica un evento de dominio (`CitaCreadaEvent`) mediante el uso de `ApplicationEventPublisher` cada vez que se registra una nueva cita.

El servicio `AuditoriaService` actúa como suscriptor del evento, escuchándolo mediante la anotación `@EventListener` y ejecutando la lógica correspondiente para persistir el registro de auditoría.

Esta implementación permite una comunicación basada en eventos (Publish–Subscribe), donde el emisor del evento no tiene conocimiento de los consumidores, logrando un desacoplamiento total entre los módulos.

El backend desarrollado en Spring Boot gestiona estos eventos de manera interna, mientras que el frontend en Angular consume únicamente los resultados de las operaciones a través de APIs REST, sin verse afectado por la lógica de auditoría.

Este diseño también permite la incorporación futura de nuevos suscriptores (por ejemplo, servicios de notificación) sin necesidad de modificar la lógica existente en `CitaService`.

---

## **Consecuencias**

### **Positivas**

* Desacoplamiento total entre la lógica de citas y la auditoría  
* Mayor flexibilidad para agregar nuevos comportamientos reactivos  
* Mejora en la mantenibilidad del sistema  
* Facilita la escalabilidad mediante la incorporación de nuevos suscriptores  
* Implementación alineada con principios de arquitectura orientada a eventos  
* Reducción del impacto de cambios en módulos independientes

### **Negativas o riesgos**

* Mayor complejidad en la comprensión del flujo de ejecución (eventos no visibles de forma directa)  
* Dificultad para depuración (debugging) al involucrar múltiples componentes desacoplados  
* Posible sobreuso de eventos si no se gestionan adecuadamente  
* Dependencia del mecanismo de eventos de Spring  
* Riesgo de pérdida de trazabilidad si no se documentan correctamente los eventos y sus consumidores

---

