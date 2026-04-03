package in.riido.tremor.client.internal;

import in.riido.tremor.client.model.TremorBreadcrumb;
import in.riido.tremor.client.model.TremorError;
import in.riido.tremor.client.model.TremorEvent;
import in.riido.tremor.client.model.TremorStackFrame;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/** Validation helpers for Tremor models and configuration. */
public final class TremorValidation {

  private static final int MAX_TEXT_LENGTH = 255;
  private static final int MAX_VERSION_LENGTH = 100;
  private static final int MAX_GROUPING_KEY_LENGTH = 100;
  private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;
  private static final int MAX_STACK_FRAMES = 100;
  private static final int MAX_TAGS = 20;
  private static final int MAX_TAG_LENGTH = 64;
  private static final int MAX_USER_CUSTOM_DATA = 50;
  private static final int MAX_BREADCRUMBS = 100;
  private static final int MAX_BREADCRUMB_CUSTOM_DATA = 20;
  private static final int MAX_INNER_ERROR_DEPTH = 5;
  private static final int MAX_CUSTOM_DATA_DEPTH = 5;
  private static final int MAX_NESTED_COLLECTION_ITEMS = 20;
  private static final int MAX_CUSTOM_DATA_KEY_LENGTH = 100;
  private static final int MAX_CUSTOM_DATA_STRING_LENGTH = 1000;

  private TremorValidation() {}

  /**
   * Validates the supplied event.
   *
   * @param event the event to validate
   */
  public static void validateEvent(TremorEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("event is required");
    }

