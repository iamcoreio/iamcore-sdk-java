package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iamcore.IRN;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
public record DeleteResourcesRequestDto(List<IRN> irns) {

  @JsonProperty("resourceIDs")
  public List<String> getResourceIds() {
    return this.irns.stream().map(IRN::toBase64).toList();
  }
}
