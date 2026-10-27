# Parte II — Despliegue de createOrder en AWS Lambda (2026-10-26)

## Función desplegada

- **Nombre:** `Orders`
- **ARN:** `arn:aws:lambda:us-east-1:694760081081:function:Orders`
- **Región:** us-east-1 (N. Virginia) — entorno educativo Vocareum
- **Runtime:** Java 17
- **Handler:** `com.example.orders.lambda.CreateOrderHandler::handleRequest`
- **Artifacto:** fat JAR `orders-api-1.0.0.jar` (~20 KB; contiene clases propias + aws-lambda-java-core; sin Jackson — el runtime hace el mapeo con su serializador interno)

## Experimento 1 — Input válido (cold start)

**Event (JSON de entrada):**
```json
{ "customerId": "CUST-42", "product": "MacBook Air", "quantity": 1 }
```

**Respuesta (JSON de salida):**
```json
{
  "id": "ORD-001",
  "customerId": "CUST-42",
  "product": "MacBook Air",
  "quantity": 1
}
```

**Resultado:** succeeded. Evidencia: `evidence/screenshots/lambda-orders-test-invoke-success.png`

**Interpretación:** el runtime convirtió el JSON del event en `CreateOrderRequest`, el servicio generó el id `ORD-001` (contador arranca en 1 en un contenedor nuevo), y el `Order` retornado fue serializado a JSON por el runtime. El handler no contiene ninguna lógica de JSON.

## Experimento 2 — Input inválido

**Event:**
```json
{ "customerId": "CUST-42", "product": "", "quantity": 1 }
```

**Respuesta (error):**
```json
{
  "errorMessage": "product must not be blank",
  "errorType": "com.example.orders.exception.ValidationException",
  "stackTrace": [
    "com.example.orders.service.OrderService.validateRequest(OrderService.java:65)",
    "com.example.orders.service.OrderService.createOrder(OrderService.java:31)",
    "com.example.orders.lambda.CreateOrderHandler.handleRequest(CreateOrderHandler.java:34)",
    "...reflection frames del runtime..."
  ]
}
```

**Resultado:** la invocación figura como **failed**. Evidencia: `evidence/screenshots/lambda-orders-test-invoke-invalid.png`

**Interpretación:** la excepción Java de validación se propagó hasta el runtime, que la empaquetó como error de función (errorMessage/errorType/stackTrace). **No hay código HTTP en ninguna parte**: un error de función Lambda no es un 400. La traducción excepción → status HTTP (400 `INVALID_INPUT`) será responsabilidad de la capa HTTP (API Gateway o el adapter de las partes IV/V). Si se invocara por CLI, la metadata de la respuesta llevaría `FunctionError`.

## Experimento 3 — Segunda invocación válida (warm start)

**Event:** el mismo válido del experimento 1.

**Respuesta:**
```json
{
  "id": "ORD-002",
  "customerId": "CUST-42",
  "product": "MacBook Air",
  "quantity": 1
}
```

**Resultado:** succeeded, execution time ~0 segundos. Evidencia: `evidence/screenshots/lambda-orders-test-invoke-warm-start-ord002.png`

**Interpretación:** dos verificaciones de un golpe:
1. **Warm start:** el runtime instanció el handler una sola vez por contenedor y lo reusa entre invocaciones (por eso el `OrderService` y su contador sobrevivieron a la primera invocación, y el tiempo cayó a ~0s).
2. **Estado no persistente pero no reiniciado:** el `AtomicInteger` siguió de `1` a `2` dentro del mismo contenedor. Si el contenedor se recicla (o si otro contenedor paralelo atiende la invocación), el contador vuelve a `1` → ids duplicados. Es exactamente el riesgo documentado en `AI-CRITIQUE.md` (Decisión 4): el estado en memoria es efímero y no coordinado.