    validateLength(event.getMachineName(), MAX_TEXT_LENGTH, "machineName");
    validateLength(event.getVersion(), MAX_VERSION_LENGTH, "version");
    validateLength(event.getGroupingKey(), MAX_GROUPING_KEY_LENGTH, "groupingKey");
    validateError(event.getError(), 0, "error");
    validateTags(event.getTags());
    validateUserCustomData(event.getUserCustomData());
    validateBreadcrumbs(event.getBreadcrumbs());
  }

  /**
   * Requires the supplied event to be valid.
   *
   * @param event the event to validate
   */
  public static void requireEvent(TremorEvent event) {
    /** Validates the supplied event. */
    validateEvent(event);
  }

  /**
   * Requires the supplied string to be non-blank.
   *
   * @param value the value to validate
   * @param fieldName the field name used in the error message
   * @return the validated value
   */
  public static String requireNotBlank(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
    return value;
  }

  /**
   * Requires the supplied value to be non-null.
   *
   * @param <T> the value type
   * @param value the value to validate
   * @param fieldName the field name used in the error message
   * @return the validated value
   */
  public static <T> T requireNotNull(T value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
    return value;
  }

  /** Validates the supplied error. */
  private static void validateError(TremorError error, int depth, String path) {
    if (error == null) {
      throw new IllegalArgumentException(path + " is required");
    }
    if (depth > MAX_INNER_ERROR_DEPTH) {
      throw new IllegalArgumentException(path + " exceeds maximum inner error depth of 5");
    }

    requireNotBlank(error.getClassName(), path + ".className");
    requireNotBlank(error.getMessage(), path + ".message");
    validateLength(error.getClassName(), MAX_TEXT_LENGTH, path + ".className");
    validateLength(error.getMessage(), MAX_ERROR_MESSAGE_LENGTH, path + ".message");

    List<TremorStackFrame> stackTrace = error.getStackTrace();
    if (stackTrace.size() > MAX_STACK_FRAMES) {
      throw new IllegalArgumentException(path + ".stackTrace exceeds maximum size of 100");
    }

    for (int i = 0; i < stackTrace.size(); i++) {
      TremorStackFrame frame = stackTrace.get(i);
      if (frame == null) {
        throw new IllegalArgumentException(path + ".stackTrace[" + i + "] is required");
      }
      validateLength(
          frame.getClassName(), MAX_TEXT_LENGTH, path + ".stackTrace[" + i + "].className");
      validateLength(
          frame.getMethodName(), MAX_TEXT_LENGTH, path + ".stackTrace[" + i + "].methodName");
      validateLength(
          frame.getFileName(), MAX_TEXT_LENGTH, path + ".stackTrace[" + i + "].fileName");
    }

    if (error.getInnerError() != null) {
      validateError(error.getInnerError(), depth + 1, path + ".innerError");
    }
  }

  /** Validates the supplied tags. */
  private static void validateTags(List<String> tags) {
    if (tags.size() > MAX_TAGS) {
      throw new IllegalArgumentException("tags exceeds maximum size of 20");
    }
    for (int i = 0; i < tags.size(); i++) {
      validateLength(tags.get(i), MAX_TAG_LENGTH, "tags[" + i + "]");
    }
  }

  /** Validates the supplied user custom data. */
  private static void validateUserCustomData(Map<String, Object> userCustomData) {
    if (userCustomData.size() > MAX_USER_CUSTOM_DATA) {
      throw new IllegalArgumentException("userCustomData exceeds maximum size of 50");
    }
    /** Validates the supplied structured data. */
    validateStructuredData(userCustomData, 1, "userCustomData");
  }

  /** Validates the supplied breadcrumbs. */
  private static void validateBreadcrumbs(List<TremorBreadcrumb> breadcrumbs) {
    if (breadcrumbs.size() > MAX_BREADCRUMBS) {
      throw new IllegalArgumentException("breadcrumbs exceeds maximum size of 100");
    }

    for (int i = 0; i < breadcrumbs.size(); i++) {
      TremorBreadcrumb breadcrumb = breadcrumbs.get(i);
      if (breadcrumb == null) {
        throw new IllegalArgumentException("breadcrumbs[" + i + "] is required");
      }
      String path = "breadcrumbs[" + i + "]";
      validateLength(breadcrumb.getMessage(), MAX_TEXT_LENGTH, path + ".message");
      validateLength(breadcrumb.getCategory(), MAX_TEXT_LENGTH, path + ".category");
      validateLength(breadcrumb.getType(), MAX_TEXT_LENGTH, path + ".type");
      validateLength(breadcrumb.getClassName(), MAX_TEXT_LENGTH, path + ".className");
      validateLength(breadcrumb.getMethodName(), MAX_TEXT_LENGTH, path + ".methodName");
      if (breadcrumb.getCustomData().size() > MAX_BREADCRUMB_CUSTOM_DATA) {
        throw new IllegalArgumentException(path + ".customData exceeds maximum size of 20");
      }
      validateStructuredData(breadcrumb.getCustomData(), 1, path + ".customData");
    }
  }

  /** Validates the supplied structured data. */
  private static void validateStructuredData(Object value, int depth, String path) {
    if (value == null) {
      return;
    }
    if (depth > MAX_CUSTOM_DATA_DEPTH) {
      throw new IllegalArgumentException(path + " exceeds maximum depth of 5");
    }
    if (value instanceof String) {
      validateLength((String) value, MAX_CUSTOM_DATA_STRING_LENGTH, path);
      return;
    }
    if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
      return;
    }
    if (value instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>) value;
      if (depth > 1 && map.size() > MAX_NESTED_COLLECTION_ITEMS) {
        throw new IllegalArgumentException(path + " exceeds maximum size of 20");
      }
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String key = String.valueOf(entry.getKey());
        validateLength(key, MAX_CUSTOM_DATA_KEY_LENGTH, path + " key");
        validateStructuredData(entry.getValue(), depth + 1, path + "." + key);
      }
      return;
    }
    if (value instanceof List<?>) {
      List<?> list = (List<?>) value;
      if (list.size() > MAX_NESTED_COLLECTION_ITEMS) {
        throw new IllegalArgumentException(path + " exceeds maximum size of 20");
      }
      for (int i = 0; i < list.size(); i++) {
        validateStructuredData(list.get(i), depth + 1, path + "[" + i + "]");
      }
      return;
    }
    if (value.getClass().isArray()) {
      int length = Array.getLength(value);
      if (length > MAX_NESTED_COLLECTION_ITEMS) {
        throw new IllegalArgumentException(path + " exceeds maximum size of 20");
      }
      for (int i = 0; i < length; i++) {
        validateStructuredData(Array.get(value, i), depth + 1, path + "[" + i + "]");
      }
      return;
    }

    validateLength(String.valueOf(value), MAX_CUSTOM_DATA_STRING_LENGTH, path);
  }

  /** Validates the supplied length. */
  private static void validateLength(String value, int maxLength, String fieldName) {
    if (value != null && value.length() > maxLength) {
      throw new IllegalArgumentException(fieldName + " exceeds maximum length of " + maxLength);
    }
  }
}
