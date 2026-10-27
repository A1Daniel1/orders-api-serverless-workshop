# Predicción antes de implementar DELETE /orders/{id} (Sección 17)

> Escrito ANTES de pedirle la implementación al agente de código. Commit como evidencia.

## Mi predicción

**1. ¿Qué clases Java deben cambiar?**
- `OrderService`: nuevo método `deleteOrder(String id)` que delega al repositorio y lanza `OrderNotFoundException` si no existe.
- `InMemoryOrderRepository`: nuevo método `delete(String id)` (el `Map.remove` ya devuelve el valor eliminado o `null`).
- `OrdersApiHandler`: nuevo caso `deleteOrder` en el switch (input: solo `operation` + `id`; ningún DTO nuevo).
- `Order`, `OrdersApiRequest`, `CreateOrderRequest`, excepciones: **sin cambios**.
- Tests: nuevo caso en `OrderServiceTest` (borrar existente / borrar inexistente) y en el test del handler (dispatch de deleteOrder, not-found propagada).

**2. ¿Necesito una función Lambda nueva?**
No — DEC-001: la función `Orders` única crece con un caso más del switch. Solo se resube el JAR.

**3. ¿Necesita cambiar la configuración de API Gateway?**
Sí: método `DELETE /orders/{id}` nuevo (Method Request + Integration Request non-proxy con mapping template `{ "operation": "deleteOrder", "id": "$input.params('id')" }`) + Integration Response para sus códigos: default → **204** (con template que deje el body vacío) y la regex `.*OrderNotFoundException.*` → **404**.

**4. ¿Dónde debe vivir la lógica de borrado?**
En `OrderService` (regla de negocio: qué significa borrar) que a su vez delega el almacenamiento en el repository.

**5. ¿Dónde debe vivir la decisión de devolver 204 vs 404?**
La *condición* (existe/no existe) la detecta el service lanzando o no `OrderNotFoundException`; la *traducción a HTTP* en esta arquitectura (A) la hace API Gateway: 204 para la respuesta default del DELETE, 404 vía regex de la excepción. En la arquitectura B (proxy), esa decisión viviría en el adapter.

**6. ¿Qué tests deben agregarse?**
- Servicio: `deleteOrder` de una orden existente la elimina (verificación: `getOrder` después lanza not-found) y devuelve/queda vacía; borrar una inexistente lanza `OrderNotFoundException`.
- Repository: `delete` existente/inexistente.
- Handler: `deleteOrder` despacha correctamente y propaga la not-found.

## Comparación con la propuesta del agente

**Nota honesta:** por tiempo de entrega, el prompt P-006 con esta predicción fue preparado pero **no llegó a enviarse al agente de código**; la implementación la realizó directamente el agente guía (Daniel la aprobó como predicción). La comparación "agente vs predicción" se convirtió en "implementación vs predicción":

**Coincidencias (implementación = predicción):**
- `OrderService.deleteOrder(String)` que lanza `OrderNotFoundException` si no existe ✔
- `InMemoryOrderRepository.delete(String)` con semántica de `Map.remove` ✔
- Nuevo caso `deleteOrder` en el switch del `OrdersApiHandler`, sin DTO nuevo ✔
- Sin función Lambda nueva (misma función `Orders`, solo resubir JAR) ✔
- 204 vs 404: la *condición* en el service (excepción), la *traducción HTTP* en API Gateway (default → 204 con template vacío, regex `.*OrderNotFoundException.*` → 404) ✔
- Tests: servicio (existe/no existe), repositorio, handler (dispatch + not-found) ✔

**Diferencias:** ninguna estructural. La única decisión fina fue qué devuelve el handler en el caso 204: `null` (el runtime serializa como payload vacío y el Integration Response del DELETE lo mapea a body vacío), tal como se especificó en el prompt.

**Qué se implementó finalmente y por qué:** exactamente la predicción — el ejercicio de predicción sirvió para confirmar que la extensión no rompía la arquitectura: el cambio quedó contenido en las capas correctas (repository + service + una línea de dispatch), sin tocar modelos ni excepciones.
