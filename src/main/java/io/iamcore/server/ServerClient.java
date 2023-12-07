package io.iamcore.server;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.CreateResourceTypeRequestDto;
import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.ResourceTypeDto;

import java.util.List;

public interface ServerClient {

  IRN getPrincipalIRN(HttpHeader header);

  void authorizeOnResources(HttpHeader authorizationHeader, String action, List<IRN> resources);

  List<IRN> authorizedOnResourceType(HttpHeader header, String action, String application,
      String tenantId, String resourceType);

  String authorizationDBQueryFilter(HttpHeader header, String action, Database database);

  IRN createResource(HttpHeader header, CreateResourceRequestDto requestDto);

  void deleteResource(HttpHeader header, IRN resourceIrn);

  void createResourceType(HttpHeader header, IRN application, CreateResourceTypeRequestDto requestDto);

  List<ResourceTypeDto> getResourceTypes(HttpHeader header, IRN applicationIRN);
}
