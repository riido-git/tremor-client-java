package in.riido.tremor.client;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.riido.tremor.client.exception.TremorApiException;
import in.riido.tremor.client.exception.TremorClientException;
import in.riido.tremor.client.exception.TremorSerializationException;
import in.riido.tremor.client.model.TremorBreadcrumb;
import in.riido.tremor.client.model.TremorClientInfo;
import in.riido.tremor.client.model.TremorEnvironment;
import in.riido.tremor.client.model.TremorError;
import in.riido.tremor.client.model.TremorEvent;
import in.riido.tremor.client.model.TremorStackFrame;
import in.riido.tremor.client.model.TremorUser;
import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TremorClientTest {

  /** Verifies that report should post the server ingest shape and return the fingerprint. */
  @Test
  @DisplayName("report should post the server ingest shape and return the fingerprint")
  void reportShouldPostTheServerIngestShapeAndReturnTheFingerprint() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"abc123\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      String fingerprint = client.report(sampleEvent());
      RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

      assertEquals("abc123", fingerprint);
      assertNotNull(request);
      assertEquals("POST", request.getMethod());
      assertEquals("/tremor/api/v1/ingest", request.getTarget());
      assertEquals("test-key", request.getHeaders().get("X-API-Key"));
      assertEquals("application/json; charset=utf-8", request.getHeaders().get("Content-Type"));

      JsonObject body = JsonParser.parseString(request.getBody().utf8()).getAsJsonObject();
      assertEquals("2026-04-01T10:15:30Z", body.get("occurredOn").getAsString());

      JsonObject details = body.getAsJsonObject("details");
      assertEquals("machine-a", details.get("machineName").getAsString());
      assertEquals("1.2.3", details.get("version").getAsString());
      assertEquals("checkout-failure", details.get("groupingKey").getAsString());
      assertEquals("critical", details.getAsJsonArray("tags").get(0).getAsString());
      assertEquals("payments", details.getAsJsonArray("tags").get(1).getAsString());
      assertEquals("12345", details.getAsJsonObject("userCustomData").get("orderId").getAsString());
      assertEquals(
          "manual",
          details.getAsJsonArray("breadcrumbs").get(0).getAsJsonObject().get("type").getAsString());

      JsonObject error = details.getAsJsonObject("error");
      assertEquals("java.lang.IllegalStateException", error.get("className").getAsString());
      assertEquals("checkout failed", error.get("message").getAsString());
      assertEquals(
          "ExampleClass",
          error
              .getAsJsonArray("stackTrace")
              .get(0)
              .getAsJsonObject()
              .get("className")
              .getAsString());

      JsonObject environment = details.getAsJsonObject("environment");
      assertEquals("Linux", environment.get("osVersion").getAsString());

      JsonObject clientInfo = details.getAsJsonObject("client");
      assertEquals("tremor-client-java", clientInfo.get("name").getAsString());

      JsonObject user = details.getAsJsonObject("user");
      assertEquals("user-123", user.get("identifier").getAsString());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report should turn non-2xx responses into TremorApiException. */
  @Test
  @DisplayName("report should turn non-2xx responses into TremorApiException")
  void reportShouldTurnNon2xxResponsesIntoTremorApiException() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(401)
            .addHeader("Content-Type", "application/json")
            .body("{\"message\":\"invalid api key\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "bad-key");
    try {
      TremorApiException exception =
          assertThrows(TremorApiException.class, () -> client.report(sampleEvent()));

      assertEquals(401, exception.getStatusCode());
      assertEquals("{\"message\":\"invalid api key\"}", exception.getResponseBody());
      assertEquals(server.url("/tremor/api/v1/ingest").toString(), exception.getUrl());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report should wrap transport failures in TremorClientException. */
  @Test
  @DisplayName("report should wrap transport failures in TremorClientException")
  void reportShouldWrapTransportFailuresInTremorClientException() throws Exception {
    MockWebServer server = new MockWebServer();
    server.start();
    String baseUrl = server.url("/tremor").toString();
    server.close();

    TremorClient client = client(baseUrl, "test-key");
    try {
      TremorClientException exception =
          assertThrows(TremorClientException.class, () -> client.report(sampleEvent()));

      assertFalse(exception instanceof TremorApiException);
      assertNotNull(exception.getCause());
      assertInstanceOf(IOException.class, exception.getCause());
    } finally {
      client.close();
    }
  }

  /** Verifies that report should reuse an ingest base url without appending the path twice. */
  @Test
  @DisplayName("report should reuse an ingest base url without appending the path twice")
  void reportShouldReuseAnIngestBaseUrlWithoutAppendingThePathTwice() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"ingest-url\"}")
            .build());
    server.start();

    URL ingestUrl = new URL(server.url("/tremor/api/v1/ingest").toString());
    TremorClient client = TremorClient.builder("test-key").baseUrl(ingestUrl).build();
    try {
      assertEquals("ingest-url", client.report(sampleEvent()));

      RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
      assertNotNull(request);
      assertEquals("/tremor/api/v1/ingest", request.getTarget());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that close should shut down only the internally resolved OkHttpClient. */
  @Test
  @DisplayName("close should shut down only the internally resolved OkHttpClient")
  void closeShouldShutDownOnlyTheInternallyResolvedOkHttpClient() {
    TremorClient internalClient = client("http://localhost:8080/tremor", "test-key");
    OkHttpClient external = new OkHttpClient();
    TremorClient externalClient = client("http://localhost:8080/tremor", "test-key", external);

    assertTrue(internalClient.closesHttpClientOnClose());
    assertFalse(externalClient.closesHttpClientOnClose());

    internalClient.close();
    externalClient.close();

    assertTrue(internalClient.httpClient().dispatcher().executorService().isShutdown());
    assertFalse(external.dispatcher().executorService().isShutdown());

    external.dispatcher().executorService().shutdown();
    external.connectionPool().evictAll();
  }

  /** Verifies that build should apply configured timeouts to the library-owned HTTP client. */
  @Test
  @DisplayName("build should apply configured timeouts to the library-owned HTTP client")
  void buildShouldApplyConfiguredTimeoutsToTheLibraryOwnedHttpClient() {
    TremorClient client =
        TremorClient.builder("test-key")
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .callTimeout(4, TimeUnit.SECONDS)
            .build();

    try {
      assertEquals(1000, client.httpClient().connectTimeoutMillis());
      assertEquals(2000, client.httpClient().readTimeoutMillis());
      assertEquals(3000, client.httpClient().writeTimeoutMillis());
      assertEquals(4000, client.httpClient().callTimeoutMillis());
    } finally {
      client.close();
    }
  }

  /** Verifies that build should reject timeout knobs when a caller provides the HTTP client. */
  @Test
  @DisplayName("build should reject timeout knobs when a caller provides the HTTP client")
  void buildShouldRejectTimeoutKnobsWhenACallerProvidesTheHttpClient() {
    OkHttpClient external = new OkHttpClient();
    try {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  TremorClient.builder("test-key")
                      .httpClient(external)
                      .connectTimeout(1, TimeUnit.SECONDS)
                      .build());

      assertTrue(exception.getMessage().contains("caller-provided httpClient"));
    } finally {
      external.dispatcher().executorService().shutdown();
      external.connectionPool().evictAll();
    }
  }

  /** Verifies that build should reject invalid event payloads before any HTTP call. */
  @Test
  @DisplayName("build should reject invalid event payloads before any HTTP call")
  void buildShouldRejectInvalidEventPayloadsBeforeAnyHttpCall() throws Exception {
    MockWebServer server = new MockWebServer();
    server.start();

    List<String> tags = new ArrayList<String>();
    for (int i = 0; i < 21; i++) {
      tags.add("tag-" + i);
    }

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> TremorEvent.builder().error(sampleError()).tags(tags).build());

    assertTrue(exception.getMessage().contains("tags"));
    assertNull(server.takeRequest(200, TimeUnit.MILLISECONDS));
    server.close();
  }

  /** Verifies that report should reject malformed Tremor success responses. */
  @Test
  @DisplayName("report should reject malformed Tremor success responses")
  void reportShouldRejectMalformedTremorSuccessResponses() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"status\":\"accepted\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      TremorSerializationException exception =
          assertThrows(TremorSerializationException.class, () -> client.report(sampleEvent()));

      assertTrue(exception.getMessage().contains("fingerprint"));
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report should reject blank Tremor success response bodies. */
  @Test
  @DisplayName("report should reject blank Tremor success response bodies")
  void reportShouldRejectBlankTremorSuccessResponseBodies() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      TremorSerializationException exception =
          assertThrows(TremorSerializationException.class, () -> client.report(sampleEvent()));

      assertTrue(exception.getMessage().contains("body"));
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report should reject blank Tremor fingerprints. */
  @Test
  @DisplayName("report should reject blank Tremor fingerprints")
  void reportShouldRejectBlankTremorFingerprints() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"   \"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      TremorSerializationException exception =
          assertThrows(TremorSerializationException.class, () -> client.report(sampleEvent()));

      assertTrue(exception.getMessage().contains("fingerprint"));
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report should reject malformed JSON in Tremor success responses. */
  @Test
  @DisplayName("report should reject malformed JSON in Tremor success responses")
  void reportShouldRejectMalformedJsonInTremorSuccessResponses() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      TremorSerializationException exception =
          assertThrows(TremorSerializationException.class, () -> client.report(sampleEvent()));

      assertTrue(exception.getMessage().contains("parse"));
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report async should return the fingerprint. */
  @Test
  @DisplayName("reportAsync should return the fingerprint")
  void reportAsyncShouldReturnTheFingerprint() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"async123\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      CompletableFuture<String> future = client.reportAsync(sampleEvent());

      assertEquals("async123", future.get(1, TimeUnit.SECONDS));
      assertNotNull(server.takeRequest(1, TimeUnit.SECONDS));
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report async should complete exceptionally for malformed success responses. */
  @Test
  @DisplayName("reportAsync should complete exceptionally for malformed success responses")
  void reportAsyncShouldCompleteExceptionallyForMalformedSuccessResponses() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"status\":\"accepted\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      ExecutionException exception =
          assertThrows(
              ExecutionException.class,
              () -> client.reportAsync(sampleEvent()).get(1, TimeUnit.SECONDS));

      assertInstanceOf(TremorSerializationException.class, exception.getCause());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report throwable should build the Tremor payload internally. */
  @Test
  @DisplayName("report throwable should build the Tremor payload internally")
  void reportThrowableShouldBuildTheTremorPayloadInternally() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"throwable123\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      Throwable throwable =
          new IllegalStateException("checkout failed", new NullPointerException("orderId missing"));

      TremorCaptureOptions options =
          TremorCaptureOptions.builder()
              .tag("payments")
              .putUserCustomData("orderId", "12345")
              .version("2.0.0")
              .build();

      String fingerprint = client.report(throwable, options);
      RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

      assertEquals("throwable123", fingerprint);
      assertNotNull(request);

      JsonObject details =
          JsonParser.parseString(request.getBody().utf8())
              .getAsJsonObject()
              .getAsJsonObject("details");

      JsonObject error = details.getAsJsonObject("error");
      assertEquals("java.lang.IllegalStateException", error.get("className").getAsString());
      assertEquals("checkout failed", error.get("message").getAsString());
      assertTrue(error.getAsJsonArray("stackTrace").size() > 0);

      JsonObject innerError = error.getAsJsonObject("innerError");
      assertEquals("java.lang.NullPointerException", innerError.get("className").getAsString());
      assertEquals("orderId missing", innerError.get("message").getAsString());

      assertEquals("payments", details.getAsJsonArray("tags").get(0).getAsString());
      assertEquals("12345", details.getAsJsonObject("userCustomData").get("orderId").getAsString());
      assertEquals("2.0.0", details.get("version").getAsString());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that report throwable should reject null throwables. */
  @Test
  @DisplayName("report throwable should reject null throwables")
  void reportThrowableShouldRejectNullThrowables() {
    try (TremorClient client = client("http://localhost:8080/tremor", "test-key")) {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> client.report((Throwable) null));

      assertEquals("throwable is required", exception.getMessage());
    }
  }

  /** Verifies that report should apply immutable client defaults before request options. */
  @Test
  @DisplayName("report should apply immutable client defaults before request options")
  void reportShouldApplyImmutableClientDefaultsBeforeRequestOptions() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"defaults123\"}")
            .build());
    server.start();

    TremorClient client =
        TremorClient.builder("test-key")
            .baseUrl(server.url("/tremor").toString())
            .defaultVersion("1.0.0")
            .defaultClient(new TremorClientInfo("orders-service", "1.0.0", "https://example.com"))
            .build();
    try {
      TremorCaptureOptions options =
          TremorCaptureOptions.builder().version("2.0.0").tag("payments").build();

      assertEquals(
          "defaults123", client.report(new IllegalStateException("checkout failed"), options));

      RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
      assertNotNull(request);

      JsonObject details =
          JsonParser.parseString(request.getBody().utf8())
              .getAsJsonObject()
              .getAsJsonObject("details");
      assertEquals("2.0.0", details.get("version").getAsString());
      assertEquals("orders-service", details.getAsJsonObject("client").get("name").getAsString());
      assertEquals("payments", details.getAsJsonArray("tags").get(0).getAsString());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that reportAndSuppress throwable should report the exception directly. */
  @Test
  @DisplayName("reportAndSuppress throwable should report the exception directly")
  void reportAndSuppressThrowableShouldReportTheExceptionDirectly() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"suppressed-throwable\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      client.reportAndSuppress(new IllegalStateException("ignored failure"));

      RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
      assertNotNull(request);

      JsonObject details =
          JsonParser.parseString(request.getBody().utf8())
              .getAsJsonObject()
              .getAsJsonObject("details");
      assertEquals(
          "java.lang.IllegalStateException",
          details.getAsJsonObject("error").get("className").getAsString());
      assertEquals(
          "ignored failure", details.getAsJsonObject("error").get("message").getAsString());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that reportAndSuppress should report the exception and continue. */
  @Test
  @DisplayName("reportAndSuppress should report the exception and continue")
  void reportAndSuppressShouldReportTheExceptionAndContinue() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"suppressed123\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      client.reportAndSuppress(
          TremorCaptureOptions.builder().tag("suppressed").build(),
          new TremorRunnable() {
            /** Runs the callback body. */
            @Override
            public void run() {
              throw new IllegalStateException("ignored failure");
            }
          });

      RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
      assertNotNull(request);

      JsonObject details =
          JsonParser.parseString(request.getBody().utf8())
              .getAsJsonObject()
              .getAsJsonObject("details");
      assertEquals("suppressed", details.getAsJsonArray("tags").get(0).getAsString());
      assertEquals(
          "java.lang.IllegalStateException",
          details.getAsJsonObject("error").get("className").getAsString());
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that reportAndSuppress callable should return the fallback value on exception. */
  @Test
  @DisplayName("reportAndSuppress callable should return the fallback value on exception")
  void reportAndSuppressCallableShouldReturnTheFallbackValueOnException() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(
        new MockResponse.Builder()
            .code(202)
            .addHeader("Content-Type", "application/json")
            .body("{\"fingerprint\":\"fallback123\"}")
            .build());
    server.start();

    TremorClient client = client(server.url("/tremor").toString(), "test-key");
    try {
      String value =
          client.reportAndSuppress(
              TremorCaptureOptions.builder().tag("fallback").build(),
              () -> {
                throw new IOException("boom");
              },
              "fallback-value");

      assertEquals("fallback-value", value);
      assertNotNull(server.takeRequest(1, TimeUnit.SECONDS));
    } finally {
      client.close();
      server.close();
    }
  }

  /** Verifies that reportAndSuppress throwable should ignore reporting failures too. */
  @Test
  @DisplayName("reportAndSuppress throwable should ignore reporting failures too")
  void reportAndSuppressThrowableShouldIgnoreReportingFailuresToo() throws Exception {
    MockWebServer server = new MockWebServer();
    server.start();
    String baseUrl = server.url("/tremor").toString();
    server.close();

    try (TremorClient client = client(baseUrl, "test-key")) {
      client.reportAndSuppress(new IllegalArgumentException("local failure"));
    }
  }

  /** Creates a sample event for the tests. */
  private TremorEvent sampleEvent() {
    Map<String, Object> breadcrumbCustomData = new LinkedHashMap<String, Object>();
    breadcrumbCustomData.put("step", "payment");

    return TremorEvent.builder()
        .occurredOn(OffsetDateTime.parse("2026-04-01T10:15:30Z"))
        .machineName("machine-a")
        .version("1.2.3")
        .groupingKey("checkout-failure")
        .error(sampleError())
        .environment(
            TremorEnvironment.builder()
                .osVersion("Linux")
                .architecture("amd64")
                .processorCount(8)
                .locale("en-IN")
                .utcOffset(5.5d)
                .build())
        .client(new TremorClientInfo("tremor-client-java", "0.1.0", "https://example.com/client"))
        .tag("critical")
        .tag("payments")
        .putUserCustomData("orderId", "12345")
        .user(new TremorUser("user-123", "user@example.com", "Jane Doe"))
        .breadcrumb(
            TremorBreadcrumb.builder()
                .message("started checkout")
                .category("lifecycle")
                .level(1)
                .type("manual")
                .timestamp(1717171717L)
                .className("CheckoutService")
                .methodName("submit")
                .lineNumber(42)
                .customData(breadcrumbCustomData)
                .build())
        .build();
  }

  /** Creates a sample error for the tests. */
  private TremorError sampleError() {
    return TremorError.builder()
        .className("java.lang.IllegalStateException")
        .message("checkout failed")
        .frame(
            new TremorStackFrame(
                "ExampleClass", "exampleMethod", "Example.java", Integer.valueOf(42)))
        .innerError(
            TremorError.builder()
                .className("java.lang.NullPointerException")
                .message("orderId was null")
                .build())
        .build();
  }

  /** Creates a test client for the supplied configuration. */
  private TremorClient client(String baseUrl, String tremorKey) {
    return TremorClient.builder(tremorKey).baseUrl(baseUrl).build();
  }

  /** Creates a test client for the supplied configuration. */
  private TremorClient client(String baseUrl, String tremorKey, OkHttpClient httpClient) {
    return TremorClient.builder(tremorKey).baseUrl(baseUrl).httpClient(httpClient).build();
  }
}
