package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import org.json.JSONObject;

public class UpdateResourceRequestDto {

  private final Set<String> poolIds;

  public UpdateResourceRequestDto(Set<String> poolIds) {
    this.poolIds = poolIds;
  }

  @JsonProperty("poolIDs")
  public Set<String> getPoolIds() {
    return poolIds;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    if (this.poolIds != null) {
      json.put("poolIDs", poolIds);
    }

    return json;
  }
}

