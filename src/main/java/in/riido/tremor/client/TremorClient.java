package in.riido.tremor.client;

import in.riido.tremor.client.exception.TremorClientException;
import in.riido.tremor.client.internal.TremorEventFactory;
import in.riido.tremor.client.internal.TremorValidation;
import in.riido.tremor.client.model.TremorClientInfo;
import in.riido.tremor.client.model.TremorEvent;
import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

/** Main public entry point for reporting events and throwables to Tremor. */
public final class TremorClient implements Closeable {

  /** Default Tremor base URL used when none is provided. */
  public static final String DEFAULT_BASE_URL = "http://localhost:8080/tremor";

  private static final String INGEST_PATH = "/api/v1/ingest";
  private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(5);
  private static final long DEFAULT_READ_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(10);
  private static final long DEFAULT_WRITE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(10);
  private static final long DEFAULT_CALL_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(30);

  private final TremorIngestTransport ingestTransport;
  private final TremorClientDefaults clientDefaults;
  private final boolean closeHttpClientOnClose;

  /**
   * Creates a new Tremor client builder.
   *
   * @param tremorKey the Tremor key used to authenticate requests
   * @return a new builder instance
   */
  public static Builder builder(String tremorKey) {
    return new Builder(tremorKey);
  }

  /** Creates a new TremorClient from the supplied builder state. */
  private TremorClient(Builder builder) {
    String tremorKey = TremorValidation.requireNotBlank(builder.tremorKey, "tremorKey");
    URL ingestUrl = buildIngestUrl(builder.baseUrl);
    OkHttpClient httpClient = resolveHttpClient(builder);
    this.ingestTransport = new TremorIngestTransport(tremorKey, ingestUrl, httpClient);
    this.clientDefaults = builder.buildClientDefaults();
    this.closeHttpClientOnClose = builder.httpClient == null;
  }

  /** Creates the default HTTP client configuration. */
  private static OkHttpClient createDefaultClient(Builder builder) {
    return new OkHttpClient.Builder()
        .connectTimeout(builder.connectTimeoutMillis, TimeUnit.MILLISECONDS)
        .readTimeout(builder.readTimeoutMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(builder.writeTimeoutMillis, TimeUnit.MILLISECONDS)
        .callTimeout(builder.callTimeoutMillis, TimeUnit.MILLISECONDS)
        .build();
  }

  /** Resolves the HTTP client for this Tremor client. */
  private static OkHttpClient resolveHttpClient(Builder builder) {
    if (builder.httpClient != null) {
      if (builder.hasCustomTimeouts()) {
        throw new IllegalArgumentException(
            "timeout settings cannot be used with a caller-provided httpClient");
      }
      return builder.httpClient;
    }
    return createDefaultClient(builder);
  }

  /** Builds the ingest url. */
  private static URL buildIngestUrl(String baseUrl) {
    String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
    if (normalizedBaseUrl.endsWith(INGEST_PATH)) {
      return ensureValidUrl(normalizedBaseUrl, "baseUrl");
    }

    String ingestEndpoint = normalizedBaseUrl + INGEST_PATH;
    return ensureValidUrl(ingestEndpoint, "baseUrl");
  }

  /** Normalizes the base url. */
  private static String normalizeBaseUrl(String baseUrl) {
    String normalized = TremorValidation.requireNotBlank(baseUrl, "baseUrl").trim();
    while (normalized.endsWith("/") && normalized.length() > 1) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  /** Ensures the valid url. */
  private static URL ensureValidUrl(String url, String fieldName) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(fieldName + " must be a valid absolute URL", e);
    }
  }

  /**
   * Reports the supplied event to Tremor.
   *
   * @param event the event to report
   * @return the Tremor fingerprint returned by the server
   * @throws TremorClientException when the request fails or the response is invalid
   */
  public String report(TremorEvent event) throws TremorClientException {
    return ingestTransport.report(event);
  }

  /**
   * Reports the supplied throwable to Tremor.
   *
   * @param throwable the throwable to report
   * @return the Tremor fingerprint returned by the server
   * @throws TremorClientException when the request fails or the response is invalid
   */
  public String report(Throwable throwable) throws TremorClientException {
    return report(TremorEventFactory.fromThrowable(throwable, clientDefaults));
  }

