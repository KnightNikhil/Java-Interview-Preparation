# Apache Camel Notes

---

## What is Routing in Apache Camel?

At its core:

Routing in Camel is about defining how messages (data/events) move from one system to another, and what transformations or decisions happen in between.

Think of it like Google Maps for data:
- You define where the message starts (`from`)
- Where it should go (`to`)
- And what rules apply on the way (transform, filter, split, aggregate, retry, etc.)

---

### 1\. Basic Idea

A route in Camel is a pipeline that connects endpoints:

```java
from("source")
    .process(...)
    .to("destination");
```

**Example:**

```java
from("file:orders")
    .to("kafka:processed-orders");
```

Here, Camel routes every file it finds in `orders` directory into a Kafka topic `processed-orders`.

---

### 2\. Routing Decisions

Camel doesn’t just move data blindly — it applies Enterprise Integration Patterns (EIPs) to make routing intelligent.

**Examples:**
- Content-based routing → Decide path based on message content

```java
from("kafka:orders")
    .choice()
        .when(body().contains("BUY"))
            .to("direct:buy")
        .when(body().contains("SELL"))
            .to("direct:sell")
        .otherwise()
            .to("direct:unknown");
```

- Multicast routing → Send message to multiple endpoints at once.
- Load balancing → Distribute messages across multiple consumers.

---

### 3\. Why Routing Matters

Without Camel, you’d have to write a lot of boilerplate code to:
- Read messages (from Kafka, DB, files, etc.)
- Parse/transform them
- Decide where to send them
- Handle errors, retries, logging

Camel simplifies this with declarative routing rules.

---

### 4\. Analogy

Imagine a postal system:
- You drop a letter (message) at a postbox (`from`)
- Sorting office checks the address (routing decision)
- It decides if it goes by air, train, or road (choice, filters, load-balancer)
- Finally, it delivers it to the destination (`to`)

Camel is like the sorting office — it ensures the right message reaches the right place in the right way.

---

**In summary:**

Routing = defining the path a message takes from a source to one or more destinations, applying rules, transformations, and error handling along the way.

---

## Why `process(new TradeValidator())` Calls `process()`

In Apache Camel:
- `process(...)` is a step in the route DSL.
- It expects a class that implements the `org.apache.camel.Processor` interface.
- That interface has exactly one method:

```java
public interface Processor {
    void process(Exchange exchange) throws Exception;
}
```

So when you do this:

```java
.process(new TradeValidator())
```

- `TradeValidator` is your custom class that implements `Processor`.
- At runtime, Camel will call its `process(Exchange exchange)` method for each message passing through the route.
- `Exchange` is the wrapper around the message (input/output, headers, metadata).

---

## TradeValidator Example Again

```java
@Component
public class TradeValidator implements Processor {
    @Override
    public void process(Exchange exchange) {
        Trade trade = exchange.getIn().getBody(Trade.class);

        if (trade.getAmount() <= 0 || trade.getSymbol() == null) {
            exchange.getIn().setHeader("valid", false);
        } else {
            exchange.getIn().setHeader("valid", true);
        }
    }
}
```

Here’s what happens:
1. Camel pulls a message from Kafka (in JSON).
2. `.unmarshal().json(JsonLibrary.Jackson, Trade.class)` converts JSON → Trade object.
3. `.process(new TradeValidator())` → Camel passes the current Exchange object into your `TradeValidator.process(exchange)` method.
4. Inside that method, you inspect the trade, and set a header `valid` with true/false.
5. The next step (`choice()`) can use that header to decide routing.

---

## Alternate Ways

Instead of writing a class, you can use a lambda because `Processor` is a functional interface:

```java
.process(exchange -> {
    Trade trade = exchange.getIn().getBody(Trade.class);
    boolean valid = trade.getAmount() > 0 && trade.getSymbol() != null;
    exchange.getIn().setHeader("valid", valid);
})
```

Same effect, shorter code.

---

**Summary:**
- `.process(...)` is a DSL hook in Camel routes.
- It expects a `Processor` implementation.
- Camel calls the `process(exchange)` method for every message passing through the route.

---

## DSL in Apache Camel

- DSL = Domain-Specific Language.
- In Camel, the Java DSL is a fluent API you use to define routes.

**Example:**

```java
from("kafka:orders")
    .filter(body().contains("BUY"))
    .to("http://localhost:8080/trade-service");
```

That’s not “general Java code” — it’s Camel’s routing DSL for describing integration flows.

---

## What I meant by “DSL hook”

When I said DSL hook, I meant:
- Methods like `.process()`, `.choice()`, `.filter()`, `.to()` are predefined entry points in Camel’s DSL.
- They act like “hooks” where you can plug in your own logic or route definitions.

