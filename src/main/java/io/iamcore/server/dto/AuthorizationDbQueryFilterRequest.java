package io.iamcore.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorizationDbQueryFilterRequest(
    String action, String database) {}
