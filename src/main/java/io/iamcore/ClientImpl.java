package io.iamcore;

import static io.iamcore.authentication.context.SecurityContextHolder.initializeSecurityContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.iamcore.authentication.AnonymousAuthenticator;
import io.iamcore.authentication.Authenticator;
import io.iamcore.authentication.HttpHeaderAuthenticator;
import io.iamcore.authentication.context.SecurityContext;
import io.iamcore.exception.SdkException;
import io.iamcore.server.ServerClient;
import io.iamcore.server.ServerClientImpl;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.CreateResourceTypeRequestDto;
import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.ResourceTypeDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@FunctionalInterface
interface TriFunction<T, U, O> {
  void apply(T t, U u, O o);
}

public class ClientImpl implements Client {

  public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
  public static final String API_KEY_HEADER_NAME = "X-iamcore-API-Key";

  private final Authenticator[] authenticators;
  private final AnonymousAuthenticator anonymousAuthenticator;
  private final boolean disabled;
  private final HttpHeader apiKeyHeader;
  private final ServerClient serverClient;

  public ClientImpl(ClientProperties properties) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    ServerClient serverClient = new ServerClientImpl(properties.getServerUrl(), objectMapper);

    this.serverClient = serverClient;
    this.disabled = properties.isDisabled();
    this.apiKeyHeader = new HttpHeader(API_KEY_HEADER_NAME, properties.getApiKey());

    HttpHeaderAuthenticator bearerAuthenticator = new HttpHeaderAuthenticator(serverClient,
        AUTHORIZATION_HEADER_NAME);
    HttpHeaderAuthenticator apiKeyAuthenticator = new HttpHeaderAuthenticator(serverClient,
        API_KEY_HEADER_NAME);
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
  public Set<String> authorize(HttpHeader authorizationHeader, String accountId, String application,
      String tenantId, String resourceType, String resourcePath, Set<String> resourceIds, String action) {
    return authorize(authorizationHeader, accountId, application, tenantId,
        resourceType, resourcePath, resourceIds, action, serverClient::authorizeOnResources);
  }

  @Override
  public Set<String> authorizeResources(HttpHeader authorizationHeader, String accountId, String application,
      String tenantId, String resourceType, String resourcePath, Set<String> resourceIds, String action) {
    return authorize(authorizationHeader, accountId, application, tenantId,
        resourceType, resourcePath, resourceIds, action, serverClient::authorizeResources);
  }

  private Set<String> authorize(HttpHeader authorizationHeader, String accountId, String application,
      String tenantId, String resourceType, String resourcePath, Set<String> resourceIds, String action,
      TriFunction<HttpHeader, String, List<IRN>> authorizationFunction) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    if (StringUtils.isEmpty(action)) {
      throw new SdkException("Action must be defined");
    }

    if (Objects.nonNull(resourceIds) && !resourceIds.isEmpty()) {
      List<IRN> resourceIrns = resourceIds.stream()
          .map(resourceID -> IRN.of(accountId, application, tenantId, null, resourceType,
              resourcePath, resourceID))
          .collect(Collectors.toList());

      authorizationFunction.apply(authorizationHeader, action, resourceIrns);

      return resourceIds;
    }

    return serverClient.authorizedOnResourceType(authorizationHeader, action, application, tenantId,
            resourceType).stream()
        .map(IRN::getResourceId)
        .collect(Collectors.toSet());
  }

  @Override
  public String authorizationDbQueryFilter(HttpHeader authorizationHeader, String action,
      Database database) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    if (StringUtils.isEmpty(action)) {
      throw new SdkException("Action must be defined");
    }

    if (database == null) {
      throw new SdkException("Database must be defined");
    }

    return serverClient.authorizationDbQueryFilter(authorizationHeader, action, database);
  }

  @Override
  public IRN createResource(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath, String resourceId) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    CreateResourceRequestDto requestDto = new CreateResourceRequestDto(application, tenantId,
        resourceType, resourcePath, resourceId, true);
    return serverClient.createResource(authorizationHeader, requestDto);
  }

  @Override
  public void deleteResource(HttpHeader authorizationHeader, String application, String tenantId,
      String resourceType, String resourcePath,
      String resourceId) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    IRN principalIrn = serverClient.getPrincipalIrn(authorizationHeader);
    IRN resourceIrn = IRN.of(principalIrn.getAccountId(), application, tenantId, null, resourceType,
        resourcePath, resourceId);

    serverClient.deleteResource(authorizationHeader, resourceIrn);
  }

  @Override
  public void createResourceType(HttpHeader authorizationHeader, String accountId,
      String application, String type, String description,
      String actionPrefix, Set<String> operations) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    IRN applicationIrn = IRN.of(accountId, "iamcore", "", null, "application", null, application);
    CreateResourceTypeRequestDto requestDto = new CreateResourceTypeRequestDto(type, description,
        actionPrefix, operations);

    serverClient.createResourceType(authorizationHeader, applicationIrn, requestDto);
  }

  @Override
  public List<ResourceTypeDto> getResourceTypes(HttpHeader authorizationHeader, String accountId,
      String application) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    IRN applicationIrn = IRN.of(accountId, "iamcore", "", null, "application", null, application);

    return serverClient.getResourceTypes(authorizationHeader, applicationIrn);
  }

  @Override
  public HttpHeader getApplicationApiKeyHeader() {
    return apiKeyHeader;
  }

  @Override
  public HttpHeader getTenantApiKeyHeader(String accountId, String tenantId) {
    if (disabled) {
      throw new SdkException("Iamcore disabled");
    }

    IRN tenantIrn = IRN.of(accountId, "iamcore", tenantId, null, "tenant", null, tenantId);
    String apiKey = serverClient.getPrincipalApiKey(apiKeyHeader, tenantIrn)
        .orElseGet(() -> serverClient.createPrincipalApiKey(apiKeyHeader, tenantIrn));

    return new HttpHeader(API_KEY_HEADER_NAME, apiKey);
  }
}
