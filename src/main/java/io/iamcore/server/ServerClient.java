package io.iamcore.server;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.CreateResourceTypeRequestDto;
import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.DeleteResourcesRequestDto;
import io.iamcore.server.dto.PoolResponse;
import io.iamcore.server.dto.PoolsQueryFilter;
import io.iamcore.server.dto.ResourceTypeDto;
import io.iamcore.server.dto.UpdateResourceRequestDto;
import java.util.List;
import java.util.Optional;

public interface ServerClient {

  IRN getPrincipalIrn(HttpHeader header);

  void authorizedOnIrns(HttpHeader authorizationHeader, String action, List<IRN> irns);

  List<IRN> authorizedOnResources(HttpHeader authorizationHeader, String action, List<IRN> resources);

  List<IRN> authorizedOnResourceType(HttpHeader header, String action, String application,
      String tenantId, String resourceType);

  String authorizationDbQueryFilter(HttpHeader header, String action, Database database);

  IRN createResource(HttpHeader header, CreateResourceRequestDto requestDto);

  void updateResource(HttpHeader header, IRN resourceIrn, UpdateResourceRequestDto requestDto);

  void deleteResource(HttpHeader header, IRN resourceIrn);

  void deleteResources(HttpHeader header, DeleteResourcesRequestDto requestDto);

  void createResourceType(HttpHeader header, IRN application, CreateResourceTypeRequestDto requestDto);

  List<ResourceTypeDto> getResourceTypes(HttpHeader header, IRN applicationIrn);

  Optional<String> getPrincipalApiKey(HttpHeader header, IRN principalIrn);

  String createPrincipalApiKey(HttpHeader header, IRN principalIrn);

  List<PoolResponse> getPools(HttpHeader header, PoolsQueryFilter filter);
}
