# Parte IV — Configuración de API Gateway (non-proxy) y evidencia de requests (2026-10-26)

## REST API

- **Name:** `orders-api` · **Stage:** `dev`
- **Endpoint:** `https://kakau23ljl.execute-api.us-east-1.amazonaws.com/dev`
- **Integración:** non-proxy (Lambda proxy desactivado) hacia la función `Orders`

## Mapping templates (Integration Request, application/json)

| Método | Template |
|---|---|
| `GET /orders/{id}` | `{ "operation": "getOrder", "id": "$input.params('id')" }` |
| `GET /orders` | `{ "operation": "getOrdersByCustomer", "customerId": "$input.params('customerId')" }` |
| `POST /orders` | `{ "operation": "createOrder", "customerId": $input.json('$.customerId'), "product": $input.json('$.product'), "quantity": $input.json('$.quantity') }` |

## Integration Responses (elegir status codes)

- default: passthrough del body (POST → **201**, GET → **200**)
- regex `.*ValidationException.*` → **400** → `{ "error": "INVALID_INPUT", "message": "$input.path('$.errorMessage')" }`
- regex `.*OrderNotFoundException.*` → **404** → `{ "error": "ORDER_NOT_FOUND", "message": "$input.path('$.errorMessage')" }`
- regex `.*Exception.*` (última en la lista) → **500** → `{ "error": "INTERNAL_ERROR", "message": "Unexpected error. See server logs." }`

## Transcripciones de curl (verificación real)

### Iteración 1 — templates mal ubicados (evidencia del proceso)

- `GET /orders/ORD-001` → **HTTP 200** con payload de error: el método tenía el template del POST → llegó `operation: createOrder` → `customerId must not be blank`.
- `GET /orders?customerId=CUST-42` y `POST /orders` → **HTTP 200**, `unknown operation: null` → sin template.
- **Lección:** API Gateway no valida la coherencia entre método y template; y sin response mappings, entrega errores como 200.

### Iteración 2 — templates corregidos (2026-10-26 ~22:27)

```text
$ curl -s -w "\nHTTP %{http_code}\n" ".../dev/orders/ORD-001"
{"id":"ORD-001","customerId":"CUST-42","product":"MacBook Air","quantity":1}
HTTP 200

$ curl -s -w "\nHTTP %{http_code}\n" ".../dev/orders?customerId=CUST-42"
[{"id":"ORD-001","customerId":"CUST-42","product":"MacBook Air","quantity":1},{"id":"ORD-002","customerId":"CUST-42","product":"iPhone","quantity":2}]
HTTP 200

$ curl -s -w "\nHTTP %{http_code}\n" -X POST ".../dev/orders" -H "Content-Type: application/json" -d '{"customerId":"CUST-42","product":"MacBook Air","quantity":1}'
{"id":"ORD-004","customerId":"CUST-42","product":"MacBook Air","quantity":1}
HTTP 200      <- correcto funcionalmente; el 201 llega con los Integration Responses

$ curl -s -w "\nHTTP %{http_code}\n" ".../dev/orders/ORD-999"
{"errorMessage":"Order ORD-999 does not exist","errorType":"...OrderNotFoundException","stackTrace":[...]}
HTTP 200      <- error crudo de Lambda entregado como 200: material del Checkpoint 3
```

Observaciones:
- `ORD-004`: el contador continuó de la semilla (ORD-001..003) → contenedor caliente, consistencia POST→GET dentro del mismo contenedor (DEC-001).
- El error crudo filtra `stackTrace` con nombres de clase internos al cliente → los Integration Responses lo corrigen.

### Iteración 3 — con status codes (pendiente de ejecución)

- [ ] GET ORD-001 → 200
- [ ] GET search CUST-42 → 200
- [ ] GET search CUST-100 → 200 con `[]`
- [ ] POST válido → 201
- [ ] GET ORD-999 → 404 con formato de error limpio
- [ ] POST inválido (quantity 0) → 400
