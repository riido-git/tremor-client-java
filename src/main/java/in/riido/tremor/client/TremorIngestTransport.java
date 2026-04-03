package in.riido.tremor.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import in.riido.tremor.client.exception.TremorApiException;
import in.riido.tremor.client.exception.TremorClientException;
import in.riido.tremor.client.exception.TremorSerializationException;
import in.riido.tremor.client.internal.TremorJsonSerializer;
import in.riido.tremor.client.internal.TremorValidation;
import in.riido.tremor.client.model.TremorEvent;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

final class TremorIngestTransport {

  private static final String API_KEY_HEADER = "X-API-Key";
  private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

  private final String tremorKey;
  private final URL ingestUrl;
  private final OkHttpClient httpClient;
  private final TremorJsonSerializer jsonSerializer;

  /** Creates a new TremorIngestTransport instance. */
  TremorIngestTransport(String tremorKey, URL ingestUrl, OkHttpClient httpClient) {
    this.tremorKey = TremorValidation.requireNotBlank(tremorKey, "tremorKey");
    this.ingestUrl = TremorValidation.requireNotNull(ingestUrl, "ingestUrl");
    this.httpClient = TremorValidation.requireNotNull(httpClient, "httpClient");
    this.jsonSerializer = new TremorJsonSerializer(new Gson());
  }

  /** Reads the response body when present. */
  private static String readResponseBody(Response response) throws IOException {
    return response.body() != null ? response.body().string() : null;
  }

  /** Creates a failed future containing the supplied throwable. */
  private static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
    CompletableFuture<T> future = new CompletableFuture<T>();
    future.completeExceptionally(throwable);
    return future;
  }

  /** Parses the Tremor fingerprint from the response body. */
  private static String parseFingerprint(String responseBody) throws TremorSerializationException {
    if (responseBody == null || responseBody.trim().isEmpty()) {
      throw new TremorSerializationException("Tremor response did not include a body");
    }

    try {
      JsonElement element = JsonParser.parseString(responseBody);
      if (!element.isJsonObject()) {
        throw new TremorSerializationException("Tremor response body was not a JSON object");
      }

      return requireFingerprint(element.getAsJsonObject());
    } catch (JsonParseException e) {
      throw new TremorSerializationException("Failed to parse Tremor response", e);
    }
  }

  /** Returns the required Tremor fingerprint from the supplied response object. */
  private static String requireFingerprint(JsonObject responseObject)
      throws TremorSerializationException {
    JsonElement fingerprintElement = responseObject.get("fingerprint");
    if (fingerprintElement == null || fingerprintElement.isJsonNull()) {
      throw new TremorSerializationException("Tremor response did not include a fingerprint");
    }

    String fingerprint = fingerprintElement.getAsString();
    if (fingerprint == null || fingerprint.trim().isEmpty()) {
      throw new TremorSerializationException("Tremor response fingerprint was blank");
    }
    return fingerprint;
  }

  /** Reports the supplied event to Tremor. */
  String report(TremorEvent event) throws TremorClientException {
    return execute(buildRequest(event));
  }

  /** Reports the supplied event to Tremor asynchronously. */
  CompletableFuture<String> reportAsync(TremorEvent event) {
    try {
      return executeAsync(buildRequest(event));
    } catch (RuntimeException e) {
      return failedFuture(e);
    } catch (TremorClientException e) {
      return failedFuture(e);
    }
  }

  /** Returns the resolved HTTP client. */
  OkHttpClient httpClient() {
    return httpClient;
  }

  /** Returns the resolved ingest URL. */
  URL ingestUrl() {
    return ingestUrl;
  }

  /** Builds the HTTP request for the supplied event. */
  private Request buildRequest(TremorEvent event) throws TremorClientException {
    TremorValidation.requireEvent(event);

    String requestBodyJson = jsonSerializer.serialize(event);
    return new Request.Builder()
        .url(ingestUrl)
        .post(RequestBody.create(requestBodyJson, JSON_MEDIA_TYPE))
        .header(API_KEY_HEADER, tremorKey)
        .build();
  }

  /** Executes the supplied request synchronously. */
  private String execute(Request request) throws TremorClientException {
    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = readResponseBody(response);
      if (!response.isSuccessful()) {
        throw new TremorApiException(response.code(), responseBody, ingestUrl.toString());
      }
      return parseFingerprint(responseBody);
    } catch (IOException e) {
      throw new TremorClientException("Failed to report event to Tremor", e);
    }
  }

  /** Executes the supplied request asynchronously. */
  private CompletableFuture<String> executeAsync(Request request) {
    final CompletableFuture<String> future = new CompletableFuture<>();
    httpClient
        .newCall(request)
        .enqueue(
            new Callback() {
              /** Handles asynchronous transport failures. */
              @Override
              public void onFailure(Call call, IOException e) {
                future.completeExceptionally(
                    new TremorClientException("Failed to report event to Tremor", e));
              }

              /** Handles asynchronous HTTP responses. */
              @Override
              public void onResponse(Call call, Response response) {
                try (Response handledResponse = response) {
                  String responseBody = readResponseBody(handledResponse);
                  if (!handledResponse.isSuccessful()) {
                    future.completeExceptionally(
                        new TremorApiException(
                            handledResponse.code(), responseBody, ingestUrl.toString()));
                    return;
                  }
                  future.complete(parseFingerprint(responseBody));
                } catch (IOException e) {
                  future.completeExceptionally(
                      new TremorClientException("Failed to report event to Tremor", e));
                } catch (TremorClientException e) {
                  future.completeExceptionally(e);
                }
              }
            });
    return future;
  }
}
