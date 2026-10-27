# Orders API

A small Order Management REST API designed, implemented and deployed progressively during the "Serverless REST API" workshop, using Java 17, Maven, AWS Lambda and Amazon API Gateway.

The API contract is designed first (this document). The implementation is added in later phases: first as plain Java, then deployed to AWS Lambda, and finally exposed through Amazon API Gateway.

## API Design

The API exposes a single resource: **Orders**. Customers and products are not resources of this API; they only appear as data inside an order.

An order is represented as:

```json
{
  "id": "ORD-001",
  "customerId": "CUST-42",
  "product": "MacBook Air",
  "quantity": 1
}
```

### Operations

#### Create an order — `POST /orders`

- **Path parameters:** none
- **Query parameters:** none
- **Request body:**

```json
{
  "customerId": "CUST-42",
  "product": "MacBook Air",
  "quantity": 1
}
```

- **Success:** `201 Created`, with the created order in the response body. The server generates the order `id`. A `Location` header points to the new resource: `/orders/{id}`.
- **Errors:**

| Case | HTTP status | Error code |
|---|---|---|
| Invalid request body (missing/blank fields, non-positive quantity) | 400 Bad Request | `INVALID_INPUT` |

#### Retrieve an order — `GET /orders/{id}`

- **Path parameters:** `id` — the order identifier (e.g. `ORD-42`)
- **Query parameters:** none
- **Request body:** none
- **Success:** `200 OK`, with the full order in the response body.
- **Errors:**

| Case | HTTP status | Error code |
|---|---|---|
| No order exists with that id | 404 Not Found | `ORDER_NOT_FOUND` |

#### Find orders by customer — `GET /orders?customerId={customerId}`

- **Path parameters:** none
- **Query parameters:** `customerId` (required) — the customer identifier (e.g. `CUST-42`)
- **Request body:** none
- **Success:** `200 OK`, with a JSON array of orders belonging to that customer. If the customer has no orders, the array is empty (`[]`) — this is a successful search without results, not an error.
- **Errors:**

| Case | HTTP status | Error code |
|---|---|---|
| Missing or blank `customerId` | 400 Bad Request | `INVALID_INPUT` |

### Error response format

All errors use a consistent JSON representation:

```json
{
  "error": "ORDER_NOT_FOUND",
  "message": "Order ORD-999 does not exist"
}
```

- `error`: a stable, machine-readable error code.
- `message`: a human-readable description of what happened.

The HTTP status code already carries the outcome of the request, so it is not repeated inside the body.

### Validation rules

- `customerId`: required, format `CUST-<digits>`.
- `product`: required, non-empty.
- `quantity`: positive integer (>= 1).

## Design Decisions

1. **What are the resources in this API?**
   Only one resource: orders. Customers and products appear as attributes of an order, but the API does not expose endpoints for them, so they are not resources.

2. **Why `/orders/{id}` to retrieve a particular order?**
   REST models a collection (`/orders`) and its individual items (`/orders/{id}`). The hierarchical path identifies the collection first and one specific item second, which is the standard and predictable way to address a single resource.

3. **Why is `customerId` a query parameter in the search operation?**
   Because the search filters the orders collection; it does not address a customer resource. Using `/orders/{customerId}` would collide with `/orders/{orderId}` and would imply a "customer" resource that this API does not have.

4. **What should `POST /orders` return when an order is successfully created?**
   `201 Created`, the created order (including the server-generated `id`) in the body, and a `Location` header with the URL of the new resource.

5. **What should the API return when an order does not exist?**
   `404 Not Found` with the standard error body (`ORDER_NOT_FOUND`), so clients can distinguish "bad request" from "resource not found".

6. **What information should an error response contain?**
   A stable machine-readable error code and a human-readable message. The HTTP status code conveys the outcome; the body explains it.

## Implementation (Part I — Plain Java)

The application logic is implemented as ordinary Java 17 with no HTTP, API Gateway, Spring or web-server dependencies.

```
orders-api/src/main/java/com/example/orders/
├── model/
│   ├── Order.java                   Immutable value object (id, customerId, product, quantity)
│   └── CreateOrderRequest.java      Raw input for creation; carries no validation logic
├── service/
│   └── OrderService.java            Validation, id generation and business operations
├── repository/
│   └── InMemoryOrderRepository.java In-memory storage backed by a LinkedHashMap
└── exception/
    ├── ValidationException.java     Invalid input
    └── OrderNotFoundException.java  Referenced order does not exist
```

**Where each responsibility lives:**

