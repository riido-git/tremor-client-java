package in.riido.tremor.client.exception;

/** Exception raised when Tremor returns a non-success HTTP response. */
public final class TremorApiException extends TremorClientException {

  /** HTTP status code returned by Tremor. */
  private final int statusCode;
  /** Response body returned by Tremor. */
  private final String responseBody;
  /** Request URL used for the Tremor call. */
  private final String url;

  /**
   * Creates a new API exception.
   *
   * @param statusCode the HTTP status code returned by Tremor
   * @param responseBody the response body returned by Tremor
   * @param url the request URL
   */
  public TremorApiException(int statusCode, String responseBody, String url) {
    super("Tremor API request failed with status " + statusCode + " for " + url);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
    this.url = url;
  }

  /**
   * Returns the HTTP status code.
   *
   * @return the HTTP status code
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Returns the Tremor response body.
   *
   * @return the response body, or {@code null} when none was returned
   */
  public String getResponseBody() {
    return responseBody;
  }

  /**
   * Returns the request URL.
   *
   * @return the request URL
   */
  public String getUrl() {
    return url;
  }
}
