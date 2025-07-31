package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record UpdateResourceRequestDto(@JsonProperty("poolIDs") Set<String> poolIds) {

  public Set<String> getPoolIds() {
    return poolIds;
  }
}
