package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.iamcore.IRN;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class DeleteResourcesRequestDto {
  private final List<IRN> irns;

  public DeleteResourcesRequestDto(List<IRN> irns) {
    this.irns = irns;
  }

  @JsonProperty("resourceIDs")
  public List<String> getResourceIds() {
    return this.irns.stream()
        .map(IRN::toBase64)
        .collect(Collectors.toList());
  }

  public JSONObject toJson() {
    List<String> resourceIds = this.irns.stream()
        .map(IRN::toBase64)
        .collect(Collectors.toList());

    JSONObject json = new JSONObject();
    json.put("resourceIDs", resourceIds);

    return json;
  }
}
