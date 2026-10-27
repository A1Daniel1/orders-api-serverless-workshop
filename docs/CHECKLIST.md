# Checklist del Workshop

## 0. Preparación
- [x] Leer todo el enunciado y tener claro el objetivo
- [x] Definir roles (agente guía + espacio de evidencias)
- [x] Estructura creada

## 1. Diseño de API (Sección 4)
- [x] Definir contrato por operación (método, URL, params, body, respuestas, errores)
- [x] Definir formato JSON de error
- [x] Responder preguntas en README.md

## 2. Parte I - Java puro (Secciones 5-6)
- [x] Crear proyecto Maven Java 17 (orders-api)
- [x] Modelos (Order, CreateOrderRequest, etc.)
- [x] Lógica sin HTTP/API Gateway/Spring
- [x] Tests JUnit mínimos
- [x] `mvn test` OK
- [x] `mvn package` OK
- [x] Checkpoint 1 en README

## 3. Parte II - Lambda (Sección 7)
- [x] Desplegar una operación en AWS Lambda (función `Orders`, us-east-1)
- [x] Probar con JSON manual (3 experimentos: válido, inválido, warm start)
- [x] Investigar mapeo JSON <-> Java
- [x] Diagrama flujo JSON->Lambda->Java
- [x] Checkpoint 2

## 4. Parte III-IV - API Gateway con transformación (Secciones 8-10)
- [x] GET /orders/{id}
- [x] GET /orders?customerId=...
- [x] POST /orders
- [ ] HTTP semantics (201/400/404/500) — configuración de Integration Responses pendiente de aplicar en el API desplegado (los curl finales muestran aún 200 en errores)
- [x] Checkpoint 3 (responsabilidades API Gateway)

## 5. Parte V - Proxy Integration (Secciones 11-13)
- [x] Guardar ejemplo de evento proxy (evidence/aws/proxy-event-example.json)
- [x] Crear HTTP Adapter (OrdersProxyHandler)
- [x] POST/GET/id/GET por customer con proxy (implementado + 11 tests locales)
- [ ] Desplegar función orders-proxy en AWS (código listo en el mismo JAR)
- [x] Lógica sigue independiente de AWS

## 6. Comparativa (Sección 14)
- [x] Comparar Arquitectura A vs B
- [x] Responder todas las preguntas

## 7. AI & Crítica (Secciones 15-16)
- [x] Usar prompt estructurado al agente (arquitectura primero)
- [x] Registrar prompts en evidence/PROMPTS-LOG.md (P-001..P-005)
- [x] Criticar >= 3 decisiones en evidence/AI-CRITIQUE.md (5 decisiones)

## 8. Extensión DELETE (Sección 17)
- [x] Predicción escrita antes de implementar (docs/DELETE-PREDICTION.md, commiteada)
- [x] Implementar DELETE /orders/{id} (204 / 404) en código + tests
- [ ] Configurar método DELETE en API Gateway (mapping template + 204/404)
- [x] Comparar predicción vs implementación

## 9. Final (Secciones 18-21)
- [x] API final completa (código: 4 operaciones)
- [x] Diagrama arquitectura final (Mermaid en README)
- [ ] Video demostración (placeholder en README)
- [x] README con todo
