package in.riido.tremor.client;

import in.riido.tremor.client.internal.TremorStructuredData;
import in.riido.tremor.client.model.TremorBreadcrumb;
import in.riido.tremor.client.model.TremorClientInfo;
import in.riido.tremor.client.model.TremorEnvironment;
import in.riido.tremor.client.model.TremorUser;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Request-scoped metadata used when reporting a throwable to Tremor. */
public final class TremorCaptureOptions {

  private final OffsetDateTime occurredOn;
  private final String machineName;
  private final String version;
  private final String groupingKey;
  private final TremorEnvironment environment;
  private final TremorClientInfo client;
  private final List<String> tags;
  private final Map<String, Object> userCustomData;
  private final TremorUser user;
  private final List<TremorBreadcrumb> breadcrumbs;

  /** Creates a new TremorCaptureOptions from the supplied builder state. */
  private TremorCaptureOptions(Builder builder) {
    this.occurredOn = builder.occurredOn;
    this.machineName = builder.machineName;
    this.version = builder.version;
    this.groupingKey = builder.groupingKey;
    this.environment = builder.environment;
    this.client = builder.client;
    this.tags = Collections.unmodifiableList(new ArrayList<>(builder.tags));
    this.userCustomData =
        Collections.unmodifiableMap(TremorStructuredData.copyMap(builder.userCustomData));
    this.user = builder.user;
    this.breadcrumbs = Collections.unmodifiableList(new ArrayList<>(builder.breadcrumbs));
  }

  /**
   * Creates a new capture options builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the event timestamp override.
   *
   * @return the event timestamp override, or {@code null} when unset
   */
  public OffsetDateTime getOccurredOn() {
    return occurredOn;
  }

  /**
   * Returns the machine name override.
   *
   * @return the machine name override, or {@code null} when unset
   */
  public String getMachineName() {
    return machineName;
  }

  /**
   * Returns the version override.
   *
   * @return the version override, or {@code null} when unset
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the grouping key override.
   *
   * @return the grouping key override, or {@code null} when unset
   */
  public String getGroupingKey() {
    return groupingKey;
  }

  /**
   * Returns the environment override.
   *
   * @return the environment override, or {@code null} when unset
   */
  public TremorEnvironment getEnvironment() {
    return environment;
  }

  /**
   * Returns the client metadata override.
   *
   * @return the client metadata override, or {@code null} when unset
   */
  public TremorClientInfo getClient() {
    return client;
  }

  /**
   * Returns the tags to include with the event.
   *
   * @return an immutable list of tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * Returns the structured custom data to include with the event.
   *
   * @return an immutable map of custom data
   */
  public Map<String, Object> getUserCustomData() {
    return userCustomData;
  }

  /**
   * Returns the user override.
   *
   * @return the user override, or {@code null} when unset
   */
  public TremorUser getUser() {
    return user;
  }

  /**
   * Returns the breadcrumbs to include with the event.
   *
   * @return an immutable list of breadcrumbs
   */
  public List<TremorBreadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  /** Builder for {@link TremorCaptureOptions}. */
  public static final class Builder {

    private final List<String> tags = new ArrayList<String>();
    private final Map<String, Object> userCustomData = new LinkedHashMap<String, Object>();
    private final List<TremorBreadcrumb> breadcrumbs = new ArrayList<TremorBreadcrumb>();
    private OffsetDateTime occurredOn;
    private String machineName;
    private String version;
    private String groupingKey;
    private TremorEnvironment environment;
    private TremorClientInfo client;
    private TremorUser user;

    /** Creates a new builder instance. */
    private Builder() {}

    /**
     * Sets the event timestamp override.
     *
     * @param occurredOn the event timestamp to use
     * @return this builder
     */
    public Builder occurredOn(OffsetDateTime occurredOn) {
      this.occurredOn = occurredOn;
      return this;
    }

    /**
     * Sets the machine name override.
     *
     * @param machineName the machine name to use
     * @return this builder
     */
    public Builder machineName(String machineName) {
      this.machineName = machineName;
      return this;
    }

    /**
     * Sets the version override.
     *
     * @param version the version to use
     * @return this builder
     */
    public Builder version(String version) {
      this.version = version;
      return this;
    }

    /**
     * Sets the grouping key override.
     *
     * @param groupingKey the grouping key to use
     * @return this builder
     */
    public Builder groupingKey(String groupingKey) {
      this.groupingKey = groupingKey;
      return this;
    }

    /**
     * Sets the environment override.
     *
     * @param environment the environment metadata to use
     * @return this builder
     */
    public Builder environment(TremorEnvironment environment) {
      this.environment = environment;
      return this;
    }

    /**
     * Sets the client metadata override.
     *
     * @param client the client metadata to use
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
     * Adds a tag to this builder.
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
     * Sets the user override.
     *
     * @param user the user metadata to use
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
     * Adds a breadcrumb to this builder.
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
     * Builds the configured capture options.
     *
     * @return the configured capture options
     */
    public TremorCaptureOptions build() {
      return new TremorCaptureOptions(this);
    }
  }
}
