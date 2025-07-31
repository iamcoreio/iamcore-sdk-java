package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluateResourceTypeRequest(
    String action,
    String application,
    @JsonProperty("resourceType") String resourceType,
    @JsonProperty("tenantID") String tenantId) {}
