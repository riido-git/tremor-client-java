package in.riido.tremor.client.model;

import in.riido.tremor.client.internal.TremorStructuredData;
import in.riido.tremor.client.internal.TremorValidation;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Top-level event payload reported to Tremor. */
public final class TremorEvent {

  private final OffsetDateTime occurredOn;
  private final String machineName;
  private final String version;
  private final String groupingKey;
  private final TremorError error;
  private final TremorEnvironment environment;
  private final TremorClientInfo client;
  private final List<String> tags;
  private final Map<String, Object> userCustomData;
  private final TremorUser user;
  private final List<TremorBreadcrumb> breadcrumbs;

  /** Creates a new TremorEvent from the supplied builder state. */
  private TremorEvent(Builder builder) {
    this.occurredOn = builder.occurredOn;
    this.machineName = builder.machineName;
    this.version = builder.version;
    this.groupingKey = builder.groupingKey;
    this.error = builder.error;
    this.environment = builder.environment;
    this.client = builder.client;
    this.tags = Collections.unmodifiableList(new ArrayList<String>(builder.tags));
    this.userCustomData =
        Collections.unmodifiableMap(TremorStructuredData.copyMap(builder.userCustomData));
    this.user = builder.user;
    this.breadcrumbs =
        Collections.unmodifiableList(new ArrayList<TremorBreadcrumb>(builder.breadcrumbs));
  }

  /**
   * Creates a new event builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the event timestamp.
   *
   * @return the event timestamp, or {@code null} when unset
   */
  public OffsetDateTime getOccurredOn() {
    return occurredOn;
  }

  /**
   * Returns the machine name.
   *
   * @return the machine name, or {@code null} when unset
   */
  public String getMachineName() {
    return machineName;
  }

  /**
   * Returns the version.
   *
   * @return the version, or {@code null} when unset
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the grouping key.
   *
   * @return the grouping key, or {@code null} when unset
   */
  public String getGroupingKey() {
    return groupingKey;
  }

  /**
   * Returns the error metadata.
   *
   * @return the error metadata
   */
  public TremorError getError() {
    return error;
  }

  /**
   * Returns the environment metadata.
   *
   * @return the environment metadata, or {@code null} when unset
   */
  public TremorEnvironment getEnvironment() {
    return environment;
  }

  /**
   * Returns the client metadata.
   *
   * @return the client metadata, or {@code null} when unset
   */
  public TremorClientInfo getClient() {
    return client;
  }

  /**
   * Returns the event tags.
   *
   * @return an immutable list of tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * Returns the structured custom data.
   *
   * @return an immutable map of custom data
   */
  public Map<String, Object> getUserCustomData() {
    return userCustomData;
  }

  /**
   * Returns the user metadata.
   *
   * @return the user metadata, or {@code null} when unset
   */
  public TremorUser getUser() {
    return user;
  }

  /**
   * Returns the event breadcrumbs.
   *
   * @return an immutable list of breadcrumbs
   */
  public List<TremorBreadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  /** Builder for {@link TremorEvent}. */
  public static final class Builder {

    private OffsetDateTime occurredOn;
    private String machineName;
    private String version;
    private String groupingKey;
    private TremorError error;
    private TremorEnvironment environment;
    private TremorClientInfo client;
    private final List<String> tags = new ArrayList<String>();
    private final Map<String, Object> userCustomData = new LinkedHashMap<String, Object>();
    private TremorUser user;
    private final List<TremorBreadcrumb> breadcrumbs = new ArrayList<TremorBreadcrumb>();

    /** Creates a new builder instance. */
    private Builder() {}

    /**
     * Sets the event timestamp.
     *
     * @param occurredOn the event timestamp
     * @return this builder
     */
    public Builder occurredOn(OffsetDateTime occurredOn) {
      this.occurredOn = occurredOn;
      return this;
    }

    /**
     * Sets the machine name.
     *
     * @param machineName the machine name
     * @return this builder
     */
    public Builder machineName(String machineName) {
      this.machineName = machineName;
      return this;
    }

    /**
     * Sets the version.
     *
     * @param version the version
     * @return this builder
     */
    public Builder version(String version) {
      this.version = version;
      return this;
    }

    /**
     * Sets the grouping key.
     *
     * @param groupingKey the grouping key
     * @return this builder
     */
    public Builder groupingKey(String groupingKey) {
      this.groupingKey = groupingKey;
      return this;
    }

    /**
     * Sets the error metadata.
     *
     * @param error the error metadata
     * @return this builder
     */
    public Builder error(TremorError error) {
      this.error = error;
      return this;
    }

    /**
     * Sets the environment metadata.
     *
     * @param environment the environment metadata
     * @return this builder
     */
    public Builder environment(TremorEnvironment environment) {
      this.environment = environment;
      return this;
    }

    /**
     * Sets the client metadata.
     *
     * @param client the client metadata
     * @return this builder
     */
    public Builder client(TremorClientInfo client) {
      this.client = client;
      return this;
    }

    /**
     * Replaces the tags on this builder.
     *
     * @param tags the tags to use
     * @return this builder
     */
    public Builder tags(List<String> tags) {
      this.tags.clear();
      if (tags != null) {
        this.tags.addAll(tags);
      }
      return this;
    }

    /**
     * Adds one tag to this builder.
     *
     * @param tag the tag to add
     * @return this builder
     */
    public Builder tag(String tag) {
      if (tag != null) {
        this.tags.add(tag);
      }
      return this;
    }

    /**
     * Replaces the custom data on this builder.
     *
     * @param userCustomData the custom data to use
     * @return this builder
     */
    public Builder userCustomData(Map<String, ?> userCustomData) {
      this.userCustomData.clear();
      if (userCustomData != null) {
        this.userCustomData.putAll(userCustomData);
      }
      return this;
    }

    /**
     * Adds one custom data entry to this builder.
     *
     * @param key the custom data key
     * @param value the custom data value
     * @return this builder
     */
    public Builder putUserCustomData(String key, Object value) {
      this.userCustomData.put(key, value);
      return this;
    }

    /**
     * Sets the user metadata.
     *
     * @param user the user metadata
     * @return this builder
     */
    public Builder user(TremorUser user) {
      this.user = user;
      return this;
    }

    /**
     * Replaces the breadcrumbs on this builder.
     *
     * @param breadcrumbs the breadcrumbs to use
     * @return this builder
     */
    public Builder breadcrumbs(List<TremorBreadcrumb> breadcrumbs) {
      this.breadcrumbs.clear();
      if (breadcrumbs != null) {
        this.breadcrumbs.addAll(breadcrumbs);
      }
      return this;
    }

    /**
     * Adds one breadcrumb to this builder.
     *
     * @param breadcrumb the breadcrumb to add
     * @return this builder
     */
    public Builder breadcrumb(TremorBreadcrumb breadcrumb) {
      if (breadcrumb != null) {
        this.breadcrumbs.add(breadcrumb);
      }
      return this;
    }

    /**
     * Builds and validates the configured event.
     *
     * @return the configured event
     * @throws IllegalArgumentException when the event payload is invalid
     */
    public TremorEvent build() {
      TremorEvent event = new TremorEvent(this);
      TremorValidation.validateEvent(event);
      return event;
    }
  }
}
