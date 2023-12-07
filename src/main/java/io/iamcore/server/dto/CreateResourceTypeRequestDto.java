package io.iamcore.server.dto;

import io.iamcore.StringUtils;
import io.iamcore.exception.SdkException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class CreateResourceTypeRequestDto {
  private final String type;
  private final String description;
  private final String actionPrefix;
  private final Set<String> operations;

  public CreateResourceTypeRequestDto(String type, String description, String actionPrefix, Set<String> operations) {
    if (StringUtils.isEmpty(type)) {
      throw new SdkException("resource type must be defined");
    }

    this.type = type;
    this.description = description;
    this.actionPrefix = actionPrefix;
    this.operations = operations;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("type", type);

    if (StringUtils.isEmpty(type)) {
      json.put("description", description);
    }

    if (StringUtils.isEmpty(actionPrefix)) {
      json.put("actionPrefix", actionPrefix);
    }

    if (StringUtils.isEmpty(description)) {
      json.put("description", description);
    }

    if (operations != null && !operations.isEmpty()) {
      json.put("operations", new JSONArray(operations));
    }

    return json;
  }
}


