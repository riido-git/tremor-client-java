package in.riido.tremor.client.exception;

/** Exception raised when Tremor request or response JSON is invalid. */
public final class TremorSerializationException extends TremorClientException {

  /**
   * Creates a new serialization exception.
   *
   * @param message the exception message
   */
  public TremorSerializationException(String message) {
    super(message);
  }

  /**
   * Creates a new serialization exception.
   *
   * @param message the exception message
   * @param cause the underlying cause
   */
  public TremorSerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