- **Model** — plain data; no validation, no infrastructure concerns.
- **OrderService** — application logic: validates input (customerId format `CUST-<digits>`, non-empty product, quantity >= 1), generates sequential ids (`ORD-001`, ...), and delegates storage to the repository.
- **InMemoryOrderRepository** — in-memory persistence; preserves insertion order so search results are deterministic. Not durable: Lambda execution environments are ephemeral.
- **Exceptions** — plain Java `RuntimeException`s carrying only a human-readable message. The mapping from these exceptions to HTTP status codes (`400`/`404`) deliberately lives nowhere yet; it will be introduced by a later HTTP adapter so the core stays infrastructure-free.

**Tests (JUnit 5):** 50 tests in total — service and repository logic (20), the non-proxy dispatcher (15), the proxy HTTP adapter (11), and repository/service edge cases added by the DELETE extension — covering creation of a valid order, retrieval of an existing order, behavior when an order does not exist, searching by customer (including the empty case), deletion, and invalid input (null/blank/malformed fields, zero and negative quantity).

### Checkpoint 1

- **Can the application functionality be tested without AWS?**
  Yes. The service layer is plain Java over an in-memory repository; `mvn test` runs 50 JUnit tests without any AWS dependency.

- **Does the application require a web server?**
  No. Operations are plain method calls; nothing listens on a port.

- **Does it require Spring Boot?**
  No. The only dependency is JUnit, and only at test scope.

- **What does the generated JAR contain?**
  Only the compiled application classes (a "thin" JAR, ~8.5 KB). It does not bundle any dependencies. When deploying to Lambda in Part II, a shaded (fat) JAR including the AWS SDK libraries will be required.

- **Which parts of the application currently know about HTTP?**
  None. Validation and not-found failures are thrown as generic Java exceptions; choosing the HTTP status code for each will be the job of a future adapter.

## Part II — Deploying to AWS Lambda

The `createOrder` operation was deployed as the `Orders` function (Java 17, region `us-east-1`):

- **Handler:** `com.example.orders.lambda.CreateOrderHandler::handleRequest`
- `CreateOrderHandler` is a typed `RequestHandler<CreateOrderRequest, Order>` and is the **only class in the project importing AWS types**.
- The deployed artifact is a fat JAR (maven-shade-plugin) bundling `aws-lambda-java-core`. It does **not** bundle Jackson: JSON conversion is performed by the Lambda runtime itself.

### Investigation: how does the Lambda Java runtime map JSON to Java?

Deployed behavior (verified against the running function, see `evidence/aws/lambda-parte2.md` and `evidence/screenshots/`):

1. **Instantiation.** The runtime instantiates the handler class once per *execution environment* using its no-arg constructor, and reuses that object across invocations (warm start). This is why `OrderService` (and its in-memory repository + id counter) is created in the constructor.
2. **JSON → Java.** For a typed `RequestHandler<I, R>`, the runtime uses its **built-in serializer** (an internal Jackson `ObjectMapper` owned by the runtime, not by the application) to deserialize the event JSON into the declared input type, `CreateOrderRequest`.
3. **Java → JSON.** The returned `Order` object is serialized back to JSON by the same runtime serializer. Our fat JAR contains zero Jackson classes, yet the mapping still happened — the conversion is entirely runtime-owned.
4. **POJO shape matters.** The runtime's serializer requires the input type to have a no-arg constructor and setters (`CreateOrderRequest`), and reads the output type through its getters (`Order`). This is why the mutable `CreateOrderRequest` decision made in Part I paid off here.
5. **Errors are not HTTP.** An input that fails validation propagated as a raw function error: `errorType: ValidationException`, `errorMessage: "product must not be blank"` plus a stack trace. The invocation is simply "failed"; no HTTP status exists at this layer. Translating domain exceptions into HTTP codes (`400`, `404`) is the job of a later HTTP-facing layer (API Gateway or an adapter).

### Checkpoint 2

```mermaid
flowchart TD
    A["JSON event<br/>{customerId, product, quantity}"] --> B["AWS Lambda Runtime<br/>(built-in serializer)"]
    B -- "deserialize" --> C["CreateOrderRequest<br/>(Java object)"]
    C --> D["CreateOrderHandler<br/>(only AWS-aware class)"]
    D --> E["OrderService.createOrder()<br/>validate + generate id"]
    E --> F["Order<br/>(Java object)"]
    F -- "serialize" --> G["JSON response<br/>{id, customerId, product, quantity}"]
```

Observed with the deployed function: input `{"customerId":"CUST-42","product":"MacBook Air","quantity":1}` → output `{"id":"ORD-001",...}` on the first invocation; a second valid invocation on the same warm container returned `ORD-002` with ~0s execution time, confirming handler reuse; an invalid input produced a raw `ValidationException` error payload with no HTTP semantics.

