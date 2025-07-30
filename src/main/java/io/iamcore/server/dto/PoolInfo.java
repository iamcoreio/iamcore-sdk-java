package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iamcore.IRN;
import io.iamcore.StringUtils;
import java.util.List;

public record PoolInfo(String id, IRN irn, String name, List<String> resources) {

  @JsonCreator
  public PoolInfo(
      @JsonProperty("id") String id,
      @JsonProperty("irn") String irn,
      @JsonProperty("name") String name,
      @JsonProperty("resources") List<String> resources) {
    this(id, StringUtils.isEmpty(irn) ? null : IRN.from(irn), name, resources);
  }
}
