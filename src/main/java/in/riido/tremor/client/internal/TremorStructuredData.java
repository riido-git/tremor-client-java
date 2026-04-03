package in.riido.tremor.client.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Utilities for copying structured custom data safely. */
public final class TremorStructuredData {

  private TremorStructuredData() {}

  /**
   * Creates a structured-data-safe copy of the supplied map.
   *
   * @param source the source map
   * @return a copied map safe for Tremor model storage
   */
  public static Map<String, Object> copyMap(Map<String, ?> source) {
    if (source == null || source.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, Object> copy = new LinkedHashMap<String, Object>();
    for (Map.Entry<String, ?> entry : source.entrySet()) {
      copy.put(entry.getKey(), copyValue(entry.getValue()));
    }
    return copy;
  }

  /** Creates a structured-data-safe copy of the supplied list. */
  private static List<Object> copyList(List<?> source) {
    List<Object> copy = new ArrayList<Object>(source.size());
    for (Object value : source) {
      copy.add(copyValue(value));
    }
    return Collections.unmodifiableList(copy);
  }

  /** Creates a structured-data-safe copy of the supplied value. */
  private static Object copyValue(Object value) {
    if (value instanceof Map<?, ?>) {
      @SuppressWarnings("unchecked")
      Map<String, ?> source = (Map<String, ?>) value;
      return Collections.unmodifiableMap(copyMap(source));
    }
    if (value instanceof List<?>) {
      return copyList((List<?>) value);
    }
    return value;
  }
}
