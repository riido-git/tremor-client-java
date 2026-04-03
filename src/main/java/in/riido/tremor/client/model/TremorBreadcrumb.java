package in.riido.tremor.client.model;

import in.riido.tremor.client.internal.TremorStructuredData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Breadcrumb metadata included with a Tremor event. */
public final class TremorBreadcrumb {

  private final String message;
  private final String category;
  private final Integer level;
  private final String type;
  private final Long timestamp;
  private final String className;
  private final String methodName;
  private final Integer lineNumber;
  private final Map<String, Object> customData;

  /** Creates a new TremorBreadcrumb from the supplied builder state. */
  private TremorBreadcrumb(Builder builder) {
    this.message = builder.message;
    this.category = builder.category;
    this.level = builder.level;
    this.type = builder.type;
    this.timestamp = builder.timestamp;
    this.className = builder.className;
    this.methodName = builder.methodName;
    this.lineNumber = builder.lineNumber;
    this.customData = Collections.unmodifiableMap(TremorStructuredData.copyMap(builder.customData));
  }

  /**
   * Creates a new breadcrumb builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the breadcrumb message.
   *
   * @return the breadcrumb message, or {@code null} when unset
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the breadcrumb category.
   *
   * @return the breadcrumb category, or {@code null} when unset
   */
  public String getCategory() {
    return category;
  }

  /**
   * Returns the breadcrumb level.
   *
   * @return the breadcrumb level, or {@code null} when unset
   */
  public Integer getLevel() {
    return level;
  }

  /**
   * Returns the breadcrumb type.
   *
   * @return the breadcrumb type, or {@code null} when unset
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the breadcrumb timestamp.
   *
   * @return the breadcrumb timestamp, or {@code null} when unset
   */
  public Long getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the breadcrumb class name.
   *
   * @return the breadcrumb class name, or {@code null} when unset
   */
  public String getClassName() {
    return className;
  }

  /**
   * Returns the breadcrumb method name.
   *
   * @return the breadcrumb method name, or {@code null} when unset
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Returns the breadcrumb line number.
   *
   * @return the breadcrumb line number, or {@code null} when unset
   */
  public Integer getLineNumber() {
    return lineNumber;
  }

  /**
   * Returns the breadcrumb custom data.
   *
   * @return an immutable map of custom data
   */
  public Map<String, Object> getCustomData() {
    return customData;
  }

  /** Builder for {@link TremorBreadcrumb}. */
  public static final class Builder {

    private String message;
    private String category;
    private Integer level;
    private String type;
    private Long timestamp;
    private String className;
    private String methodName;
    private Integer lineNumber;
    private final Map<String, Object> customData = new LinkedHashMap<String, Object>();

    /** Creates a new builder instance. */
    private Builder() {}

    /**
     * Sets the breadcrumb message.
     *
     * @param message the breadcrumb message
     * @return this builder
     */
    public Builder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets the breadcrumb category.
     *
     * @param category the breadcrumb category
     * @return this builder
     */
    public Builder category(String category) {
      this.category = category;
      return this;
    }

    /**
     * Sets the breadcrumb level.
     *
     * @param level the breadcrumb level
     * @return this builder
     */
    public Builder level(Integer level) {
      this.level = level;
      return this;
    }

    /**
     * Sets the breadcrumb type.
     *
     * @param type the breadcrumb type
     * @return this builder
     */
    public Builder type(String type) {
      this.type = type;
      return this;
    }

    /**
     * Sets the breadcrumb timestamp.
     *
     * @param timestamp the breadcrumb timestamp
     * @return this builder
     */
    public Builder timestamp(Long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * Sets the breadcrumb class name.
     *
     * @param className the breadcrumb class name
     * @return this builder
     */
    public Builder className(String className) {
      this.className = className;
      return this;
    }

    /**
     * Sets the breadcrumb method name.
     *
     * @param methodName the breadcrumb method name
     * @return this builder
     */
    public Builder methodName(String methodName) {
      this.methodName = methodName;
      return this;
    }

    /**
     * Sets the breadcrumb line number.
     *
     * @param lineNumber the breadcrumb line number
     * @return this builder
     */
    public Builder lineNumber(Integer lineNumber) {
      this.lineNumber = lineNumber;
      return this;
    }

    /**
     * Replaces the breadcrumb custom data on this builder.
     *
     * @param customData the custom data to use
     * @return this builder
     */
    public Builder customData(Map<String, ?> customData) {
      this.customData.clear();
      if (customData != null) {
        this.customData.putAll(customData);
      }
      return this;
    }

    /**
     * Adds one breadcrumb custom data entry.
     *
     * @param key the custom data key
     * @param value the custom data value
     * @return this builder
     */
    public Builder putCustomData(String key, Object value) {
      this.customData.put(key, value);
      return this;
    }

    /**
     * Builds the configured breadcrumb metadata.
     *
     * @return the configured breadcrumb metadata
     */
    public TremorBreadcrumb build() {
      return new TremorBreadcrumb(this);
    }
  }
}
