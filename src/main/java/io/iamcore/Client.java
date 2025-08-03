package io.iamcore;

import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.PoolsQueryFilter;
import io.iamcore.server.dto.ResourceTypeDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public interface Client {

  void authenticate(HttpServletRequest request);

  Set<String> authorizeIrns(HttpHeader authorizationHeader, String accountId, String application,
      String tenantId, String resourceType, String resourcePath, Set<String> resourceIds,
      String action);

  Set<String> authorizeResources(HttpHeader authorizationHeader, String accountId, String application,
      String tenantId, String resourceType, String resourcePath, Set<String> resourceIds,
      String action);

  String authorizationDbQueryFilter(HttpHeader authorizationHeader, String action,
      Database database);

  IRN createResource(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath, String resourceId);

  IRN createResource(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath, String resourceId, Set<String> poolIds);

  void updateResource(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath, String resourceId, Set<String> poolIds);

  void deleteResource(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath, String resourceId);

  List<ResourceTypeDto> getResourceTypes(HttpHeader authorizationHeader, String accountId,
      String application);

  void deleteResources(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath, Set<String> resourceIds);

  void createResourceType(HttpHeader authorizationHeader, String accountId, String application,
      String type, String description, String actionPrefix, Set<String> operations);

  HttpHeader getApplicationApiKeyHeader();

  HttpHeader getTenantApiKeyHeader(String accountId, String tenantId);

  Set<String> getPoolIds(HttpHeader authorizationHeader, PoolsQueryFilter filter);
}
