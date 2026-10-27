# DEC-001 — Una sola función Lambda con dispatcher para toda la Orders API

**Fecha:** 2026-10-26 · **Fase:** Parte III/IV · **Decidida por:** Daniel (con revisión del agente guía)

## Contexto

El agente de código propuso una función Lambda por operación (P-004): `CreateOrderHandler`, `GetOrderHandler`, `GetOrdersByCustomerHandler`. Cada una con su propio `InMemoryOrderRepository`.

**Problema detectado:** funciones separadas = almacenes separados → `GET /orders/{id}` **nunca** encontraría órdenes creadas por `POST /orders` (la API violaría su propio contrato). Además, aun con una sola función, el estado en memoria es por-contenedor y efímero (sección 3 del enunciado: *"in-memory data must not be considered persistent storage"*).

## Decisión

**Una sola función Lambda** (`OrdersApiHandler`) con un campo `operation` en el input que la despacha a la operación correspondiente. Los mapping templates de API Gateway (non-proxy) construyen ese input.

## Alternativas consideradas

| Alternativa | Pros | Contras |
|---|---|---|
| **A. Una función por operación** | Patrón non-proxy "puro"; cada integración apunta a su función | 3 almacenes aislados (POST→GET incoherente SIEMPRE); 3 funciones que administrar/pagar/borrar; requiere sembrar datos en cada una; las 2 funciones extra mueren al llegar la Parte V |
| **B. Una función + dispatcher** ✔ | Coherencia POST→GET dentro del contenedor caliente (mejor esfuerzo posible sin DB); 1 recurso AWS que administrar; transición natural a la Parte V (proxy suele usar 1 función) | El handler tiene un routing (debe mantenerse fino); el input DTO necesita campos opcionales; "routing dentro de Lambda" es una decisión criticable (le quita trabajo de routing a API Gateway) |
| C. Storage externo (DynamoDB) | Coherencia garantizada | Fuera de alcance del taller (no requiere DB); costo/riesgo en Vocareum; complica la Parte I |

## Consecuencias que se aceptan

1. POST→GET funciona de manera confiable solo si la invocación cae en el mismo contenedor caliente. Con semilla de datos predefinidos, los GET de demostración son deterministas.
2. Los ids sembrados (ORD-001..003) conviven con los generados; POST crea desde ORD-004.
3. El dispatcher mueve una responsabilidad de routing a Lambda que en la arquitectura "A" tendría API Gateway — es exactamente el trade-off que se analizará en el Checkpoint 3 y la sección 14.

## Estado de los handlers de P-004

Los 3 handlers por-operación se **eliminan** del código fuente (quedan en el historial Git como evidencia de la propuesta del agente y de esta decisión). Sus tests se reemplazan por tests del dispatcher.
