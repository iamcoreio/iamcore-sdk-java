package io.iamcore.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.StringUtils;
import io.iamcore.exception.IamcoreServerException;
import io.iamcore.exception.SdkException;
import io.iamcore.server.dto.ApiKeyResponse;
import io.iamcore.server.dto.AuthorizationDbQueryFilterRequest;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.CreateResourceTypeRequestDto;
import io.iamcore.server.dto.DataResponse;
import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.DeleteResourcesRequestDto;
import io.iamcore.server.dto.EvaluateResourceTypeRequest;
import io.iamcore.server.dto.EvaluateResourcesRequest;
import io.iamcore.server.dto.PageableResponse;
import io.iamcore.server.dto.PoolResponse;
import io.iamcore.server.dto.PoolsQueryFilter;
import io.iamcore.server.dto.ResourceResponse;
import io.iamcore.server.dto.ResourceTypeDto;
import io.iamcore.server.dto.UpdateResourceRequestDto;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerClientImpl implements ServerClient {

  public static final String USER_IRN_PATH = "/api/v1/users/me/irn";
  public static final String EVALUATE_PATH = "/api/v1/evaluate";
  public static final String RESOURCES_EVALUATE_PATH =
      "/api/v1/resources/evaluate?filterResources=true";
  public static final String EVALUATE_RESOURCES_PATH = "/api/v1/evaluate/resources";
  public static final String AUTHORIZATION_QUERY_FILTER_PATH =
      "/api/v1/evaluate/database-query-filter";
  public static final String RESOURCE_PATH = "/api/v1/resources";
  public static final String APPLICATION_PATH = "/api/v1/applications";
  public static final String RESOURCE_TYPE_PATH_TEMPLATE = APPLICATION_PATH + "/%s/resource-types";
  public static final String API_KEY_PATH_TEMPLATE = "/api/v1/principals/%s/api-keys";
  public static final String POOLS_PATH = "/api/v1/pools";
  private static final int PAGE_SIZE = 100000;

  private final URI serverUrl;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public ServerClientImpl(URI serverUrl, ObjectMapper objectMapper) {
    this.serverUrl = serverUrl;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
  }

  @Override
  public IRN getPrincipalIrn(HttpHeader header) {
    DataResponse<String> principalIrnResponse =
        executeRequest(USER_IRN_PATH, "GET", header, null, new TypeReference<>() {});

    return IRN.from(principalIrnResponse.data());
  }

  @Override
  public void authorizedOnIrns(HttpHeader authorizationHeader, String action, List<IRN> irns) {
    EvaluateResourcesRequest requestBody =
        new EvaluateResourcesRequest(action, irns.stream().map(IRN::toString).toList());

    executeRequest(EVALUATE_PATH, "POST", authorizationHeader, requestBody);
  }

  @Override
  public List<IRN> authorizedOnResources(
      HttpHeader authorizationHeader, String action, List<IRN> resources) {
    EvaluateResourcesRequest requestBody =
        new EvaluateResourcesRequest(action, resources.stream().map(IRN::toString).toList());

    List<String> evaluatedResourceIrns =
        executeRequest(
            RESOURCES_EVALUATE_PATH,
            "POST",
            authorizationHeader,
            requestBody,
            new TypeReference<>() {});

    return evaluatedResourceIrns.stream().map(IRN::from).toList();
  }

  @Override
  public List<IRN> authorizedOnResourceType(
      HttpHeader header, String action, String application, String tenantId, String resourceType) {
    EvaluateResourceTypeRequest requestBody =
        new EvaluateResourceTypeRequest(
            action, application, resourceType, StringUtils.isEmpty(tenantId) ? null : tenantId);

    String path = String.format("%s?pageSize=%s", EVALUATE_RESOURCES_PATH, PAGE_SIZE);

    PageableResponse<String> pageOfResourceIrns =
        executeRequest(path, "POST", header, requestBody, new TypeReference<>() {});

    return pageOfResourceIrns.data().stream().map(IRN::from).toList();
  }

  @Override
  public String authorizationDbQueryFilter(
      HttpHeader authorizationHeader, String action, Database database) {
    AuthorizationDbQueryFilterRequest requestBody =
        new AuthorizationDbQueryFilterRequest(action, database.getValue());

    DataResponse<String> dbQueryFilterResponse =
        executeRequest(
            AUTHORIZATION_QUERY_FILTER_PATH,
            "POST",
            authorizationHeader,
            requestBody,
            new TypeReference<>() {});

    return dbQueryFilterResponse.data();
  }

  @Override
  public IRN createResource(HttpHeader authorizationHeader, CreateResourceRequestDto requestDto) {
    DataResponse<ResourceResponse> resourceResponse =
        executeRequest(
            RESOURCE_PATH, "POST", authorizationHeader, requestDto, new TypeReference<>() {});

    return resourceResponse.data().irn();
  }

  @Override
  public void updateResource(
      HttpHeader header, IRN resourceIrn, UpdateResourceRequestDto updateDto) {
    String path = RESOURCE_PATH + "/" + resourceIrn.toBase64();
    executeRequest(path, "PATCH", header, updateDto);
  }

  @Override
  public void deleteResource(HttpHeader header, IRN resourceIrn) {
    String path = RESOURCE_PATH + "/" + resourceIrn.toBase64();
    executeRequest(path, "DELETE", header, null);
  }

  @Override
  public void deleteResources(HttpHeader header, DeleteResourcesRequestDto requestDto) {
    executeRequest(RESOURCE_PATH + "/delete", "POST", header, requestDto);
  }

  @Override
  public void createResourceType(
      HttpHeader header, IRN application, CreateResourceTypeRequestDto requestDto) {
    String path = String.format(RESOURCE_TYPE_PATH_TEMPLATE, application.toBase64());
    executeRequest(path, "POST", header, requestDto);
  }

  @Override
  public List<ResourceTypeDto> getResourceTypes(HttpHeader header, IRN applicationIrn) {
    String path =
        RESOURCE_TYPE_PATH_TEMPLATE.formatted(applicationIrn.toBase64()) + "?pageSize=" + PAGE_SIZE;

    PageableResponse<ResourceTypeDto> pageOfResourceTypes =
        executeRequest(path, "GET", header, null, new TypeReference<>() {});

    return pageOfResourceTypes.data();
  }

  @Override
  public Optional<String> getPrincipalApiKey(HttpHeader header, IRN principalIrn) {
    String path =
        API_KEY_PATH_TEMPLATE.formatted(principalIrn.toBase64()) + "?state=active&pageSize=1";

    PageableResponse<ApiKeyResponse> pageOfApiKeys =
        executeRequest(path, "GET", header, null, new TypeReference<>() {});

    return pageOfApiKeys.data().stream().findFirst().map(ApiKeyResponse::apiKey);
  }

  @Override
  public String createPrincipalApiKey(HttpHeader header, IRN principalIrn) {
    String url = API_KEY_PATH_TEMPLATE.formatted(principalIrn.toBase64());

    return executeRequest(url, "POST", header, null, this::getIdFromLocationHeader);
  }

  private String getIdFromLocationHeader(HttpResponse<String> response) {
    Optional<String> location = response.headers().firstValue("Location");
    if (location.isEmpty()) {
      throw new IamcoreServerException(
          "Location header not found in response for created API Key", response.statusCode());
    }

    return location.get().substring(location.get().lastIndexOf('/') + 1);
  }

  @Override
  public List<PoolResponse> getPools(HttpHeader header, PoolsQueryFilter filter) {
    String poolIrn = filter.irn() == null ? "" : filter.irn().toBase64();
    String poolName = filter.name();
    String resourceIrn = filter.resourceIrn() == null ? "" : filter.resourceIrn().toBase64();

    Map<String, String> queryParams =
        Map.of(
            "pageSize", String.valueOf(PAGE_SIZE),
            "irn", poolIrn,
            "name", poolName,
            "resourceIRN", resourceIrn);

    String rawQuery = buildRawQuery(queryParams);
    String path = POOLS_PATH + "?" + rawQuery;

    PageableResponse<PoolResponse> pageOfPools =
        executeRequest(path, "GET", header, null, new TypeReference<>() {});

    return pageOfPools.data();
  }

  private String buildRawQuery(Map<String, String> queryParams) {
    return queryParams.entrySet().stream()
        .filter(entry -> !StringUtils.isEmpty(entry.getValue()))
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  private HttpResponse<String> sendRequest(
      String path, String method, HttpHeader header, Object body)
      throws IOException, InterruptedException {
    URI requestUri = serverUrl.resolve(path);

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(requestUri);

    if (header != null) {
      requestBuilder.header(header.getName(), header.getValue());
    }

    if (body != null) {
      String jsonBody = objectMapper.writeValueAsString(body);
      HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonBody);

      switch (method) {
        case "POST" -> requestBuilder.POST(bodyPublisher);
        case "PUT" -> requestBuilder.PUT(bodyPublisher);
        case "PATCH" -> requestBuilder.method("PATCH", bodyPublisher);
        default -> throw new SdkException("Unsupported HTTP method with body: " + method);
      }
      requestBuilder.header("Content-Type", "application/json");
    } else {
      switch (method) {
        case "GET" -> requestBuilder.GET();
        case "DELETE" -> requestBuilder.DELETE();
        case "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
        default -> requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
      }
    }

    HttpRequest request = requestBuilder.build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private void executeRequest(String path, String method, HttpHeader header, Object requestBody) {
    executeRequest(path, method, header, requestBody, response -> null);
  }

  private <T> T executeRequest(
      String path, String method, HttpHeader header, Object requestBody, TypeReference<T> ref) {
    return executeRequest(
        path, method, header, requestBody, response -> readResponse(response.body(), ref));
  }

  private <T> T executeRequest(
      String path,
      String method,
      HttpHeader header,
      Object requestBody,
      ResponseProcessor<T> responseProcessor) {
    try {
      HttpResponse<String> response = sendRequest(path, method, header, requestBody);
      int responseCode = response.statusCode();

      if (responseCode >= 200 && responseCode < 300) {
        return responseProcessor.process(response);
      } else {
        String errorMessage = parseErrorResponse(response.body());
        throw new IamcoreServerException(
            "Server error (Status " + responseCode + "): " + errorMessage, responseCode);
      }
    } catch (IOException | InterruptedException ex) {
      throw new SdkException(
          "Network or I/O error during request to " + path + ": " + ex.getMessage());
    }
  }

  private String parseErrorResponse(String responseBody) {
    try {
      if (responseBody == null || responseBody.trim().isEmpty()) {
        return "No error response body available.";
      }

      Map<String, String> errorResponse =
          readResponse(responseBody, new TypeReference<Map<String, String>>() {});

      return errorResponse.getOrDefault("message", "Unknown server error.");
    } catch (SdkException ex) {
      return "Failed to parse error response: " + responseBody;
    }
  }

  private <T> T readResponse(String responseBody, TypeReference<T> ref) {
    try {
      return objectMapper.readValue(responseBody, ref);
    } catch (IOException ex) {
      throw new SdkException("Failed to read or parse response: " + ex.getMessage());
    }
  }

  @FunctionalInterface
  private interface ResponseProcessor<T> {

    T process(HttpResponse<String> response) throws IOException;
  }
}
