# Checklist del Workshop

## 0. Preparación
- [ ] Leer todo el enunciado y tener claro el objetivo
- [ ] Definir roles (agente guía + espacio de evidencias)
- [ ] Estructura creada

## 1. Diseño de API (Sección 4)
- [x] Definir contrato por operación (método, URL, params, body, respuestas, errores)
- [x] Definir formato JSON de error
- [x] Responder preguntas en README.md

## 2. Parte I - Java puro (Secciones 5-6)
- [x] Crear proyecto Maven Java 17 (orders-api)
- [x] Modelos (Order, CreateOrderRequest, etc.)
- [x] Lógica sin HTTP/API Gateway/Spring
- [x] Tests JUnit mínimos
- [x] `mvn test` OK (20/20)
- [x] `mvn package` OK (thin JAR 8.5 KB)
- [x] Checkpoint 1 en README

## 3. Parte II - Lambda (Sección 7)
- [ ] Desplegar una operación en AWS Lambda
- [ ] Probar con JSON manual
- [ ] Investigar mapeo JSON <-> Java
- [ ] Diagrama flujo JSON->Lambda->Java
- [ ] Checkpoint 2

## 4. Parte III-IV - API Gateway con transformación (Secciones 8-10)
- [ ] GET /orders/{id}
- [ ] GET /orders?customerId=...
- [ ] POST /orders
- [ ] HTTP semantics (200,201,400,404)
- [ ] Checkpoint 3 (responsabilidades API Gateway)

## 5. Parte V - Proxy Integration (Secciones 11-13)
- [ ] Guardar ejemplo de evento proxy
- [ ] Crear HTTP Adapter/Handler
- [ ] POST/GET/id/GET por customer con proxy
- [ ] Respuestas HTTP correctas + posible Location
- [ ] Lógica sigue independiente de AWS

## 6. Comparativa (Sección 14)
- [ ] Comparar Arquitectura A vs B
- [ ] Responder todas las preguntas

## 7. AI & Crítica (Secciones 15-16)
- [ ] Usar prompt estructurado al agente
- [ ] Registrar prompts en `evidence/prompts/`
- [ ] Criticar >= 3 decisiones en `evidence/AI-CRITIQUE.md`

## 8. Extensión DELETE (Sección 17)
- [ ] Predicción escrita antes de implementar
- [ ] Implementar DELETE /orders/{id} (204 / 404)
- [ ] Comparar predicción vs propuesta agente

## 9. Final (Secciones 18-21)
- [ ] API final completa
- [ ] Diagrama arquitectura final
- [ ] Video demostración
- [ ] README con todo