So:
- `.from("source")` → Hook into a message source.
- `.to("destination")` → Hook into a message sink.
- `.process(myProcessor)` → Hook into a custom processing step.
- `.choice().when(...).otherwise(...)` → Hook into a routing decision step.

These methods are not magic — they’re part of Camel’s `RouteBuilder` fluent API.

---

## `.process()` Specifically

- `.process()` is a DSL method that tells Camel:
  > “At this point in the route, call a Processor implementation with the current message (Exchange).”

**Example:**

```java
from("kafka:orders")
    .process(new TradeValidator()) // <– Hook where you inject your custom logic
    .to("direct:notify");
```

Camel’s engine sees `.process()` in the route, and it knows it must invoke your `process(exchange)` method every time a message flows through.

---

**Summary:**
- DSL = special API for defining routes in a readable way.
- DSL hook = a point in that DSL where you “plug in” your own logic (like `.process()`).
- Camel gives you these hooks so you don’t have to write low-level boilerplate for consuming/producing messages.

---

## What is the Exchange in Apache Camel?

### 1\. Core Definition
- Exchange = a wrapper for a message as it flows through a Camel route.
- It represents a single message exchange between systems.
- It contains:
   - The input message (called In).
   - The output message (called Out, in older versions).
   - Headers (metadata).
   - Properties (context data for the whole exchange).
   - Exception (if one occurs).

Think of it like an envelope carrying the message + metadata.

---

### 2\. Exchange Structure

```
Exchange
├── In (Message)       // input message
│     ├── Body         // actual data (JSON, String, Object, etc.)
│     └── Headers      // key-value metadata
├── Out (Message)*     // output message (not used in Camel 3+, replaced by In)
├── Properties         // data shared across the route
└── Exception          // error info if something fails
```

---

### 3\. Example in a Processor

```java
.process(exchange -> {
    // Get body as a String
    String body = exchange.getIn().getBody(String.class);

    // Get a header
    String symbol = exchange.getIn().getHeader("symbol", String.class);

    // Add/modify header
    exchange.getIn().setHeader("valid", true);

    // Change the body
    exchange.getMessage().setBody(body.toUpperCase());
})
```

- `exchange.getIn()` → current message.
- `getBody()` → actual payload.
- `getHeader()` → metadata.
- `setHeader()` → add metadata.
- `getMessage()` → preferred way (Camel 3+) to modify the message going forward.

---

### 4\. Headers vs Properties

- Headers = attached to a single message (like HTTP headers).
- Properties = attached to the whole exchange (like request-scoped attributes).

**Example:**

```java
exchange.setProperty("requestId", "12345"); // valid across whole route
exchange.getIn().setHeader("operation", "BUY"); // valid only for this message
```

---

### 5\. Why is Exchange Important?

- It lets you carry data + metadata across the route.
- You can:
   - Transform body (String → JSON → POJO).
   - Add headers for routing decisions.
   - Store properties for correlation.
   - Capture exceptions.

Without Exchange, Camel would have to pass around raw objects, and you’d lose context (headers, properties, error info).

---

### 6\. Analogy

Think of Exchange as a courier package:
- In (Body) → the item you’re shipping.
- Headers → labels on the box (fragile, priority, destination).
- Properties → internal notes the courier uses (tracking ID, warehouse info).
- Exception → if something goes wrong in delivery.

---

**Summary:**
- Exchange = the context + data container for every message in Camel.
- It holds the message body, headers, properties, and errors.
- Every step in the route reads/modifies the same Exchange.

---


### 1. Writing a Camel DSL

When you write:

```java
from("jms:queue:orders")
    .filter(header("type").isEqualTo("trade"))
    .process(new TradeValidator())
    .to("jms:queue:validatedOrders");
```

this looks like a DSL, but really it’s just method calls returning builder objects.

---

### 2. How DSL translates under the hood

Each call (`from`, `filter`, `process`, `to`) creates a node in a route definition tree.
- `from("jms:queue:orders")` → creates a `RouteDefinition` with an Endpoint (`jms:queue:orders`).
- `.filter(...)` → adds a `FilterDefinition` (a processor definition that wraps a predicate).
- `.process(...)` → adds a `ProcessDefinition` that holds a reference to your `TradeValidator`.
- `.to(...)` → adds a `ToDefinition` pointing to the `jms:queue:validatedOrders` endpoint.

So internally, Camel builds a tree/chain of `ProcessorDefinition<?>` objects, not executable code yet.

**Example (simplified internal structure):**

```
RouteDefinition
└── FromDefinition("jms:queue:orders")
└── FilterDefinition(Predicate=header("type") == "trade")
└── ProcessDefinition(Processor=TradeValidator)
└── ToDefinition("jms:queue:validatedOrders")
```

---

### 3\. RouteBuilder → RouteDefinition

Your `RouteBuilder` is just a factory for building a `List<RouteDefinition>`.

