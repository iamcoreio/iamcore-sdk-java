package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iamcore.IRN;
import io.iamcore.StringUtils;
import java.util.List;

public record ResourceResponse(
    String id,
    IRN irn,
    String application,
    String tenantId,
    String name,
    String displayName,
    String path,
    String resourceType,
    Boolean enabled,
    String description,
    Object metadata,
    List<String> poolIds) {

  @JsonCreator
  public ResourceResponse(
      @JsonProperty("resourceID") String id,
      @JsonProperty("irn") String irn,
      @JsonProperty("application") String application,
      @JsonProperty("tenantID") String tenantId,
      @JsonProperty("name") String name,
      @JsonProperty("displayName") String displayName,
      @JsonProperty("path") String path,
      @JsonProperty("resourceType") String resourceType,
      @JsonProperty("enabled") Boolean enabled,
      @JsonProperty("description") String description,
      @JsonProperty("metadata") Object metadata,
      @JsonProperty("poolIDs") List<String> poolIds) {
    this(id, StringUtils.isEmpty(irn) ? null : IRN.from(irn), application, tenantId, name,
        displayName, path, resourceType, enabled, description, metadata, poolIds);
  }
}
