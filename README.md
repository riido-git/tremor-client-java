# tremor-client-java

`tremor-client-java` is a lightweight Java client for reporting exceptions and explicit error events to Tremor.

It targets Java 8, uses OkHttp for HTTP transport, Gson for JSON serialization, and keeps the public API small.

[![Maven Central](https://img.shields.io/maven-central/v/in.riido/tremor-client-java)](https://central.sonatype.com/artifact/in.riido/tremor-client-java)

## Requirements

- Java 8 or higher
- A Tremor server exposing `/tremor/api/v1/ingest`

## Maven

```xml
<dependency>
    <groupId>in.riido</groupId>
    <artifactId>tremor-client-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Gradle

```groovy
implementation 'in.riido:tremor-client-java:0.1.0'
```

## Kotlin DSL

```kotlin
implementation("in.riido:tremor-client-java:0.1.0")
```

## Quick Start

```java
import in.riido.tremor.client.TremorClient;

try (TremorClient client = TremorClient.builder("your-tremor-key").build()) {
  String fingerprint = client.report(new IllegalStateException("checkout failed"));
}
```

Default base URL:

```text
http://localhost:8080/tremor
```

## Common Usage

### Report a Throwable

```java
import in.riido.tremor.client.TremorCaptureOptions;
import in.riido.tremor.client.TremorClient;

public final class OrderService {

  private final TremorClient tremorClient =
      TremorClient.builder("your-tremor-key")
          .baseUrl("https://tremor.example.com/tremor")
          .build();

  public void processOrder(String orderId) {
    try {
      chargePayment(orderId);
      reserveInventory(orderId);
    } catch (Exception exception) {
      tremorClient.report(
          exception,
          TremorCaptureOptions.builder()
              .tag("orders")
              .tag("payment")
              .putUserCustomData("orderId", orderId)
              .build());
      throw exception;
    }
  }
}
```

The client maps the throwable into Tremor's event shape, including:

- exception type
- message
- cause chain
- stack trace frames

### Report Asynchronously

```java
import java.util.concurrent.CompletableFuture;

CompletableFuture<String> future =
    tremorClient.reportAsync(new IllegalStateException("checkout failed"));
```

The future completes with the Tremor fingerprint or completes exceptionally with the same client exception types used by the synchronous API.

### Report and Suppress

Use this only when swallowing the original failure is intentional.

```java
try {
  refreshCache();
} catch (Exception exception) {
  tremorClient.reportAndSuppress(exception);
}
```

There are also runnable and callable overloads for best-effort work.

### Report an Explicit Event

```java
import in.riido.tremor.client.TremorClient;
import in.riido.tremor.client.model.TremorError;
import in.riido.tremor.client.model.TremorEvent;
import in.riido.tremor.client.model.TremorStackFrame;

TremorError error =
    TremorError.builder()
        .className("java.lang.IllegalStateException")
        .message("checkout failed")
        .frame(new TremorStackFrame("CheckoutService", "submit", "CheckoutService.java", 42))
        .build();

TremorEvent event =
    TremorEvent.builder().machineName("api-1").version("1.2.3").error(error).build();

try (TremorClient client = TremorClient.builder("your-tremor-key").build()) {
  String fingerprint = client.report(event);
}
```

## Client Construction

`TremorClient` is built through `TremorClient.builder(...)` and is intended to be reused.

```java
TremorClient client =
    TremorClient.builder("your-tremor-key")
        .baseUrl("https://tremor.example.com/tremor")
        .build();
```

### Base URL

Both `String` and `URL` forms are supported.

```java
import java.net.URL;

TremorClient.builder("your-tremor-key")
    .baseUrl("https://tremor.example.com/tremor")
    .build();

TremorClient.builder("your-tremor-key")
    .baseUrl(new URL("https://tremor.example.com/tremor"))
    .build();
```

### Builder Timeouts

Timeout knobs apply only when the library creates the `OkHttpClient`.

```java
import java.util.concurrent.TimeUnit;

TremorClient client =
    TremorClient.builder("your-tremor-key")
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .build();
```

If you pass your own `OkHttpClient`, configure timeouts on that client instead.

### Shared OkHttpClient

```java
import okhttp3.OkHttpClient;

OkHttpClient sharedClient = new OkHttpClient();

TremorClient client =
    TremorClient.builder("your-tremor-key")
        .baseUrl("https://tremor.example.com/tremor")
        .httpClient(sharedClient)
        .build();
```

Ownership rules:

- If you pass an `OkHttpClient`, Tremor uses that instance.
- If you do not pass one, Tremor creates one internally.
- `TremorClient.close()` only shuts down the internally created client.
- A caller-provided `OkHttpClient` is never shut down by the library.

### Immutable Client Defaults

For application-level metadata that should be reused across throwable reports, configure defaults once on the client.

```java
import in.riido.tremor.client.TremorClientDefaults;
import in.riido.tremor.client.model.TremorClientInfo;

TremorClient client =
    TremorClient.builder("your-tremor-key")
        .defaults(
            TremorClientDefaults.builder()
                .version("1.2.3")
                .client(new TremorClientInfo("orders-service", "1.2.3", "https://example.com"))
                .build())
        .build();
```

Per-request `TremorCaptureOptions` take precedence over these defaults.

## Error Handling

The client uses checked exceptions for runtime reporting failures.

- `IllegalArgumentException` indicates invalid client configuration or invalid event payload.
- `TremorClientException` indicates a transport failure or general client-side reporting failure.
- `TremorApiException` indicates Tremor responded with a non-2xx status.
- `TremorSerializationException` indicates request or response JSON could not be serialized or parsed correctly.

## Public API

Primary API:

- `report(TremorEvent event)`
- `report(Throwable throwable)`
- `report(Throwable throwable, TremorCaptureOptions options)`
- `reportAsync(TremorEvent event)`
- `reportAsync(Throwable throwable)`
- `reportAsync(Throwable throwable, TremorCaptureOptions options)`
- `reportAndSuppress(Throwable throwable)`

Additional convenience overloads are available for tags, custom data, runnable suppression, and callable suppression.

## Development

Build and run tests:

```bash
mvn -q test
```

Install locally:

```bash
mvn -q install
```
