package in.riido.tremor.client.model;

/** Client metadata included with a Tremor event. */
public final class TremorClientInfo {

  private final String name;
  private final String version;
  private final String clientUrl;

  /**
   * Creates a new client metadata value.
   *
   * @param name the client name
   * @param version the client version
   * @param clientUrl the client URL
   */
  public TremorClientInfo(String name, String version, String clientUrl) {
    this.name = name;
    this.version = version;
    this.clientUrl = clientUrl;
  }

  /**
   * Returns the client name.
   *
   * @return the client name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the client version.
   *
   * @return the client version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the client URL.
   *
   * @return the client URL
   */
  public String getClientUrl() {
    return clientUrl;
  }
}
