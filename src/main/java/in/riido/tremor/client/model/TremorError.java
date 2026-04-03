package in.riido.tremor.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Error metadata included with a Tremor event. */
public final class TremorError {

  private final String className;
  private final String message;
  private final List<TremorStackFrame> stackTrace;
  private final TremorError innerError;

  /** Creates a new TremorError from the supplied builder state. */
  private TremorError(Builder builder) {
    this.className = builder.className;
    this.message = builder.message;
    this.stackTrace = Collections.unmodifiableList(new ArrayList<>(builder.stackTrace));
    this.innerError = builder.innerError;
  }

  /**
   * Creates a new error builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the exception class name.
   *
   * @return the exception class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Returns the exception message.
   *
   * @return the exception message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the stack frames.
   *
   * @return an immutable list of stack frames
   */
  public List<TremorStackFrame> getStackTrace() {
    return stackTrace;
  }

  /**
   * Returns the inner error.
   *
   * @return the inner error, or {@code null} when absent
   */
  public TremorError getInnerError() {
    return innerError;
  }

  /** Builder for {@link TremorError}. */
  public static final class Builder {

    private final List<TremorStackFrame> stackTrace = new ArrayList<>();
    private String className;
    private String message;
    private TremorError innerError;

    /** Creates a new builder instance. */
    private Builder() {}

    /**
     * Sets the exception class name.
     *
     * @param className the exception class name
     * @return this builder
     */
    public Builder className(String className) {
      this.className = className;
      return this;
    }

    /**
     * Sets the exception message.
     *
     * @param message the exception message
     * @return this builder
     */
    public Builder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Replaces the stack frames on this builder.
     *
     * @param stackTrace the stack frames to use
     * @return this builder
     */
    public Builder stackTrace(List<TremorStackFrame> stackTrace) {
      this.stackTrace.clear();
      if (stackTrace != null) {
        this.stackTrace.addAll(stackTrace);
      }
      return this;
    }

    /**
     * Adds one stack frame to this builder.
     *
     * @param frame the stack frame to add
     * @return this builder
     */
    public Builder frame(TremorStackFrame frame) {
      if (frame != null) {
        this.stackTrace.add(frame);
      }
      return this;
    }

    /**
     * Sets the inner error.
     *
     * @param innerError the inner error to use
     * @return this builder
     */
    public Builder innerError(TremorError innerError) {
      this.innerError = innerError;
      return this;
    }

    /**
     * Builds the configured error metadata.
     *
     * @return the configured error metadata
     */
    public TremorError build() {
      return new TremorError(this);
    }
  }
}
