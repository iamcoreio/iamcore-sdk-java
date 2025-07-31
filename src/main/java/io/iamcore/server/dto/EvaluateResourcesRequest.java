package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
public record EvaluateResourcesRequest(String action, List<String> resources) {}