  /**
   * Reports the supplied throwable to Tremor with tags.
   *
   * @param throwable the throwable to report
   * @param tags the tags to attach to the event
   * @return the Tremor fingerprint returned by the server
   * @throws TremorClientException when the request fails or the response is invalid
   */
  public String report(Throwable throwable, List<String> tags) throws TremorClientException {
    return report(throwable, TremorCaptureOptions.builder().tags(tags).build());
  }

  /**
   * Reports the supplied throwable to Tremor with custom data.
   *
   * @param throwable the throwable to report
   * @param userCustomData the custom data to attach to the event
   * @return the Tremor fingerprint returned by the server
   * @throws TremorClientException when the request fails or the response is invalid
   */
  public String report(Throwable throwable, Map<String, ?> userCustomData)
      throws TremorClientException {
    return report(throwable, TremorCaptureOptions.builder().userCustomData(userCustomData).build());
  }

  /**
   * Reports the supplied throwable to Tremor with tags and custom data.
   *
   * @param throwable the throwable to report
   * @param tags the tags to attach to the event
   * @param userCustomData the custom data to attach to the event
   * @return the Tremor fingerprint returned by the server
   * @throws TremorClientException when the request fails or the response is invalid
   */
  public String report(Throwable throwable, List<String> tags, Map<String, ?> userCustomData)
      throws TremorClientException {
    return report(
        throwable,
        TremorCaptureOptions.builder().tags(tags).userCustomData(userCustomData).build());
  }

  /**
   * Reports the supplied throwable to Tremor with capture options.
   *
   * @param throwable the throwable to report
   * @param options the request-scoped capture options
   * @return the Tremor fingerprint returned by the server
   * @throws TremorClientException when the request fails or the response is invalid
   */
  public String report(Throwable throwable, TremorCaptureOptions options)
      throws TremorClientException {
    return report(TremorEventFactory.fromThrowable(throwable, clientDefaults, options));
  }

  /**
   * Reports the supplied event to Tremor asynchronously.
   *
   * @param event the event to report
   * @return a future that completes with the Tremor fingerprint or exceptionally on failure
   */
  public CompletableFuture<String> reportAsync(TremorEvent event) {
    return ingestTransport.reportAsync(event);
  }

  /**
   * Reports the supplied throwable to Tremor asynchronously.
   *
   * @param throwable the throwable to report
   * @return a future that completes with the Tremor fingerprint or exceptionally on failure
   */
  public CompletableFuture<String> reportAsync(Throwable throwable) {
    try {
      return reportAsync(TremorEventFactory.fromThrowable(throwable, clientDefaults));
    } catch (RuntimeException e) {
      return failedFuture(e);
    }
  }

  /**
   * Reports the supplied throwable to Tremor asynchronously with tags.
   *
   * @param throwable the throwable to report
   * @param tags the tags to attach to the event
   * @return a future that completes with the Tremor fingerprint or exceptionally on failure
   */
  public CompletableFuture<String> reportAsync(Throwable throwable, List<String> tags) {
    return reportAsync(throwable, TremorCaptureOptions.builder().tags(tags).build());
  }

  /**
   * Reports the supplied throwable to Tremor asynchronously with custom data.
   *
   * @param throwable the throwable to report
   * @param userCustomData the custom data to attach to the event
   * @return a future that completes with the Tremor fingerprint or exceptionally on failure
   */
  public CompletableFuture<String> reportAsync(Throwable throwable, Map<String, ?> userCustomData) {
    return reportAsync(
        throwable, TremorCaptureOptions.builder().userCustomData(userCustomData).build());
  }

  /**
   * Reports the supplied throwable to Tremor asynchronously with tags and custom data.
   *
   * @param throwable the throwable to report
   * @param tags the tags to attach to the event
   * @param userCustomData the custom data to attach to the event
   * @return a future that completes with the Tremor fingerprint or exceptionally on failure
   */
  public CompletableFuture<String> reportAsync(
      Throwable throwable, List<String> tags, Map<String, ?> userCustomData) {
    return reportAsync(
        throwable,
        TremorCaptureOptions.builder().tags(tags).userCustomData(userCustomData).build());
  }

  /**
   * Reports the supplied throwable to Tremor asynchronously with capture options.
   *
   * @param throwable the throwable to report
   * @param options the request-scoped capture options
   * @return a future that completes with the Tremor fingerprint or exceptionally on failure
   */
  public CompletableFuture<String> reportAsync(Throwable throwable, TremorCaptureOptions options) {
    try {
      return reportAsync(TremorEventFactory.fromThrowable(throwable, clientDefaults, options));
    } catch (RuntimeException e) {
      return failedFuture(e);
    }
  }

