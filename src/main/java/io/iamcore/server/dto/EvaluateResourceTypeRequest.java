package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EvaluateResourceTypeRequest(
    String action,
    String application,
    String resourceType,  
    @JsonProperty("tenantID") String tenantId
) {}
