package io.iamcore.server.dto;

import java.util.Set;
import org.json.JSONObject;

public class UpdateResourceRequestDto {

  private final Set<String> poolIds;

  public UpdateResourceRequestDto(Set<String> poolIDs) {
    this.poolIds = poolIDs;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    if (this.poolIds != null) {
      json.put("poolIDs", poolIds);
    }

    return json;
  }
}
