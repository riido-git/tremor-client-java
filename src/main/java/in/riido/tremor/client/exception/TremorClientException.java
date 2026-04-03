package in.riido.tremor.client.exception;

/** Base exception type for Tremor client failures. */
public class TremorClientException extends Exception {

  /**
   * Creates a new client exception.
   *
   * @param message the exception message
   */
  public TremorClientException(String message) {
    super(message);
  }

  /**
   * Creates a new client exception.
   *
   * @param message the exception message
   * @param cause the underlying cause
   */
  public TremorClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
