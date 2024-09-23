package io.iamcore.server.dto;

import io.iamcore.IRN;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class DeleteResourcesRequestDto {
  private final List<IRN> irns;

  public DeleteResourcesRequestDto(List<IRN> irns) {
    this.irns = irns;
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
