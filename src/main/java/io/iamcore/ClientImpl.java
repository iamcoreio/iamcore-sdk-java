package io.iamcore;

import static io.iamcore.authentication.context.SecurityContextHolder.initializeSecurityContext;

import io.iamcore.authentication.Authenticator;
import io.iamcore.authentication.AnonymousAuthenticator;
import io.iamcore.authentication.HttpHeaderAuthenticator;
import io.iamcore.authentication.context.SecurityContext;
import io.iamcore.exception.SdkException;
import io.iamcore.server.ServerClient;
import io.iamcore.server.ServerClientImpl;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.Database;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

public class ClientImpl implements Client {

  public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
  public static final String API_KEY_HEADER_NAME = "X-iamcore-API-Key";

  private final Authenticator[] authenticators;
  private final AnonymousAuthenticator anonymousAuthenticator;
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
    this.authenticators = new Authenticator[]{
        bearerAuthenticator,
        apiKeyAuthenticator,
    };
    this.anonymousAuthenticator = new AnonymousAuthenticator(serverClient);
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

    initializeSecurityContext(anonymousAuthenticator.authenticate());
  }

  @Override
  public Set<String> authorize(HttpHeader authorizationHeader, String accountId, String application, String tenantId, String resourceType,
      String resourcePath, Set<String> resourceIds, String action) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    if (StringUtils.isEmpty(action)) {
      throw new SdkException("Action must be defined");
    }

    if (Objects.nonNull(resourceIds) && !resourceIds.isEmpty()) {
      List<IRN> resourceIRNs = resourceIds.stream()
          .map(resourceID -> IRN.of(accountId, application, tenantId, null, resourceType, resourcePath, resourceID))
          .collect(Collectors.toList());

      serverClient.authorizeOnResources(authorizationHeader, action, resourceIRNs);

      return resourceIds;
    }

    return serverClient.authorizedOnResourceType(authorizationHeader, action, application, tenantId, resourceType).stream()
        .map(IRN::getResourceId)
        .collect(Collectors.toSet());
  }

  @Override
  public String authorizationDBQueryFilter(HttpHeader authorizationHeader, String action, Database database) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    if (StringUtils.isEmpty(action)) {
      throw new SdkException("Action must be defined");
    }

    if (database == null) {
      throw new SdkException("Database must be defined");
    }

    return serverClient.authorizationDBQueryFilter(authorizationHeader, action, database);
  }

  @Override
  public IRN createResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath, String resourceId) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    CreateResourceRequestDto requestDto = new CreateResourceRequestDto(application, tenantId, resourceType, resourcePath, resourceId, true);
    return serverClient.createResource(authorizationHeader, requestDto);
  }

  @Override
  public void deleteResource(HttpHeader authorizationHeader, String application, String tenantId, String resourceType, String resourcePath,
      String resourceId) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
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
