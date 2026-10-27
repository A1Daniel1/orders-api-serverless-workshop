# Log de Prompts al Agente de Código

Usa este archivo para guardar **evidencia incremental** de todos los prompts que le envíes al otro agente/LLM.

## Instrucciones
- Copia el prompt **completo y literal** (tal como lo enviaste)
- Anota fecha/hora
- Indica qué querías conseguir
- Registra la respuesta relevante (resumen) o adjunta archivo en `evidence/prompts/`
- Decisión: Aceptado / Rechazado / Modificado
- Razón de la decisión
- Archivos creados/modificados

## Entradas

| Fecha | Prompt ID | Objetivo | Prompt (resumido o enlace a archivo) | Respuesta agente (resumen) | Decisión | Razón | Archivos | Notas |
|---|---|---|---|---|---|---|---|---|
| 2026-10-26 | P-001 | Diseñar la estructura de la app Java puro ANTES de generar código (Parte I) | Prompt enviado tal cual la plantilla del agente guía: app Java 17/Maven/JUnit 5, 3 operaciones (create/get/getByCustomer), storage en memoria, sin HTTP/AWS/Spring; pedido: proponer arquitectura, archivos a crear y modelado de errores antes de escribir código | Propuso 4 capas: `model` (Order inmutable, CreateOrderRequest mutable), `service` (OrderService: validación + generación de id), `repository` (InMemoryOrderRepository con LinkedHashMap), `exception` (ValidationException y OrderNotFoundException como RuntimeException genéricas). Explícitamente dejó el mapeo a HTTP para un adapter futuro | Aceptado | Respeta las restricciones (cero dependencias de HTTP/AWS/Spring), separación de responsabilidades clara y testeable. Revisado con el agente guía antes de aceptar el código | orders-api/pom.xml, orders-api/src/main/**, orders-api/src/test/** | mvn test: 20/20 OK; mvn package: thin JAR ~8.5 KB. Crítica de decisiones en AI-CRITIQUE.md (Decisiones 1-4) |
| 2026-10-26 | P-002 | Handler Lambda + fat JAR para desplegar solo createOrder (Parte II) | Prompt de arquitectura-primero: dependencias/plugins necesarias (aws-lambda-java-core, shade), ubicación del handler, instanciación del servicio por contenedor, y qué pasa con las excepciones en Lambda | Actualizó pom.xml (aws-lambda-java-core en scope **provided**, jackson-databind, maven-shade-plugin) y creó `CreateOrderHandler` como **RequestStreamHandler** con Jackson manual. Afirma que excluir com/amazonaws del JAR es "correcto" | Rechazado parcialmente | **Bug verificado**: el runtime de Lambda NO provee aws-lambda-java-core; con scope provided el JAR queda sin esas clases (verificado con unzip -l: 0 clases com/amazonaws) → NoClassDefFoundError en la primera invocación. Además el handler stream+Jackson manual evita el mapeo JSON↔Java del runtime, que es justo lo que hay que investigar (sección 7) | orders-api/pom.xml, orders-api/src/main/java/com/example/orders/lambda/CreateOrderHandler.java | Corrección solicitada en P-003. Nota: las warnings de shade (META-INF/LICENSE, module-info) son inofensivas |
| | | | | | | | | |