  /**
   * Reports the supplied throwable to Tremor and suppresses reporting failures.
   *
   * @param throwable the throwable to report
   */
  public void reportAndSuppress(Throwable throwable) {
    tryReportAndIgnoreFailures(throwable);
  }

  /**
   * Runs the supplied operation, reports any thrown exception, and suppresses the failure.
   *
   * @param runnable the operation to execute
   */
  public void reportAndSuppress(TremorRunnable runnable) {
    TremorValidation.requireNotNull(runnable, "runnable");

    try {
      runnable.run();
    } catch (Exception exception) {
      reportAndSuppress(exception);
    }
  }

  /**
   * Runs the supplied operation, reports any thrown exception with capture options, and suppresses
   * the failure.
   *
   * @param options the request-scoped capture options
   * @param runnable the operation to execute
   */
  public void reportAndSuppress(TremorCaptureOptions options, TremorRunnable runnable) {
    TremorValidation.requireNotNull(runnable, "runnable");

    try {
      runnable.run();
    } catch (Exception exception) {
      tryReportAndIgnoreFailures(exception, options);
    }
  }

  /**
   * Runs the supplied operation, reports any thrown exception, and suppresses the failure.
   *
   * @param <T> the callable result type
   * @param callable the operation to execute
   * @param fallbackValue the value to return when the callable fails
   * @return the callable result, or {@code fallbackValue} when the callable throws
   */
  public <T> T reportAndSuppress(Callable<T> callable, T fallbackValue) {
    TremorValidation.requireNotNull(callable, "callable");

    try {
      return callable.call();
    } catch (Exception exception) {
      reportAndSuppress(exception);
      return fallbackValue;
    }
  }

  /**
   * Runs the supplied operation, reports any thrown exception with capture options, and suppresses
   * the failure.
   *
   * @param <T> the callable result type
   * @param options the request-scoped capture options
   * @param callable the operation to execute
   * @param fallbackValue the value to return when the callable fails
   * @return the callable result, or {@code fallbackValue} when the callable throws
   */
  public <T> T reportAndSuppress(
      TremorCaptureOptions options, Callable<T> callable, T fallbackValue) {
    TremorValidation.requireNotNull(callable, "callable");

    try {
      return callable.call();
    } catch (Exception exception) {
      tryReportAndIgnoreFailures(exception, options);
      return fallbackValue;
    }
  }

  /** Releases any resources owned by this client. */
  @Override
  public void close() {
    if (!closeHttpClientOnClose) {
      return;
    }
    httpClient().dispatcher().executorService().shutdown();
    httpClient().connectionPool().evictAll();
  }

  /** Returns the resolved HTTP client. */
  OkHttpClient httpClient() {
    return ingestTransport.httpClient();
  }

  /** Returns whether closing this client also closes the resolved HTTP client. */
  boolean closesHttpClientOnClose() {
    return closeHttpClientOnClose;
  }

  /** Returns the resolved ingest URL. */
  URL ingestUrl() {
    return ingestTransport.ingestUrl();
  }

  /** Tries to report the supplied throwable and suppresses reporting failures. */
  private void tryReportAndIgnoreFailures(Throwable throwable) {
    try {
      report(throwable);
    } catch (TremorClientException ignored) {
      // Suppressing reporting failures is the point of this API.
    }
  }

  /** Tries to report the supplied throwable and suppresses reporting failures. */
  private void tryReportAndIgnoreFailures(Throwable throwable, TremorCaptureOptions options) {
    try {
      report(throwable, options);
    } catch (TremorClientException ignored) {
      // Suppressing reporting failures is the point of this API.
    }
  }

