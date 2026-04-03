package in.riido.tremor.client.internal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.riido.tremor.client.exception.TremorSerializationException;
import in.riido.tremor.client.model.TremorBreadcrumb;
import in.riido.tremor.client.model.TremorClientInfo;
import in.riido.tremor.client.model.TremorEnvironment;
import in.riido.tremor.client.model.TremorError;
import in.riido.tremor.client.model.TremorEvent;
import in.riido.tremor.client.model.TremorStackFrame;
import in.riido.tremor.client.model.TremorUser;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Serializes Tremor models into the Tremor ingest JSON shape. */
public final class TremorJsonSerializer {

  private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  private final Gson gson;

  /**
   * Creates a new JSON serializer.
   *
   * @param gson the Gson instance to use
   */
  public TremorJsonSerializer(Gson gson) {
    this.gson = gson;
  }

  /**
   * Serializes the supplied Tremor event into the ingest payload.
   *
   * @param event the event to serialize
   * @return the serialized JSON payload
   * @throws TremorSerializationException when serialization fails
   */
  public String serialize(TremorEvent event) throws TremorSerializationException {
    try {
      JsonObject root = new JsonObject();
      if (event.getOccurredOn() != null) {
        root.addProperty("occurredOn", ISO_DATE_TIME.format(event.getOccurredOn()));
      }

      JsonObject details = new JsonObject();
      addProperty(details, "machineName", event.getMachineName());
      addProperty(details, "version", event.getVersion());
      addProperty(details, "groupingKey", event.getGroupingKey());
      details.add("error", toJson(event.getError()));

      if (event.getEnvironment() != null) {
        details.add("environment", toJson(event.getEnvironment()));
      }
      if (event.getClient() != null) {
        details.add("client", toJson(event.getClient()));
      }
      if (!event.getTags().isEmpty()) {
        details.add("tags", toJsonArray(event.getTags()));
      }
      if (!event.getUserCustomData().isEmpty()) {
        details.add("userCustomData", gson.toJsonTree(event.getUserCustomData()));
      }
      if (event.getUser() != null) {
        details.add("user", toJson(event.getUser()));
      }
      if (!event.getBreadcrumbs().isEmpty()) {
        details.add("breadcrumbs", toJson(event.getBreadcrumbs()));
      }

      root.add("details", details);
      return gson.toJson(root);
    } catch (RuntimeException e) {
      throw new TremorSerializationException("Failed to serialize Tremor event", e);
    }
  }

  /** Serializes the supplied value to JSON. */
  private JsonObject toJson(TremorError error) {
    JsonObject object = new JsonObject();
    addProperty(object, "className", error.getClassName());
    addProperty(object, "message", error.getMessage());
    if (!error.getStackTrace().isEmpty()) {
      JsonArray array = new JsonArray();
      for (TremorStackFrame frame : error.getStackTrace()) {
        JsonObject frameObject = new JsonObject();
        addProperty(frameObject, "className", frame.getClassName());
        addProperty(frameObject, "methodName", frame.getMethodName());
        addProperty(frameObject, "fileName", frame.getFileName());
        if (frame.getLineNumber() != null) {
          frameObject.addProperty("lineNumber", frame.getLineNumber());
        }
        array.add(frameObject);
      }
      object.add("stackTrace", array);
    }
    if (error.getInnerError() != null) {
      object.add("innerError", toJson(error.getInnerError()));
    }
    return object;
  }

  /** Serializes the supplied value to JSON. */
  private JsonObject toJson(TremorEnvironment environment) {
    JsonObject object = new JsonObject();
    addProperty(object, "osVersion", environment.getOsVersion());
    addProperty(object, "architecture", environment.getArchitecture());
    if (environment.getProcessorCount() != null) {
      object.addProperty("processorCount", environment.getProcessorCount());
    }
    if (environment.getTotalPhysicalMemory() != null) {
      object.addProperty("totalPhysicalMemory", environment.getTotalPhysicalMemory());
    }
    if (environment.getAvailablePhysicalMemory() != null) {
      object.addProperty("availablePhysicalMemory", environment.getAvailablePhysicalMemory());
    }
    if (environment.getTotalVirtualMemory() != null) {
      object.addProperty("totalVirtualMemory", environment.getTotalVirtualMemory());
    }
    if (environment.getAvailableVirtualMemory() != null) {
      object.addProperty("availableVirtualMemory", environment.getAvailableVirtualMemory());
    }
    addProperty(object, "locale", environment.getLocale());
    if (environment.getUtcOffset() != null) {
      object.addProperty("utcOffset", environment.getUtcOffset());
    }
    return object;
  }

  /** Serializes the supplied value to JSON. */
  private JsonObject toJson(TremorClientInfo clientInfo) {
    JsonObject object = new JsonObject();
    addProperty(object, "name", clientInfo.getName());
    addProperty(object, "version", clientInfo.getVersion());
    addProperty(object, "clientUrl", clientInfo.getClientUrl());
    return object;
  }

  /** Serializes the supplied value to JSON. */
  private JsonObject toJson(TremorUser user) {
    JsonObject object = new JsonObject();
    addProperty(object, "identifier", user.getIdentifier());
    addProperty(object, "email", user.getEmail());
    addProperty(object, "fullName", user.getFullName());
    return object;
  }

  /** Serializes the supplied value to JSON. */
  private JsonArray toJson(List<TremorBreadcrumb> breadcrumbs) {
    JsonArray array = new JsonArray();
    for (TremorBreadcrumb breadcrumb : breadcrumbs) {
      JsonObject object = new JsonObject();
      addProperty(object, "message", breadcrumb.getMessage());
      addProperty(object, "category", breadcrumb.getCategory());
      if (breadcrumb.getLevel() != null) {
        object.addProperty("level", breadcrumb.getLevel());
      }
      addProperty(object, "type", breadcrumb.getType());
      if (breadcrumb.getTimestamp() != null) {
        object.addProperty("timestamp", breadcrumb.getTimestamp());
      }
      addProperty(object, "className", breadcrumb.getClassName());
      addProperty(object, "methodName", breadcrumb.getMethodName());
      if (breadcrumb.getLineNumber() != null) {
        object.addProperty("lineNumber", breadcrumb.getLineNumber());
      }
      if (!breadcrumb.getCustomData().isEmpty()) {
        object.add("customData", gson.toJsonTree(breadcrumb.getCustomData()));
      }
      array.add(object);
    }
    return array;
  }

  /** Serializes the supplied values into a JSON array. */
  private JsonArray toJsonArray(List<String> values) {
    JsonArray array = new JsonArray();
    for (String value : values) {
      array.add(value);
    }
    return array;
  }

  /** Adds the property when the supplied value is not null. */
  private void addProperty(JsonObject object, String name, String value) {
    if (value != null) {
      object.addProperty(name, value);
    }
  }
}
