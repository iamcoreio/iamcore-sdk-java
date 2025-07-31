package io.iamcore.server;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.iamcore.server.ServerClientImpl.*;
import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.exception.IamcoreServerException;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.CreateResourceTypeRequestDto;
import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.DeleteResourcesRequestDto;
import io.iamcore.server.dto.PoolResponse;
import io.iamcore.server.dto.PoolsQueryFilter;
import io.iamcore.server.dto.ResourceTypeDto;
import io.iamcore.server.dto.UpdateResourceRequestDto;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class ServerClientTest {

  private static final String TEST_AUTH_TOKEN = "test-auth-token";

  private IRN testPrincipalIrn;
  private IRN testResourceIrn;
  private IRN testApplicationIrn;
  private HttpHeader authHeader;

  // SUT
  private ServerClient serverClient;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    ObjectMapper objectMapper = new ObjectMapper();
    URI connectionUrl = URI.create(wmRuntimeInfo.getHttpBaseUrl());
    testPrincipalIrn = IRN.of("iamcore", "user", "", "/pool", "user", "", "test-user");
    testResourceIrn = IRN.of("iamcore", "resource", "", "/pool", "document", "", "test-doc");
    testApplicationIrn =
        IRN.of("iamcore", "application", "", "/pool", "application", "", "test-app");
    authHeader = new HttpHeader("Authorization", TEST_AUTH_TOKEN);
    serverClient = new ServerClientImpl(connectionUrl, objectMapper);
  }

  @Test
  void getPrincipalIrn_happyPath() {
    // given
    stubFor(
        get(urlEqualTo(USER_IRN_PATH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": \"" + testPrincipalIrn + "\"}")));

    // when
    IRN principalIrn = serverClient.getPrincipalIrn(authHeader);

    // then
    assertThat(testPrincipalIrn.toBase64()).isEqualTo(principalIrn.toBase64());
  }

  @Test
  void getPrincipalIrn_serverError() {
    // given
    stubFor(
        get(urlEqualTo(USER_IRN_PATH))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Internal server error\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.getPrincipalIrn(authHeader))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Internal server error");
  }

  @Test
  void authorizedOnIrns_happyPath() {
    // given
    List<IRN> irns = Collections.singletonList(testResourceIrn);
    stubFor(
        post(urlEqualTo(EVALUATE_PATH))
            .withRequestBody(containing("\"action\":\"read\""))
            .withRequestBody(containing("\"resources\":[\"" + testResourceIrn + "\"]"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

    // when & then
    assertThatCode(() -> serverClient.authorizedOnIrns(authHeader, "read", irns))
        .doesNotThrowAnyException();
  }

  @Test
  void authorizedOnIrns_unauthorized() {
    // given
    List<IRN> irns = Collections.singletonList(testResourceIrn);
    stubFor(
        post(urlEqualTo(EVALUATE_PATH))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Access denied\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.authorizedOnIrns(authHeader, "read", irns))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Access denied");
  }

  @Test
  void authorizedOnResources_happyPath() {
    // given
    List<IRN> resources = Collections.singletonList(testResourceIrn);
    stubFor(
        post(urlEqualTo(RESOURCES_EVALUATE_PATH))
            .withRequestBody(containing("\"action\":\"read\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"" + testResourceIrn + "\"]")));

    // when
    List<IRN> authorizedResources =
        serverClient.authorizedOnResources(authHeader, "read", resources);

    // then
    assertThat(authorizedResources).hasSize(1);
    assertThat(authorizedResources.getFirst().toBase64()).isEqualTo(testResourceIrn.toBase64());
  }

  @Test
  void authorizedOnResources_emptyResult() {
    // given
    List<IRN> resources = Collections.singletonList(testResourceIrn);
    stubFor(
        post(urlEqualTo(RESOURCES_EVALUATE_PATH))
            .withRequestBody(containing("\"action\":\"read\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[]")));

    // when
    List<IRN> authorizedResources =
        serverClient.authorizedOnResources(authHeader, "read", resources);

    // then
    assertThat(authorizedResources).isEmpty();
  }

  @Test
  void authorizedOnResources_serverError() {
    // given
    List<IRN> resources = Collections.singletonList(testResourceIrn);
    stubFor(
        post(urlEqualTo(RESOURCES_EVALUATE_PATH))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Internal server error\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.authorizedOnResources(authHeader, "read", resources))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Internal server error");
  }

  @Test
  void authorizedOnResourceType_happyPath() {
    // given
    String action = "read";
    String application = "test-app";
    String tenantId = "tenant1";
    String resourceType = "document";
    stubFor(
        post(urlPathEqualTo(EVALUATE_RESOURCES_PATH))
            .withQueryParam("pageSize", equalTo("100000"))
            .withRequestBody(containing("\"action\":\"" + action + "\""))
            .withRequestBody(containing("\"application\":\"" + application + "\""))
            .withRequestBody(containing("\"resourceType\":\"" + resourceType + "\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": [\"" + testResourceIrn + "\"]}")));

    // when
    List<IRN> authorizedResources =
        serverClient.authorizedOnResourceType(
            authHeader, action, application, tenantId, resourceType);

    // then
    assertThat(authorizedResources).hasSize(1);
    assertThat(authorizedResources.getFirst().toBase64()).isEqualTo(testResourceIrn.toBase64());
  }

  @Test
  void authorizedOnResourceType_emptyResult() {
    // given
    String action = "read";
    String application = "test-app";
    String tenantId = "tenant1";
    String resourceType = "document";
    stubFor(
        post(urlPathEqualTo(EVALUATE_RESOURCES_PATH))
            .withQueryParam("pageSize", equalTo("100000"))
            .withRequestBody(containing("\"action\":\"" + action + "\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": []}")));

    // when
    List<IRN> authorizedResources =
        serverClient.authorizedOnResourceType(
            authHeader, action, application, tenantId, resourceType);

    // then
    assertThat(authorizedResources).isEmpty();
  }

  @Test
  void authorizedOnResourceType_serverError() {
    // given
    String action = "read";
    String application = "test-app";
    String tenantId = "tenant1";
    String resourceType = "document";
    stubFor(
        post(urlPathEqualTo(EVALUATE_RESOURCES_PATH))
            .withQueryParam("pageSize", equalTo("100000"))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Insufficient permissions\"}")));

    // when & then
    assertThatThrownBy(
            () ->
                serverClient.authorizedOnResourceType(
                    authHeader, action, application, tenantId, resourceType))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Insufficient permissions");
  }

  @Test
  void authorizationDbQueryFilter_happyPath() {
    // given
    String action = "read";
    Database database = Database.MONGO;
    stubFor(
        post(urlEqualTo(AUTHORIZATION_QUERY_FILTER_PATH))
            .withRequestBody(containing("\"action\":\"" + action + "\""))
            .withRequestBody(containing("\"database\":\"" + database.getValue() + "\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": \"{'userId': 'test'}\"}")));

    // when
    String filter = serverClient.authorizationDbQueryFilter(authHeader, action, database);

    // then
    assertThat(filter).contains("test");
  }

  @Test
  void authorizationDbQueryFilter_serverError() {
    // given
    String action = "read";
    Database database = Database.POSTGRES;
    stubFor(
        post(urlEqualTo(AUTHORIZATION_QUERY_FILTER_PATH))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Invalid database type\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.authorizationDbQueryFilter(authHeader, action, database))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Invalid database type");
  }

  @Test
  void createResource_happyPath() {
    // given
    CreateResourceRequestDto requestDto =
        new CreateResourceRequestDto(
            "test-app", "tenant1", "document", "/", "test-resource", true, new HashSet<>());
    stubFor(
        post(urlEqualTo(RESOURCE_PATH))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": {\"irn\": \"" + testResourceIrn + "\"}}")));

    // when
    IRN createdResourceIrn = serverClient.createResource(authHeader, requestDto);

    // then
    assertThat(createdResourceIrn.toBase64()).isEqualTo(testResourceIrn.toBase64());
  }

  @Test
  void createResource_serverError() {
    // given
    CreateResourceRequestDto requestDto =
        new CreateResourceRequestDto(
            "test-app", "tenant1", "document", "/", "test-resource", true, new HashSet<>());
    stubFor(
        post(urlEqualTo(RESOURCE_PATH))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Invalid request\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.createResource(authHeader, requestDto))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Invalid request");
  }

  @Test
  void createResource_withPoolIds() {
    // given
    Set<String> poolIds = new HashSet<>(Arrays.asList("pool1", "pool2"));
    CreateResourceRequestDto requestDto =
        new CreateResourceRequestDto(
            "test-app", "tenant1", "document", "/", "test-resource", true, poolIds);
    stubFor(
        post(urlEqualTo(RESOURCE_PATH))
            .withRequestBody(containing("\"poolIDs\":["))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": {\"irn\": \"" + testResourceIrn + "\"}}")));

    // when
    IRN createdResourceIrn = serverClient.createResource(authHeader, requestDto);

    // then
    assertThat(createdResourceIrn.toBase64()).isEqualTo(testResourceIrn.toBase64());
  }

  @Test
  void updateResource_happyPath() {
    // given
    UpdateResourceRequestDto updateDto = new UpdateResourceRequestDto(new HashSet<>());
    stubFor(
        patch(urlEqualTo(RESOURCE_PATH + "/" + testResourceIrn.toBase64()))
            .willReturn(aResponse().withStatus(204)));

    // when & then
    assertThatCode(() -> serverClient.updateResource(authHeader, testResourceIrn, updateDto))
        .doesNotThrowAnyException();
  }

  @Test
  void updateResource_serverError() {
    // given
    UpdateResourceRequestDto updateDto = new UpdateResourceRequestDto(new HashSet<>());
    stubFor(
        patch(urlEqualTo(RESOURCE_PATH + "/" + testResourceIrn.toBase64()))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Invalid update request\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.updateResource(authHeader, testResourceIrn, updateDto))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Invalid update request");
  }

  @Test
  void updateResource_withPoolIds() {
    // given
    Set<String> poolIds = new HashSet<>(Arrays.asList("pool1", "pool2"));
    UpdateResourceRequestDto updateDto = new UpdateResourceRequestDto(poolIds);
    stubFor(
        patch(urlEqualTo(RESOURCE_PATH + "/" + testResourceIrn.toBase64()))
            .withRequestBody(containing("\"poolIDs\":["))
            .willReturn(aResponse().withStatus(204)));

    // when & then
    assertThatCode(() -> serverClient.updateResource(authHeader, testResourceIrn, updateDto))
        .doesNotThrowAnyException();
  }

  @Test
  void deleteResource_happyPath() {
    // given
    stubFor(
        delete(urlEqualTo(RESOURCE_PATH + "/" + testResourceIrn.toBase64()))
            .willReturn(aResponse().withStatus(204)));

    // when & then
    assertThatCode(() -> serverClient.deleteResource(authHeader, testResourceIrn))
        .doesNotThrowAnyException();
  }

  @Test
  void deleteResource_notFound() {
    // given
    stubFor(
        delete(urlEqualTo(RESOURCE_PATH + "/" + testResourceIrn.toBase64()))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Resource not found\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.deleteResource(authHeader, testResourceIrn))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Resource not found");
  }

  @Test
  void deleteResources_happyPath() {
    // given
    DeleteResourcesRequestDto requestDto =
        new DeleteResourcesRequestDto(Collections.singletonList(testResourceIrn));
    stubFor(post(urlEqualTo(RESOURCE_PATH + "/delete")).willReturn(aResponse().withStatus(204)));

    // when & then
    assertThatCode(() -> serverClient.deleteResources(authHeader, requestDto))
        .doesNotThrowAnyException();
  }

  @Test
  void deleteResources_serverError() {
    // given
    DeleteResourcesRequestDto requestDto =
        new DeleteResourcesRequestDto(Collections.singletonList(testResourceIrn));
    stubFor(
        post(urlEqualTo(RESOURCE_PATH + "/delete"))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Invalid delete request\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.deleteResources(authHeader, requestDto))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Invalid delete request");
  }

  @Test
  void createResourceType_happyPath() {
    // given
    CreateResourceTypeRequestDto requestDto =
        new CreateResourceTypeRequestDto(
            "document", "Document resource type", "doc", new HashSet<>());
    stubFor(
        post(urlEqualTo(String.format(RESOURCE_TYPE_PATH_TEMPLATE, testApplicationIrn.toBase64())))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

    // when & then
    assertThatCode(
            () -> serverClient.createResourceType(authHeader, testApplicationIrn, requestDto))
        .doesNotThrowAnyException();
  }

  @Test
  void createResourceType_serverError() {
    // given
    CreateResourceTypeRequestDto requestDto =
        new CreateResourceTypeRequestDto(
            "document", "Document resource type", "doc", new HashSet<>());
    stubFor(
        post(urlEqualTo(String.format(RESOURCE_TYPE_PATH_TEMPLATE, testApplicationIrn.toBase64())))
            .willReturn(
                aResponse()
                    .withStatus(409)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Resource type already exists\"}")));

    // when & then
    assertThatThrownBy(
            () -> serverClient.createResourceType(authHeader, testApplicationIrn, requestDto))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Resource type already exists");
  }

  @Test
  void createResourceType_withOperations() {
    // given
    Set<String> operations = new HashSet<>(Arrays.asList("read", "write", "delete"));
    CreateResourceTypeRequestDto requestDto =
        new CreateResourceTypeRequestDto("document", "Document resource type", "doc", operations);
    stubFor(
        post(urlEqualTo(String.format(RESOURCE_TYPE_PATH_TEMPLATE, testApplicationIrn.toBase64())))
            .withRequestBody(containing("\"operations\":["))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

    // when & then
    assertThatCode(
            () -> serverClient.createResourceType(authHeader, testApplicationIrn, requestDto))
        .doesNotThrowAnyException();
  }

  @Test
  void getResourceTypes_happyPath() {
    // given
    String responseBody =
        """
        {
          "data": [
            {
              "id": "type1",
              "type": "document",
              "description": "Document type",
              "actionPrefix": "doc",
              "operations": ["read", "write"]
            }
          ]
        }
        """;

    stubFor(
        get(urlPathEqualTo(String.format(RESOURCE_TYPE_PATH_TEMPLATE, testApplicationIrn.toBase64())))
            .withQueryParam("pageSize", equalTo("100000"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));

    // when
    List<ResourceTypeDto> resourceTypes =
        serverClient.getResourceTypes(authHeader, testApplicationIrn);

    // then
    assertThat(resourceTypes).hasSize(1);
    assertThat(resourceTypes.getFirst().getId()).isEqualTo("type1");
    assertThat(resourceTypes.getFirst().getType()).isEqualTo("document");
    assertThat(resourceTypes.getFirst().getDescription()).isEqualTo("Document type");
    assertThat(resourceTypes.getFirst().getActionPrefix()).isEqualTo("doc");
    assertThat(resourceTypes.getFirst().getOperations()).containsExactly("read", "write");
  }

  @Test
  void getResourceTypes_serverError() {
    // given
    stubFor(
        get(urlPathEqualTo(String.format(RESOURCE_TYPE_PATH_TEMPLATE, testApplicationIrn.toBase64())))
            .withQueryParam("pageSize", equalTo("100000"))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Application not found\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.getResourceTypes(authHeader, testApplicationIrn))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Application not found");
  }

  @Test
  void getPrincipalApiKey_happyPath() {
    // given
    String expectedApiKey = "test-api-key";
    stubFor(
        get(urlPathEqualTo(String.format(API_KEY_PATH_TEMPLATE, testPrincipalIrn.toBase64())))
            .withQueryParam("state", equalTo("active"))
            .withQueryParam("pageSize", equalTo("1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": [{\"apiKey\": \"" + expectedApiKey + "\"}]}")));

    // when
    Optional<String> apiKey = serverClient.getPrincipalApiKey(authHeader, testPrincipalIrn);

    // then
    assertThat(apiKey).isPresent();
    assertThat(apiKey).contains(expectedApiKey);
  }

  @Test
  void getPrincipalApiKey_notFound() {
    // given
    stubFor(
        get(urlPathEqualTo(String.format(API_KEY_PATH_TEMPLATE, testPrincipalIrn.toBase64())))
            .withQueryParam("state", equalTo("active"))
            .withQueryParam("pageSize", equalTo("1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": []}")));

    // when
    Optional<String> apiKey = serverClient.getPrincipalApiKey(authHeader, testPrincipalIrn);

    // then
    assertThat(apiKey).isEmpty();
  }

  @Test
  void getPrincipalApiKey_serverError() {
    // given
    stubFor(
        get(urlPathEqualTo(String.format(API_KEY_PATH_TEMPLATE, testPrincipalIrn.toBase64())))
            .withQueryParam("state", equalTo("active"))
            .withQueryParam("pageSize", equalTo("1"))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Access denied\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.getPrincipalApiKey(authHeader, testPrincipalIrn))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Access denied");
  }

  @Test
  void createPrincipalApiKey_happyPath() {
    // given
    String expectedApiKeyId = "new-api-key-id";
    stubFor(
        post(urlEqualTo(String.format(API_KEY_PATH_TEMPLATE, testPrincipalIrn.toBase64())))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader(
                        "Location",
                        "/api/v1/principals/"
                            + testPrincipalIrn.toBase64()
                            + "/api-keys/"
                            + expectedApiKeyId)));

    // when
    String apiKeyId = serverClient.createPrincipalApiKey(authHeader, testPrincipalIrn);

    // then
    assertThat(apiKeyId).isEqualTo(expectedApiKeyId);
  }

  @Test
  void createPrincipalApiKey_serverError() {
    // given
    stubFor(
        post(urlEqualTo(String.format(API_KEY_PATH_TEMPLATE, testPrincipalIrn.toBase64())))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Invalid request\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.createPrincipalApiKey(authHeader, testPrincipalIrn))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Invalid request");
  }

  @Test
  void getPools_happyPath() {
    // given
    String responseBody =
        """
        {
          "data": [
            {
              "id": "aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjpwb29sL3Byb2QvYWRtaW4=",
              "irn": "irn:rc73dbh7q0:iamcore:4atcicnisg::pool/prod/admin",
              "name": "Test Pool",
              "resources": [
                "aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjp1c2VyL3RvbQ=="
              ]
            }
          ]
        }
        """;

    IRN resourceIrn = IRN.from("irn:rc73dbh7q0:iamcore:4atcicnisg::user/tom");
    IRN poolIrn = IRN.from("irn:rc73dbh7q0:iamcore:4atcicnisg::pool/prod/admin");
    PoolsQueryFilter filter = new PoolsQueryFilter(poolIrn, "", resourceIrn);

    stubFor(
        get(urlPathEqualTo(POOLS_PATH))
            .withQueryParam("pageSize", equalTo("100000"))
            .withQueryParam("resourceIRN", equalTo(resourceIrn.toBase64()))
            .withQueryParam("irn", equalTo(poolIrn.toBase64()))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));

    // when
    List<PoolResponse> pools = serverClient.getPools(authHeader, filter);

    // then
    assertThat(pools).hasSize(1);
    assertThat(pools.getFirst().name()).isEqualTo("Test Pool");
    assertThat(pools.getFirst().id())
        .isEqualTo("aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjpwb29sL3Byb2QvYWRtaW4=");
    assertThat(pools.getFirst().irn())
        .hasToString("irn:rc73dbh7q0:iamcore:4atcicnisg::pool/prod/admin");
    assertThat(pools.getFirst().resources())
        .containsExactly("aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjp1c2VyL3RvbQ==");
  }

  @Test
  void getPools_emptyResponse() {
    // given
    PoolsQueryFilter filter = new PoolsQueryFilter(null, "", null);
    stubFor(
        get(urlPathEqualTo(POOLS_PATH))
            .withQueryParam("pageSize", equalTo("100000"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"data\": []}")));

    // when
    List<PoolResponse> pools = serverClient.getPools(authHeader, filter);

    // then
    assertThat(pools).isEmpty();
  }

  @Test
  void getPools_serverError() {
    // given
    PoolsQueryFilter filter = new PoolsQueryFilter(null, "test", null);
    stubFor(
        get(urlPathEqualTo(POOLS_PATH))
            .withQueryParam("name", equalTo("test"))
            .withQueryParam("pageSize", equalTo("100000"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Internal server error\"}")));

    // when & then
    assertThatThrownBy(() -> serverClient.getPools(authHeader, filter))
        .isInstanceOf(IamcoreServerException.class)
        .hasMessageContaining("Internal server error");
  }
}
