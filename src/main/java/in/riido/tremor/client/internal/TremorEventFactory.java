package in.riido.tremor.client.internal;

import in.riido.tremor.client.TremorCaptureOptions;
import in.riido.tremor.client.TremorClientDefaults;
import in.riido.tremor.client.model.TremorError;
import in.riido.tremor.client.model.TremorEvent;
import in.riido.tremor.client.model.TremorStackFrame;
import java.util.List;

/** Maps Java throwables into Tremor event models. */
public final class TremorEventFactory {

  private static final TremorClientDefaults EMPTY_DEFAULTS = TremorClientDefaults.builder().build();
  private static final TremorCaptureOptions EMPTY_OPTIONS = TremorCaptureOptions.builder().build();
  private static final int MAX_STACK_FRAMES = 100;
  private static final int MAX_INNER_ERROR_DEPTH = 5;

  private TremorEventFactory() {}

  /**
   * Builds a Tremor event from the supplied throwable.
   *
   * @param throwable the throwable to convert
   * @return the converted Tremor event
   */
  public static TremorEvent fromThrowable(Throwable throwable) {
    return fromThrowable(throwable, EMPTY_DEFAULTS, EMPTY_OPTIONS);
  }

  /**
   * Builds a Tremor event from the supplied throwable and capture options.
   *
   * @param throwable the throwable to convert
   * @param options the request-scoped capture options
   * @return the converted Tremor event
   */
  public static TremorEvent fromThrowable(Throwable throwable, TremorCaptureOptions options) {
    return fromThrowable(throwable, EMPTY_DEFAULTS, options);
  }

  /**
   * Builds a Tremor event from the supplied throwable and client defaults.
   *
   * @param throwable the throwable to convert
   * @param defaults the client defaults to apply
   * @return the converted Tremor event
   */
  public static TremorEvent fromThrowable(Throwable throwable, TremorClientDefaults defaults) {
    return fromThrowable(throwable, defaults, EMPTY_OPTIONS);
  }

  /**
   * Builds a Tremor event from the supplied throwable, client defaults, and capture options.
   *
   * @param throwable the throwable to convert
   * @param defaults the client defaults to apply
   * @param options the request-scoped capture options
   * @return the converted Tremor event
   */
  public static TremorEvent fromThrowable(
      Throwable throwable, TremorClientDefaults defaults, TremorCaptureOptions options) {
    if (throwable == null) {
      throw new IllegalArgumentException("throwable is required");
    }

    TremorClientDefaults clientDefaults = defaults != null ? defaults : EMPTY_DEFAULTS;
    TremorCaptureOptions captureOptions = options != null ? options : EMPTY_OPTIONS;

    TremorEvent.Builder builder =
        TremorEvent.builder()
            .occurredOn(captureOptions.getOccurredOn())
            .machineName(captureOptions.getMachineName())
            .version(resolveValue(captureOptions.getVersion(), clientDefaults.getVersion()))
            .groupingKey(captureOptions.getGroupingKey())
            .error(toError(throwable, 0))
            .environment(captureOptions.getEnvironment())
            .client(resolveValue(captureOptions.getClient(), clientDefaults.getClient()))
            .tags(captureOptions.getTags())
            .userCustomData(captureOptions.getUserCustomData())
            .user(captureOptions.getUser())
            .breadcrumbs(captureOptions.getBreadcrumbs());

    return builder.build();
  }

  /** Returns the primary value when present, otherwise the fallback value. */
  private static <T> T resolveValue(T primary, T fallback) {
    return primary != null ? primary : fallback;
  }

  /** Converts the supplied throwable into a Tremor error model. */
  private static TremorError toError(Throwable throwable, int depth) {
    if (throwable == null) {
      throw new IllegalArgumentException("throwable is required");
    }

    TremorError.Builder builder =
        TremorError.builder()
            .className(throwable.getClass().getName())
            .message(resolveMessage(throwable))
            .stackTrace(toStackFrames(throwable.getStackTrace()));

    if (throwable.getCause() != null && depth < MAX_INNER_ERROR_DEPTH) {
      builder.innerError(toError(throwable.getCause(), depth + 1));
    }

    return builder.build();
  }

  /** Converts the supplied stack trace into Tremor stack frames. */
  private static List<TremorStackFrame> toStackFrames(StackTraceElement[] stackTrace) {
    int frameCount = Math.min(stackTrace.length, MAX_STACK_FRAMES);
    java.util.ArrayList<TremorStackFrame> frames =
        new java.util.ArrayList<TremorStackFrame>(frameCount);
    for (int i = 0; i < frameCount; i++) {
      StackTraceElement element = stackTrace[i];
      frames.add(
          new TremorStackFrame(
              element.getClassName(),
              element.getMethodName(),
              element.getFileName(),
              element.getLineNumber() >= 0 ? Integer.valueOf(element.getLineNumber()) : null));
    }
    return frames;
  }

  /** Resolves the message. */
  private static String resolveMessage(Throwable throwable) {
    String message = throwable.getMessage();
    if (message != null && !message.trim().isEmpty()) {
      return message;
    }
    return throwable.getClass().getSimpleName();
  }
}
