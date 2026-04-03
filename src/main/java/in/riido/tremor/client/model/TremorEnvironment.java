package in.riido.tremor.client.model;

/** Environment metadata included with a Tremor event. */
public final class TremorEnvironment {

  private final String osVersion;
  private final String architecture;
  private final Integer processorCount;
  private final Long totalPhysicalMemory;
  private final Long availablePhysicalMemory;
  private final Long totalVirtualMemory;
  private final Long availableVirtualMemory;
  private final String locale;
  private final Double utcOffset;

  /** Creates a new TremorEnvironment from the supplied builder state. */
  private TremorEnvironment(Builder builder) {
    this.osVersion = builder.osVersion;
    this.architecture = builder.architecture;
    this.processorCount = builder.processorCount;
    this.totalPhysicalMemory = builder.totalPhysicalMemory;
    this.availablePhysicalMemory = builder.availablePhysicalMemory;
    this.totalVirtualMemory = builder.totalVirtualMemory;
    this.availableVirtualMemory = builder.availableVirtualMemory;
    this.locale = builder.locale;
    this.utcOffset = builder.utcOffset;
  }

  /**
   * Creates a new environment builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the operating system version.
   *
   * @return the operating system version, or {@code null} when unset
   */
  public String getOsVersion() {
    return osVersion;
  }

  /**
   * Returns the machine architecture.
   *
   * @return the machine architecture, or {@code null} when unset
   */
  public String getArchitecture() {
    return architecture;
  }

  /**
   * Returns the processor count.
   *
   * @return the processor count, or {@code null} when unset
   */
  public Integer getProcessorCount() {
    return processorCount;
  }

  /**
   * Returns the total physical memory.
   *
   * @return the total physical memory, or {@code null} when unset
   */
  public Long getTotalPhysicalMemory() {
    return totalPhysicalMemory;
  }

  /**
   * Returns the available physical memory.
   *
   * @return the available physical memory, or {@code null} when unset
   */
  public Long getAvailablePhysicalMemory() {
    return availablePhysicalMemory;
  }

  /**
   * Returns the total virtual memory.
   *
   * @return the total virtual memory, or {@code null} when unset
   */
  public Long getTotalVirtualMemory() {
    return totalVirtualMemory;
  }

  /**
   * Returns the available virtual memory.
   *
   * @return the available virtual memory, or {@code null} when unset
   */
  public Long getAvailableVirtualMemory() {
    return availableVirtualMemory;
  }

  /**
   * Returns the locale.
   *
   * @return the locale, or {@code null} when unset
   */
  public String getLocale() {
    return locale;
  }

  /**
   * Returns the UTC offset.
   *
   * @return the UTC offset, or {@code null} when unset
   */
  public Double getUtcOffset() {
    return utcOffset;
  }

  /** Builder for {@link TremorEnvironment}. */
  public static final class Builder {

    private String osVersion;
    private String architecture;
    private Integer processorCount;
    private Long totalPhysicalMemory;
    private Long availablePhysicalMemory;
    private Long totalVirtualMemory;
    private Long availableVirtualMemory;
    private String locale;
    private Double utcOffset;

    /** Creates a new builder instance. */
    private Builder() {}

    /**
     * Sets the operating system version.
     *
     * @param osVersion the operating system version
     * @return this builder
     */
    public Builder osVersion(String osVersion) {
      this.osVersion = osVersion;
      return this;
    }

    /**
     * Sets the machine architecture.
     *
     * @param architecture the machine architecture
     * @return this builder
     */
    public Builder architecture(String architecture) {
      this.architecture = architecture;
      return this;
    }

    /**
     * Sets the processor count.
     *
     * @param processorCount the processor count
     * @return this builder
     */
    public Builder processorCount(Integer processorCount) {
      this.processorCount = processorCount;
      return this;
    }

    /**
     * Sets the total physical memory.
     *
     * @param totalPhysicalMemory the total physical memory
     * @return this builder
     */
    public Builder totalPhysicalMemory(Long totalPhysicalMemory) {
      this.totalPhysicalMemory = totalPhysicalMemory;
      return this;
    }

    /**
     * Sets the available physical memory.
     *
     * @param availablePhysicalMemory the available physical memory
     * @return this builder
     */
    public Builder availablePhysicalMemory(Long availablePhysicalMemory) {
      this.availablePhysicalMemory = availablePhysicalMemory;
      return this;
    }

    /**
     * Sets the total virtual memory.
     *
     * @param totalVirtualMemory the total virtual memory
     * @return this builder
     */
    public Builder totalVirtualMemory(Long totalVirtualMemory) {
      this.totalVirtualMemory = totalVirtualMemory;
      return this;
    }

    /**
     * Sets the available virtual memory.
     *
     * @param availableVirtualMemory the available virtual memory
     * @return this builder
     */
    public Builder availableVirtualMemory(Long availableVirtualMemory) {
      this.availableVirtualMemory = availableVirtualMemory;
      return this;
    }

    /**
     * Sets the locale.
     *
     * @param locale the locale
     * @return this builder
     */
    public Builder locale(String locale) {
      this.locale = locale;
      return this;
    }

    /**
     * Sets the UTC offset.
     *
     * @param utcOffset the UTC offset
     * @return this builder
     */
    public Builder utcOffset(Double utcOffset) {
      this.utcOffset = utcOffset;
      return this;
    }

    /**
     * Builds the configured environment metadata.
     *
     * @return the configured environment metadata
     */
    public TremorEnvironment build() {
      return new TremorEnvironment(this);
    }
  }
}
