# Parte II — Despliegue de createOrder en AWS Lambda (2026-10-26)

## Función desplegada

- **Nombre:** `Orders`
- **ARN:** `arn:aws:lambda:us-east-1:694760081081:function:Orders`
- **Región:** us-east-1 (N. Virginia) — entorno educativo Vocareum
- **Runtime:** Java 17
- **Handler:** `com.example.orders.lambda.CreateOrderHandler::handleRequest`
- **Artifacto:** fat JAR `orders-api-1.0.0.jar` (~20 KB; contiene clases propias + aws-lambda-java-core; sin Jackson — el runtime hace el mapeo)

## Experimento 1 — Input válido

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

**Resultado:** succeeded. Evidencia visual: `evidence/screenshots/lambda-orders-test-invoke-success.png`

**Interpretación:** el JSON del event fue convertido en un objeto `CreateOrderRequest` (por el runtime, con su serializador interno), `OrderService.createOrder` generó el id `ORD-001` (primera invocación de este contenedor, contador inicia en 1), y el objeto `Order` retornado fue serializado de vuelta a JSON por el runtime. El código Java del handler no contiene ninguna lógica de JSON.

## Experimento 2 — Input inválido (pendiente)

- [ ] Event con `product: ""` (o quantity inválida)
- [ ] Screenshot de la respuesta
- [ ] Registrar el errorType/errorMessage observado y qué implica para el consumidor

## Observaciones / notas

- (por completar: tiempo de ejecución de la primera invocación vs segunda — cold start)
- (por completar: qué pasa con el contador de ids si se invoca de nuevo — ORD-002?)