```java
public abstract class RouteBuilder {
    protected abstract void configure();

    public List<RouteDefinition> getRouteCollection() {
        // when CamelContext starts, it calls this
        configure(); // your DSL runs here
        return routes; // holds the tree
    }
}
```

So when you write `from("...")`, you’re mutating an internal `RouteDefinition` list.

---

### 4. Runtime compilation to Processors

At startup, Camel takes the `RouteDefinition` tree and compiles it into a chain of `Processor` objects, which are executable.

**Example:**

```
RouteDefinition → Pipeline(processors=[
    ConsumerProcessor("jms:queue:orders"),
    FilterProcessor(predicate=header("type") == "trade",
        delegate=Pipeline(processors=[
            Processor(TradeValidator),
            ProducerProcessor("jms:queue:validatedOrders")
        ])
    )
])
```

- Each `ProcessorDefinition<?>` knows how to create a real `Processor` via `createProcessor()`.
- Camel wires these into a pipeline of processors.
- At runtime, every Exchange flows through this pipeline.

---

### 5\. Execution flow

When a message arrives:
1. `ConsumerProcessor` pulls message from `jms:queue:orders`.
2. Wraps it into an Exchange.
3. Passes it to the Pipeline.
4. `FilterProcessor` evaluates the predicate:
   - If true → forwards exchange down.
   - If false → drops exchange.
5. `TradeValidator` modifies/validates exchange.
6. `ProducerProcessor` sends it to `jms:queue:validatedOrders`.

---

So the DSL is just syntactic sugar that builds:
- RouteDefinition objects at build time,
- compiled into a processor pipeline at runtime,
- executed by passing around Exchange.

---

## Typical Apache Camel + Spring Boot Project Structure

```
my-camel-app/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.camelapp/
│   │   │       ├── MyCamelApplication.java        <-- Spring Boot entry point
│   │   │       ├── config/
│   │   │       │   ├── CamelConfig.java           <-- Camel-related configs
│   │   │       │   ├── KafkaConfig.java           <-- Kafka broker & endpoint config
│   │   │       │   └── EndpointConfig.java        <-- reusable endpoint URIs
│   │   │       ├── routes/
│   │   │       │   ├── TradeRoute.java            <-- RouteBuilder implementations
│   │   │       │   ├── NotificationRoute.java
│   │   │       │   └── AuditRoute.java
│   │   │       ├── processors/
│   │   │       │   ├── TradeValidator.java
│   │   │       │   ├── TradeEnricher.java
│   │   │       │   └── NotificationProcessor.java
│   │   │       ├── beans/
│   │   │       │   ├── TradeService.java
│   │   │       │   └── NotificationService.java
│   │   │       └── model/
│   │   │           ├── Trade.java
│   │   │           ├── Notification.java
│   │   │           └── AuditLog.java
│   │   └── resources/
│   │       ├── application.yml                     <-- Spring Boot + Camel config
│   │       ├── logback.xml                         <-- logging config
│   │       └── routes/                             <-- optional route DSL in XML/YAML
│   │
│   └── test/
│       ├── java/com.example.camelapp/
│       │   ├── TradeRouteTest.java
│       │   └── NotificationRouteTest.java
│       └── resources/
│           └── test-data/
│
├── pom.xml                                       <-- Maven dependencies
└── README.md
```

---

## How URLs fit in this structure

Camel endpoint URLs like:

```
kafka:orders?brokers=localhost:9092
file:data/inbox?noop=true
http://localhost:8080/api/validate
direct:notify
```

are part of route definitions (`routes/TradeRoute.java`) or configuration constants (`config/EndpointConfig.java`).

**Example:**

`EndpointConfig.java`

```java
@Configuration
public class EndpointConfig {

    @Value("${kafka.brokers}")
    private String kafkaBrokers;

    public String ordersKafkaEndpoint() {
        return "kafka:orders?brokers=" + kafkaBrokers;
    }

    public String fileInputEndpoint() {
        return "file:data/inbox?noop=true";
    }

    public String notifyEndpoint() {
        return "direct:notify";
    }
}
```

`TradeRoute.java`

```java
@Component
public class TradeRoute extends RouteBuilder {

    @Autowired
    private EndpointConfig endpointConfig;

    @Override
    public void configure() {
        from(endpointConfig.fileInputEndpoint())   // from file endpoint
            .process(new TradeValidator())         // custom processor
            .to(endpointConfig.ordersKafkaEndpoint()); // to Kafka endpoint
    }
}
```

---

## Why this project structure matters

- Separation of concerns → routes, processors, configs, and models are in different packages.
- Reuse → endpoint URIs are centralized in `EndpointConfig`.
- Scalability → adding new routes or processors doesn’t break existing code.
- Maintainability → configs in `application.yml` → easy to change without touching code.

---

**Example `application.yml`:**

