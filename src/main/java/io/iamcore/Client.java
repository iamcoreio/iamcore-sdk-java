package io.iamcore;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public interface Client {

  void authenticate(HttpServletRequest request);

  Set<String> authorize(HttpHeader authorizationHeader,String application, String tenantId, String resourceType, String resourcePath, Set<String> resourceIds,
      String action);

  void createResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath,
      String resourceId);

  void deleteResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath,
      String resourceId);

  HttpHeader getApiKeyHeader();
}
