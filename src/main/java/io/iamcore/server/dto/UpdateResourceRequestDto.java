package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateResourceRequestDto(@JsonProperty("poolIDs") Set<String> poolIds) {

  public Set<String> getPoolIds() {
    return poolIds;
  }
}