```yaml
kafka:
  brokers: localhost:9092

camel:
  file:
    inbox: data/inbox
```

---

**Summary:**
- Endpoints URLs live in routes (or configs for reuse).
- RouteBuilder classes define the DSL.
- Processors handle business logic for each step.
- Config classes centralize endpoint definitions.
- `application.yml` → stores dynamic endpoint parameters.

---

#### Explain `file:data/inbox?noop=true`
- This is an endpoint URI for Camel’s File Component.
- `data/inbox` is a path on your local filesystem, relative to the current working directory of your application (usually where your JAR is executed).
- It is not inside your project src automatically unless you explicitly put it there and run your app from the project root.

---

## How it works

**Example project structure:**

```
my-camel-app/
├── data/
│   └── inbox/
│       ├── trade1.json
│       └── trade2.json
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
```

If you run your app from `my-camel-app/` root:

```
java -jar target/my-camel-app.jar
```

Then `file:data/inbox` will map to:

```
my-camel-app/data/inbox/
```

---

## The `noop=true` parameter

- Means: Don’t move or delete the file after reading.
- Without `noop=true`, Camel will move the file to `.camel` or delete it after processing (to avoid reprocessing).
- Useful for testing and debugging.

**Example:**

```java
from("file:data/inbox?noop=true")
    .to("log:orderFile");
```

This will:
- Read files in `data/inbox/`
- Log their content
- Keep them intact for future runs

---

## Absolute vs Relative Paths

- `file:data/inbox` → relative path (relative to the process working directory).
- `file:/var/data/inbox` → absolute path in your file system.

**Example:**

```java
from("file:/var/data/inbox?noop=true")
    .to("log:orderFile");
```

Here Camel will read files from `/var/data/inbox` regardless of where the app runs.

---

## Real Banking Project Use Case

In your high-frequency data project, file endpoints might be used for:
- Batch ingestion of trade/order data for offline processing.
- Error replay from DLQ directories.
- Audit logs in a flat file store.

---

## `http://localhost:8080/api/validate` in Camel

This is an HTTP endpoint URI used with Camel’s HTTP component (or similar, like http4, http5, or rest).

It means:

Camel will make a request to an HTTP service running locally at port 8080, under path `/api/validate`.

---

### What it Represents in Camel

- Scheme → `http`
- Host → `localhost`
- Port → `8080`
- Path → `/api/validate`

This is typically used in producer mode (with `.to()`), meaning Camel sends a message to this endpoint.

**Example:**

```java
from("direct:validateTrade")
    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
    .to("http://localhost:8080/api/validate")
    .process(exchange -> {
        String response = exchange.getMessage().getBody(String.class);
        System.out.println("Validation response: " + response);
    });
```

---

### How it Works

When Camel processes `.to("http://localhost:8080/api/validate")`:
1. It makes an HTTP call to that address.
2. By default, it uses a GET request unless overridden.
3. You can send a body with POST, PUT, etc., by setting the exchange pattern and HTTP method header.

**Example — sending a POST request:**

```java
from("direct:validateTrade")
    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
    .setBody(constant("{ \"tradeId\": \"T123\", \"amount\": 1000 }"))
    .to("http://localhost:8080/api/validate");
```

---

### Headers Camel Adds for HTTP Endpoints

When Camel sends the request, it automatically adds certain headers:
- `CamelHttpMethod` → HTTP method (GET, POST, etc.).
- `CamelHttpUri` → full URI of the request.
- `CamelHttpQuery` → query string parameters.
- `CamelHttpResponseCode` → HTTP status returned.
- `CamelHttpResponseText` → HTTP status message.

**Example after call:**

```java
Integer code = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
String response = exchange.getMessage().getBody(String.class);
System.out.println("HTTP Code: " + code);
System.out.println("Response: " + response);
```

---

### Real-World Use Case in Banking / High-Frequency Data

In your context, this endpoint could be used for:
- Trade validation service: check trade data before saving to DB.
- Risk check API: call an internal microservice to validate against risk rules.
- Enrichment service: fetch additional data before processing.

**Example:**

Kafka trade order → Camel Route → call validate API → route to DB or DLQ

---

### HTTP Endpoint Options in Camel

Camel HTTP component URIs can have query params for advanced behavior:

```
http://localhost:8080/api/validate?throwExceptionOnFailure=false&bridgeEndpoint=true
```

- `throwExceptionOnFailure=false` → don’t throw exceptions on non-200 responses.
- `bridgeEndpoint=true` → avoids Camel altering request headers/URI internally.

---

 **Summary:**
- `http://localhost:8080/api/validate` is a Camel producer endpoint to call an HTTP service.
- It’s a synchronous request (by default), following the InOut exchange pattern.
- Camel treats it like any other endpoint, wrapping the request and response in an Exchange.

