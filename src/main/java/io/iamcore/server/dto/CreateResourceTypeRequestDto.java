package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iamcore.StringUtils;
import io.iamcore.exception.SdkException;
import java.util.Set;

@JsonInclude(Include.NON_EMPTY)
public record CreateResourceTypeRequestDto(
    String type,
    String description,
    @JsonProperty("actionPrefix") String actionPrefix,
    Set<String> operations) {

  public CreateResourceTypeRequestDto {
    if (StringUtils.isEmpty(type)) {
      throw new SdkException("resource type must be defined");
    }
  }

  /* Getters for backward compatibility */

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public String getActionPrefix() {
    return actionPrefix;
  }

  public Set<String> getOperations() {
    return operations;
  }
}
