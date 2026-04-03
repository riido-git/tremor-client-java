package in.riido.tremor.client.model;

/** One stack frame included with a Tremor error. */
public final class TremorStackFrame {

  private final String className;
  private final String methodName;
  private final String fileName;
  private final Integer lineNumber;

  /**
   * Creates a new stack frame value.
   *
   * @param className the declaring class name
   * @param methodName the method name
   * @param fileName the source file name
   * @param lineNumber the source line number
   */
  public TremorStackFrame(
      String className, String methodName, String fileName, Integer lineNumber) {
    this.className = className;
    this.methodName = methodName;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  /**
   * Returns the declaring class name.
   *
   * @return the declaring class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Returns the method name.
   *
   * @return the method name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Returns the source file name.
   *
   * @return the source file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns the source line number.
   *
   * @return the source line number, or {@code null} when unavailable
   */
  public Integer getLineNumber() {
    return lineNumber;
  }
}
