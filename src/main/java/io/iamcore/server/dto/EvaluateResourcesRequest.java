package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EvaluateResourcesRequest(
    String action, List<String> resources) {}
