package io.iamcore.server;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.server.dto.CreateResourceRequestDto;

import java.util.List;

public interface ServerClient {

  IRN getPrincipalIRN(HttpHeader header);

  void authorizeOnResources(HttpHeader authorizationHeader, String action, List<IRN> resources);

  List<IRN> authorizedOnResourceType(HttpHeader header, String action, String application,
      String resourceType);

  void createResource(HttpHeader header, CreateResourceRequestDto requestDto);

  void deleteResource(HttpHeader header, IRN resourceIrn);
}
