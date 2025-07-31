package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiKeyResponse(@JsonProperty("apiKey") String apiKey, String state) {}
