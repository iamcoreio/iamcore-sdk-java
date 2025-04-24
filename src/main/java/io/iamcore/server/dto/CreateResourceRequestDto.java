package io.iamcore.server.dto;

import io.iamcore.StringUtils;
import java.util.Set;
import org.json.JSONObject;

public class CreateResourceRequestDto {
  private final String application;
  private final String tenantId;
  private final String resourceType;
  private final String path;
  private final String name;
  private final Boolean enabled;
  private final Set<String> poolIds;

  public CreateResourceRequestDto(
      String application,
      String tenantId,
      String resourceType,
      String path,
      String name,
      Boolean enabled,
      Set<String> poolIds) {
    this.application = application;
    this.tenantId = tenantId;
    this.resourceType = resourceType;
    this.path = StringUtils.isEmpty(path) ? "/" : path;
    this.name = name;
    this.enabled = enabled;
    this.poolIds = poolIds;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("name", name);
    json.put("path", path);
    json.put("resourceType", resourceType);
    json.put("enabled", enabled);
    json.put("tenantID", tenantId);
    json.put("application", application);

    if (poolIds != null && !poolIds.isEmpty()) {
      json.put("poolIDs", poolIds);
    }

    return json;
  }
}


