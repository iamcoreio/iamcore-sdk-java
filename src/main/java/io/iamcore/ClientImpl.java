package io.iamcore;

import static io.iamcore.authentication.context.SecurityContextHolder.getSecurityContext;
import static io.iamcore.authentication.context.SecurityContextHolder.initializeSecurityContext;

import io.iamcore.authentication.Authenticator;
import io.iamcore.authentication.HttpHeaderAuthenticator;
import io.iamcore.authentication.context.SecurityContext;
import io.iamcore.exception.SdkException;
import io.iamcore.server.ServerClient;
import io.iamcore.server.ServerClientImpl;
import io.iamcore.server.dto.CreateResourceRequestDto;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

public class ClientImpl implements Client {

  public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
  public static final String API_KEY_HEADER_NAME = "X-iamcore-API-Key";

  private final List<Authenticator> authenticators;
  private final boolean disabled;
  private final HttpHeader apiKeyHeader;
  private final ServerClient serverClient;

  public ClientImpl(ClientProperties properties) {
    ServerClient serverClient = new ServerClientImpl(properties.getServerURL());

    this.serverClient = serverClient;
    this.disabled = properties.isDisabled();
    this.apiKeyHeader = new HttpHeader(API_KEY_HEADER_NAME, properties.getApiKey());

    HttpHeaderAuthenticator bearerAuthenticator = new HttpHeaderAuthenticator(serverClient, AUTHORIZATION_HEADER_NAME);
    HttpHeaderAuthenticator apiKeyAuthenticator = new HttpHeaderAuthenticator(serverClient, API_KEY_HEADER_NAME);
    this.authenticators = Arrays.asList(bearerAuthenticator, apiKeyAuthenticator);
  }

  @Override
  public void authenticate(HttpServletRequest request) {
    if (disabled) {
      return;
    }

    for (Authenticator authenticator : authenticators) {
      Optional<SecurityContext> securityContext = authenticator.authenticate(request);

      if (securityContext.isPresent()) {
        initializeSecurityContext(securityContext.get());

        return;
      }
    }

    throw new SdkException("Failed to authenticate request with any of available authenticators");
  }

  @Override
  public Set<String> authorize(HttpHeader authorizationHeader, String application, String resourceType, String resourcePath, Set<String> resourceIds, String action) {
    if (StringUtils.isEmpty(action)) {
      throw new SdkException("Action must be defined");
    }

    if (disabled) {
      return resourceIds;
    }

    if (Objects.nonNull(resourceIds) && !resourceIds.isEmpty()) {
      IRN principalIRN = getSecurityContext().getPrincipalIRN();

      List<IRN> resourceIRNs = resourceIds.stream()
          .map(resourceID -> IRN.of(principalIRN.getAccountId(), application, principalIRN.getTenantId(), null, resourceType,
              resourcePath, resourceID))
          .collect(Collectors.toList());

      serverClient.authorizeOnResources(authorizationHeader, action, resourceIRNs);

      return resourceIds;
    }

    return serverClient.authorizedOnResourceType(authorizationHeader, action, application, resourceType).stream()
        .map(IRN::getResourceId)
        .collect(Collectors.toSet());
  }

  @Override
  public void createResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath,
      String resourceId) {
    if (disabled) {
      return;
    }

    CreateResourceRequestDto requestDto = new CreateResourceRequestDto(application, tenantId, resourceType, resourcePath, resourceId, true);
    serverClient.createResource(authorizationHeader, requestDto);
  }

  @Override
  public void deleteResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath,
      String resourceId) {
    if (disabled) {
      return;
    }

    IRN principalIRN = serverClient.getPrincipalIRN(authorizationHeader);
    IRN resourceIRN = IRN.of(principalIRN.getAccountId(), application, tenantId, null, resourceType, resourcePath, resourceId);

    serverClient.deleteResource(authorizationHeader, resourceIRN);
  }

  @Override
  public HttpHeader getApiKeyHeader() {
    return apiKeyHeader;
  }
}
