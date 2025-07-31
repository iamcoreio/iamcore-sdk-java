package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iamcore.StringUtils;
import java.util.Set;

@JsonInclude(Include.NON_EMPTY)
public record CreateResourceRequestDto(
    String application,
    @JsonProperty("tenantID") String tenantId,
    @JsonProperty("resourceType") String resourceType,
    String path,
    String name,
    Boolean enabled,
    @JsonProperty("poolIDs") Set<String> poolIds) {

  public CreateResourceRequestDto(
      String application,
      String tenantId,
      String resourceType,
      String path,
      String name,
      Boolean enabled,
      Set<String> poolIds) {
    this.application = application;
    this.tenantId = tenantId;
    this.resourceType = resourceType;
    this.path = StringUtils.isEmpty(path) ? "/" : path;
    this.name = name;
    this.enabled = enabled;
    this.poolIds = poolIds;
  }

  /* Getters for backward compatibility */

  public String getApplication() {
    return application;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getPath() {
    return path;
  }

  public String getName() {
    return name;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public Set<String> getPoolIds() {
    return poolIds;
  }
}