## Part III/IV — Exposing the API through API Gateway (non-proxy)

A **REST API** (`orders-api`, stage `dev`) exposes the three operations through **non-proxy integrations** pointing to the single `Orders` function. API Gateway mapping templates transform each HTTP request into the JSON payload the dispatcher expects, injecting the `operation` field (DEC-001):

| Method | Mapping template (application/json) |
|---|---|
| `GET /orders/{id}` | `{ "operation": "getOrder", "id": "$input.params('id')" }` |
| `GET /orders` | `{ "operation": "getOrdersByCustomer", "customerId": "$input.params('customerId')" }` |
| `POST /orders` | `{ "operation": "createOrder", "customerId": $input.json('$.customerId'), "product": $input.json('$.product'), "quantity": $input.json('$.quantity') }` |

The Java application never sees HTTP: it receives a small JSON document whose shape was designed by the mapping template.

### HTTP semantics (status codes)

With non-proxy integration, status codes are chosen by API Gateway **Integration Response** rules, matched by regex against the Lambda response body:

| Lambda outcome | Integration response | Status | Body |
|---|---|---|---|
| Return value (success) | default mapping (POST → 201, GET → 200) | 200 / 201 | passthrough of the returned object |
| `ValidationException` | regex `.*ValidationException.*` | 400 | `{ "error": "INVALID_INPUT", "message": ... }` |
| `OrderNotFoundException` | regex `.*OrderNotFoundException.*` | 404 | `{ "error": "ORDER_NOT_FOUND", "message": ... }` |
| Any other exception | catch-all regex `.*Exception.*` (listed last) | 500 | `{ "error": "INTERNAL_ERROR", "message": "Unexpected error. See server logs." }` |

This hides the raw Lambda error payload (`errorMessage`/`errorType`/`stackTrace`) from clients and turns Java exceptions into proper HTTP responses.

**A notable limitation discovered here:** a `Location` header on `201 Created` is impractical in this architecture — API Gateway header mappings can only derive headers from integration *headers*, not from the response *body*, where the generated id lives. This is a concrete disadvantage of Architecture A that the proxy architecture (Part V) does not have.

### Checkpoint 3 — what is API Gateway responsible for in this architecture?

Routing (resource/method tree); extracting path parameters (`id`) and query parameters (`customerId`); transforming requests (mapping templates → dispatcher payload); **dispatching by injecting the `operation` field** (in this design, API Gateway decides which business operation is invoked); transforming responses (regex-based error mapping); choosing status codes (201 vs 200, 400/404/500 from exception types); hiding internal error details (stack traces never reach the client).

**Advantages of this distribution:**
- The Java application stays domain-pure: no HTTP, no AWS event shapes, no status codes in code.
- HTTP semantics live in one place (API Gateway configuration), decoupled from business logic.
- Small JAR; no HTTP or event-format dependencies in the application.
- Clients get consistent error documents even though Lambda has no concept of HTTP.

**Disadvantages:**
- Configuration-heavy and error-prone: templates live in the console, not in version control; a misplaced template produces confusing cross-operation bugs (during the workshop, `GET /orders/{id}` briefly dispatched `createOrder` because it carried the POST template, and missing templates produced `unknown operation: null`).
- Changes require redeployment to take effect — easy to forget, hard to diagnose from the client side (we observed `HTTP 200` with error payloads before configuring response mappings).
- The HTTP behavior of the API is spread across three mapping templates + four integration responses per concern — harder to review, diff, or transfer to another team member than code.
- Dynamic response headers (e.g. `Location`) derived from the response body are not practically achievable.
- Velocity Template Language is an additional, legacy-flavored syntax the team must maintain.

## Part V — Lambda proxy integration (Architecture B)

An alternative architecture exposes the same API with **Lambda proxy integration**: API Gateway forwards the *complete* HTTP request to Lambda as-is, and a thin **HTTP adapter** inside the Lambda produces the full HTTP response. Implemented in `OrdersProxyHandler`, deployed as a second function (`orders-proxy`) reusing the same fat JAR, so Architecture A keeps working untouched.

The adapter uses the AWS-provided event types (`aws-lambda-java-events`): `RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>`. An example of the proxy event that API Gateway sends is stored in `evidence/aws/proxy-event-example.json`. Where each piece of the HTTP request appears in the event:

| Information | Field in the proxy event |
|---|---|
| HTTP method | `httpMethod` |
| Path | `path` (and `resource` for the configured route) |
| Path parameters | `pathParameters` → `{"id": "ORD-001"}` |
| Query parameters | `queryStringParameters` → `{"customerId": "CUST-42"}` |
| Headers | `headers` |
| Request body | `body` (a JSON **string**; `null` when there is no body) |

