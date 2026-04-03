package in.riido.tremor.client;

import in.riido.tremor.client.model.TremorClientInfo;

/** Immutable client-level defaults applied during throwable capture. */
public final class TremorClientDefaults {

  private final String version;
  private final TremorClientInfo client;

  /** Creates a new TremorClientDefaults from the supplied builder state. */
  private TremorClientDefaults(Builder builder) {
    this.version = builder.version;
    this.client = builder.client;
  }

  /**
   * Creates a new client defaults builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the default version.
   *
   * @return the default version, or {@code null} when unset
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the default client metadata.
   *
   * @return the default client metadata, or {@code null} when unset
   */
  public TremorClientInfo getClient() {
    return client;
  }

  /** Builder for {@link TremorClientDefaults}. */
  public static final class Builder {

    private String version;
    private TremorClientInfo client;

    /** Creates a new builder instance. */
    private Builder() {}

    /**
     * Sets the default version.
     *
     * @param version the default version to use
     * @return this builder
     */
    public Builder version(String version) {
      this.version = version;
      return this;
    }

    /**
     * Sets the default client metadata.
     *
     * @param client the default client metadata to use
     * @return this builder
     */
    public Builder client(TremorClientInfo client) {
      this.client = client;
      return this;
    }

    /**
     * Builds the configured client defaults.
     *
     * @return the configured client defaults
     */
    public TremorClientDefaults build() {
      return new TremorClientDefaults(this);
    }
  }
}
