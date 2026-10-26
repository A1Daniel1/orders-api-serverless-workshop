# Contrato de la API (Sección 4 del taller)

> Rellena esto ANTES de escribir código Java. Es tu primer entregable de diseño.

## Recurso: Orders

### Operación 1 — POST /orders

- **Método:** POST
- **URL:** /orders
- **Path params:** ninguno
- **Query params:** ninguno
- **Request body:**
  ```json
  {
    "customerId": "...",
    "product": "...",
    "quantity": 1
  }
  ```
- **Respuesta exitosa — status:** ¿200 o 201? (decide y justifica)
- **Respuesta exitosa — body:**
  ```json
  ? (¿incluye el id? ¿quién genera el id?)
  ```
- **Errores posibles:**
  | Caso | Status | error code |
  |---|---|---|
  | ? | ? | ? |

### Operación 2 — GET /orders/{id}

- **Método:** GET
- **URL:** /orders/{id}
- **Path params:** ¿cuál y con qué formato?
- **Query params:** ninguno
- **Request body:** ninguno
- **Respuesta exitosa — status:** ?
- **Respuesta exitosa — body:** ?
- **Errores posibles:**
  | Caso | Status | error code |
  |---|---|---|

### Operación 3 — GET /orders?customerId={customerId}

- **Método:** GET
- **URL:** /orders
- **Path params:** ninguno
- **Query params:** ?
- **Respuesta exitosa — status:** ¿200 incluso con lista vacía? (decide y justifica)
- **Respuesta exitosa — body:** ¿array JSON directo o envuelto en objeto?
- **Errores posibles:**
  | Caso | Status | error code |
  |---|---|---|

## Formato de errores (consistente en toda la API)

```json
{
  "error": "NOMBRE_EN_MAYUSCULAS",
  "message": "descripción legible"
}
```

- ¿Qué campos debe tener siempre un error?
- ¿Código de error para validación fallida?
- ¿Código de error para orden no encontrada?

## Reglas de validación (¿qué es "input inválido"?)

- ¿customerId obligatorio? ¿formato?
- ¿product obligatorio?
- ¿quantity? ¿qué valores son válidos (0? negativo? límite superior?)

---

## Preguntas de diseño del README (responde con tus palabras)

1. ¿Cuáles son los recursos en tu API?
2. ¿Por qué `/orders/{id}` para recuperar una orden particular?
3. ¿Por qué `customerId` es query parameter en la búsqueda?
4. ¿Qué debe devolver `POST /orders` al crear exitosamente?
5. ¿Qué debe devolver la API cuando una orden no existe?
6. ¿Qué información debe contener una respuesta de error?