The adapter routes by method + path, calls the same `OrderService` as Architecture A, and serializes the HTTP response itself:

- success → status codes 200 / 201 / 204 set **in Java**, in one place;
- `ValidationException` → 400 `INVALID_INPUT`, `OrderNotFoundException` → 404 `ORDER_NOT_FOUND`, unexpected exceptions → 500 `INTERNAL_ERROR` — all caught in one try/catch;
- a **`Location` header** is set on 201 Created (`/orders/{id}`) — trivial here because the adapter owns the full response, unlike Architecture A where it is practically impossible.

The application logic (`OrderService`) is unchanged in both architectures: it still deals only with orders, customers, products and quantities — never with HTTP.

## Comparison of the Two Architectures (Section 14)

| Question | A — API Gateway transformation (non-proxy) | B — Proxy + HTTP adapter |
|---|---|---|
| More API Gateway configuration? | **A.** 3 mapping templates + 4 integration-response rules per method; all in the console, not in version control. | B needs almost none: proxy on + Lambda target. |
| More Java infrastructure code? | A: almost none. | **B.** The adapter (~150 lines): routing, parsing, response building. |
| Easier to test locally? | **B.** The adapter is a plain class testable with event POJOs (11 unit tests). Architecture A's templates can only be verified against the deployed API (we found this the hard way: templates misplaced and a missing redeploy produced confusing failures). | |
| Where are status codes defined? | A: in API Gateway Integration Response mappings (console). | B: in the adapter's Java code (versioned, reviewable). |
| API grows from 3 to 30 operations? | A scales badly: 30 methods × (template + response mappings), each a manual console step with cross-method mistakes possible — we hit exactly that with 3 methods. | B scales in code: the routing block grows, but every change is a code review, not a console operation. |
| Cleaner Java business logic? | **A.** The core knows zero HTTP; API Gateway does everything. | B is nearly as clean: the core is untouched; HTTP lives confined in the adapter. |
| Easier for another developer? | **B.** Everything is in the repository: code + tests + (optionally) infra-as-code. Architecture A requires console archaeology to understand the API. | |
| Choice for a larger system? | **B (proxy)** — testable, versionable, and the standard practice in the industry (serverless frameworks like SAM/CDK/Terraform assume it). Architecture A remains attractive when you want the HTTP layer managed by a platform team without touching application code, or for small APIs with stable contracts. | |

## Final Architecture (Section 19)

```mermaid
flowchart TD
    C["Client<br/>(curl / Postman)"] -- "HTTP request" --> AG
    subgraph AWS["AWS (cloud)"]
        AG["API Gateway REST API<br/>orders-api (stage dev)"]
        AG -- "Architecture A: mapping template<br/>{operation, id / customerId / body}" --> L1["Lambda: Orders<br/>OrdersApiHandler (dispatcher)"]
        AG -- "Architecture B: proxy event<br/>(full HTTP request)" --> L2["Lambda: orders-proxy<br/>OrdersProxyHandler (HTTP adapter)"]
        L1 -- "JSON (runtime-serialized)" --> SVC
        L2 -- "Java method calls" --> SVC
    end
    SVC["Application logic (plain Java)<br/>OrderService · Order · CreateOrderRequest ·<br/>InMemoryOrderRepository · domain exceptions"]

    style SVC fill:#e8f5e9
    style AG fill:#fff3e0
    style L1 fill:#e3f2fd
    style L2 fill:#e3f2fd
```

Dependencies: **API Gateway and the Lambda runtime are AWS-managed**. `OrdersApiHandler` and `OrdersProxyHandler` depend on AWS types (the only two classes that import AWS). Everything below the handlers — `OrderService`, models, repository, exceptions — is ordinary Java and knows nothing about HTTP or AWS.

## Demonstration (Section 21)

(Video placeholder — recording pending.)

The video shows: `mvn test`, `mvn package`, a manual Lambda invocation, and the deployed API answering 200, 201, 400, 404 and the DELETE 204/404 extension, plus the final architecture diagram.

## Build and Test

```bash
mvn test     # run unit tests (50 tests, all passing)
mvn package  # package the application (fat JAR: target/orders-api-1.0.0.jar)
```

Handler strings:
- Architecture A (non-proxy dispatcher): `com.example.orders.lambda.OrdersApiHandler::handleRequest`
- Architecture B (proxy): `com.example.orders.lambda.OrdersProxyHandler::handleRequest` — deployed as a second function (`orders-proxy`) reusing the same JAR.
```
