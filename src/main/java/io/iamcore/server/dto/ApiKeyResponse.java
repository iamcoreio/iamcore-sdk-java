package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiKeyResponse(
    @JsonProperty("apiKey") String apiKey, @JsonProperty("state") String state) {}
