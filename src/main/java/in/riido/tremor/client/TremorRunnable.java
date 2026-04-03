package in.riido.tremor.client;

/** Runnable callback that is allowed to throw checked exceptions. */
@FunctionalInterface
public interface TremorRunnable {

  /**
   * Runs the wrapped operation.
   *
   * @throws Exception when the wrapped operation fails
   */
  void run() throws Exception;
}
