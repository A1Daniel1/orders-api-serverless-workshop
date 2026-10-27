# AI Assistant Critique (Section 16)

Selected non-trivial decisions made by the coding agent during Part I, evaluated as engineering decisions (not whether the code compiled).

## Decision 1 — `OrderService` depends on the concrete `InMemoryOrderRepository`, not on an interface

- **Decision made by the agent:**
  The service takes `InMemoryOrderRepository` directly in its constructor instead of depending on a `OrderRepository` abstraction.

- **Why do I think it proposed this?**
  Fewer files, less ceremony. For a 3-operation workshop API, an interface feels like over-engineering to an LLM optimizing for the shortest path.

- **Do you agree?**
  Partially. Acceptable for this workshop's scope, but the whole point of the workshop is keeping the core independent from infrastructure. A repository interface would make that boundary explicit and would let a future durable implementation (DynamoDB, etc.) replace the in-memory one without touching `OrderService`.

- **Alternative that could have been used:**
  Define a `OrderRepository` interface (save/findById/findByCustomerId) and have `InMemoryOrderRepository` implement it.

- **What was finally implemented and why:**
  The agent's version was kept as-is for now, accepting the trade-off for this phase. If Part II/III requires swapping storage or mocking in tests, this is the first refactor to make.

## Decision 2 — `CreateOrderRequest` was designed as a mutable POJO (no-arg constructor + setters) "to make later JSON deserialization easy"

- **Decision made by the agent:**
  A mutable data carrier with getters/setters, explicitly anticipating Jackson-style deserialization in the AWS Lambda phase.

- **Why do I think it proposed this?**
  The agent knows the target platform: AWS SDK request/response objects deserialized with Jackson require a no-arg constructor. It optimized for the future step instead of the current one.

- **Do you agree?**
  Partially. It is pragmatic and will save friction in Part II, but it lets a serialization concern (a detail of the *AWS adapter*) influence the design of a *core* class before any HTTP code exists. An immutable type would be safer and more honest to the current phase.

- **Alternative that could have been used:**
  An immutable class (or a Java 17 `record`) in the core, and let the future adapter deserialize its own AWS-shaped DTO and map it into the core type.

- **What was finally implemented and why:**
  The mutable POJO was kept, consciously accepting the small leak, because the adapter will need it and re-mapping adds boilerplate for little benefit at this size.

## Decision 3 — Asymmetric validation: `createOrder` enforces the `CUST-<digits>` format, but `getOrder`/`getOrdersByCustomer` only check for blank values

- **Decision made by the agent:**
  Creation validates the customerId format with a regex, but reads accept any non-blank id/customerId.

- **Why do I think it proposed this?**
  It treats creation as "guarded input" and reads as "lookup by whatever was given". Lenient reads mean a malformed id simply matches nothing and surfaces as not-found.

- **Do you agree?**
  Partially — it is defensible for `GET /orders/{id}`: an unknown id is a `404`, and validating the format would return `400` instead, which is also reasonable. But it creates an inconsistency: the *search* by `customerId` does not apply the format rule that *creation* enforces, so `GET /orders?customerId=garbage` silently returns `200 []` instead of `400`.

- **Alternative that could have been used:**
  Either validate the format on every entry point (consistent `400`s) or don't validate format anywhere at the HTTP level and document ids as opaque strings.

- **What was finally implemented and why:**
  Implemented as the agent proposed for now, with the design decision documented in the README: a malformed order id is treated as not found (`404`), not as a bad request. The asymmetry for the search is still an open point to settle when the HTTP adapter is built.

## Decision 4 — Sequential id generation with `AtomicInteger` (`ORD-001`, `ORD-002`, ...)

- **Decision made by the agent:**
  Ids are generated from an in-memory counter formatted as `ORD-%03d`.

- **Why do I think it proposed this?**
  Matches the example data in the problem statement and is simple to reason about.

- **Do you agree?**
  Mostly. For an in-memory, ephemeral store it is fine, but it hides an important fact: on AWS each Lambda container has its own counter, so ids restart and can collide across warm containers. The design should not pretend the counter is durable.

- **Alternative that could have been used:**
  A UUID, or a sequence coordinated by whatever durable store would exist in a real system.

- **What was finally implemented and why:**
  Kept, since the workshop explicitly allows non-persistent in-memory data and predefined information. The ephemeral caveat is documented here and in the README.

## Decision 5 — `aws-lambda-java-core` marked as scope `provided`, excluded from the fat JAR

- **Decision made by the agent:**
  The Lambda dependency was added with `<scope>provided</scope>`, so the shade plugin excluded it from the deployed JAR — and the agent's report even presented this as "correctly excluded".

- **Why do I think it proposed this?**
  The agent applied a habit from servlet/Web development, where the server *does* provide the API at runtime (e.g. `javax.servlet` in Tomcat). It mapped that pattern onto Lambda without checking whether the Lambda Java runtime actually provides these classes.

- **Do you agree?**
  No. This one was wrong, not just debatable: the AWS Lambda Java runtime does **not** provide `aws-lambda-java-core`. Deploying that JAR would fail on the first invocation with `NoClassDefFoundError` when the runtime tried to load a handler implementing `RequestStreamHandler`.

- **Alternative that could have been used:**
  Default scope (`compile`), so the small (~150 KB) library is bundled in the fat JAR. Or, if a "provided-style" assumption was made, it should have been verified against AWS documentation and against the actual JAR contents — the verification I ran (`unzip -l`) showed zero `com/amazonaws` entries.

- **What was finally implemented and why:**
  Correction requested (P-003): bundle the library in the JAR. Lesson recorded: verify the agent's claims with direct inspection — the agent inspected the JAR itself and still called the exclusion "correct".

## General reflection

- **Did the agent think about architecture or just about compiling code?**
  It thought about architecture more than average: it separated model/service/repository/exceptions, kept AWS out, and its Javadoc explains *why* (e.g. exceptions note that HTTP mapping is a later adapter concern). However, it anticipated AWS requirements (mutable POJO for Jackson) before they were needed — thinking ahead, but that ahead-thinking is itself a subtle coupling. In Part II it produced code that compiled and passed tests, yet carried a real deployment-breaking assumption ("provided" scope) presented as verified-correct — compiling green is not the same as working on the platform.

- **Did it force me to think before implementing?**
  Yes — the prompt asked for architecture proposal first, and the class layout was reviewed before accepting code.

- **What would I ask it next time?**
  To present trade-offs explicitly (e.g. interface vs concrete repository) instead of silently choosing; and to distinguish decisions forced by AWS from decisions that are good plain-Java design, so they can be made consciously.