  /** Creates a failed future containing the supplied throwable. */
  private static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
    CompletableFuture<T> future = new CompletableFuture<T>();
    future.completeExceptionally(throwable);
    return future;
  }

  /** Builder for {@link TremorClient}. */
  public static final class Builder {

    private final String tremorKey;
    private String baseUrl = DEFAULT_BASE_URL;
    private String defaultVersion;
    private TremorClientInfo defaultClient;
    private OkHttpClient httpClient;
    private long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;
    private long writeTimeoutMillis = DEFAULT_WRITE_TIMEOUT_MILLIS;
    private long callTimeoutMillis = DEFAULT_CALL_TIMEOUT_MILLIS;
    private boolean customConnectTimeout;
    private boolean customReadTimeout;
    private boolean customWriteTimeout;
    private boolean customCallTimeout;

    /** Creates a new builder instance. */
    private Builder(String tremorKey) {
      this.tremorKey = tremorKey;
    }

    /**
     * Sets the Tremor base URL.
     *
     * @param baseUrl the Tremor base URL
     * @return this builder
     */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Sets the Tremor base URL.
     *
     * @param baseUrl the Tremor base URL
     * @return this builder
     */
    public Builder baseUrl(URL baseUrl) {
      this.baseUrl = TremorValidation.requireNotNull(baseUrl, "baseUrl").toString();
      return this;
    }

    /**
     * Sets immutable client defaults for throwable capture.
     *
     * @param defaults the client defaults to apply
     * @return this builder
     */
    public Builder defaults(TremorClientDefaults defaults) {
      TremorClientDefaults resolvedDefaults =
          defaults != null ? defaults : TremorClientDefaults.builder().build();
      this.defaultVersion = resolvedDefaults.getVersion();
      this.defaultClient = resolvedDefaults.getClient();
      return this;
    }

    /**
     * Sets the default version for throwable capture.
     *
     * @param version the default version to apply
     * @return this builder
     */
    public Builder defaultVersion(String version) {
      this.defaultVersion = version;
      return this;
    }

    /**
     * Sets the default client metadata for throwable capture.
     *
     * @param client the default client metadata to apply
     * @return this builder
     */
    public Builder defaultClient(TremorClientInfo client) {
      this.defaultClient = client;
      return this;
    }

    /**
     * Sets the shared HTTP client to use for requests.
     *
     * @param httpClient the caller-provided HTTP client
     * @return this builder
     */
    public Builder httpClient(OkHttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    /**
     * Sets the connect timeout for the library-owned HTTP client.
     *
     * @param timeout the timeout amount
     * @param unit the timeout unit
     * @return this builder
     */
    public Builder connectTimeout(long timeout, TimeUnit unit) {
      this.connectTimeoutMillis = toTimeoutMillis(timeout, unit, "connectTimeout");
      this.customConnectTimeout = true;
      return this;
    }

    /**
     * Sets the read timeout for the library-owned HTTP client.
     *
     * @param timeout the timeout amount
     * @param unit the timeout unit
     * @return this builder
     */
    public Builder readTimeout(long timeout, TimeUnit unit) {
      this.readTimeoutMillis = toTimeoutMillis(timeout, unit, "readTimeout");
      this.customReadTimeout = true;
      return this;
    }

    /**
     * Sets the write timeout for the library-owned HTTP client.
     *
     * @param timeout the timeout amount
     * @param unit the timeout unit
     * @return this builder
     */
    public Builder writeTimeout(long timeout, TimeUnit unit) {
      this.writeTimeoutMillis = toTimeoutMillis(timeout, unit, "writeTimeout");
      this.customWriteTimeout = true;
      return this;
    }

    /**
     * Sets the call timeout for the library-owned HTTP client.
     *
     * @param timeout the timeout amount
     * @param unit the timeout unit
     * @return this builder
     */
    public Builder callTimeout(long timeout, TimeUnit unit) {
      this.callTimeoutMillis = toTimeoutMillis(timeout, unit, "callTimeout");
      this.customCallTimeout = true;
      return this;
    }

    /** Returns whether any custom timeout has been configured. */
    boolean hasCustomTimeouts() {
      return customConnectTimeout || customReadTimeout || customWriteTimeout || customCallTimeout;
    }

    /** Builds the resolved client defaults. */
    TremorClientDefaults buildClientDefaults() {
      return TremorClientDefaults.builder().version(defaultVersion).client(defaultClient).build();
    }

    /** Converts the supplied timeout into milliseconds. */
    private static long toTimeoutMillis(long timeout, TimeUnit unit, String fieldName) {
      TremorValidation.requireNotNull(unit, fieldName + "Unit");
      if (timeout < 0L) {
        throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
      }
      return unit.toMillis(timeout);
    }

    /**
     * Builds the configured Tremor client.
     *
     * @return the configured Tremor client
     */
    public TremorClient build() {
      return new TremorClient(this);
    }
  }
}
