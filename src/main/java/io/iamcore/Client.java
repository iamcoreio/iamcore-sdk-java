package io.iamcore;

import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.ResourceTypeDto;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public interface Client {

  void authenticate(HttpServletRequest request);

  Set<String> authorize(HttpHeader authorizationHeader, String accountId, String application, String tenantId, String resourceType, String resourcePath, Set<String> resourceIds,
      String action);

  String authorizationDBQueryFilter(HttpHeader authorizationHeader, String action, Database database);

  IRN createResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath, String resourceId);

  void deleteResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath, String resourceId);

  List<ResourceTypeDto> getResourceTypes(HttpHeader authorizationHeader, String accountId, String application);

  void createResourceType(HttpHeader authorizationHeader, String accountId, String application, String type, String description, String actionPrefix, Set<String> operations);

  HttpHeader getApiKeyHeader();
}
