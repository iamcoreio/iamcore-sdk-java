package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageableResponse<T>(
    @JsonProperty("data") List<T> data,
    @JsonProperty("count") Integer count,
    @JsonProperty("page") Integer page,
    @JsonProperty("pageSize") Integer pageSize) {

  public PageableResponse {
    if (data == null) {
      data = List.of();
    }
  }
}
