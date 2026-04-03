package in.riido.tremor.client.model;

/** User metadata included with a Tremor event. */
public final class TremorUser {

  private final String identifier;
  private final String email;
  private final String fullName;

  /**
   * Creates a new user metadata value.
   *
   * @param identifier the user identifier
   * @param email the user email
   * @param fullName the user full name
   */
  public TremorUser(String identifier, String email, String fullName) {
    this.identifier = identifier;
    this.email = email;
    this.fullName = fullName;
  }

  /**
   * Returns the user identifier.
   *
   * @return the user identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the user email.
   *
   * @return the user email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Returns the user full name.
   *
   * @return the user full name
   */
  public String getFullName() {
    return fullName;
  }
}
