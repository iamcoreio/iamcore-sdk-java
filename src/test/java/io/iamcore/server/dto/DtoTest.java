package io.iamcore.server.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iamcore.IRN;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DtoTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void apiKeyResponse() throws Exception {
    String json =
        """
          {
            "apiKey": "2W9Xv6Ae7y0nHf8Mb1JdKc5Tl4sZgIzNtEjPuSqOxDhYi3LrpQmGkFVbCh94ygh6",
            "state": "active",
            "lastUsed": "2021-10-19T17:57:31.14492667Z",
            "created": "2021-10-18T12:27:15.55267632Z",
            "updated": "2021-10-18T12:27:15.55267632Z"
          }
          """;

    ApiKeyResponse dto = objectMapper.readValue(json, ApiKeyResponse.class);
    assertThat(dto.apiKey()).isEqualTo("2W9Xv6Ae7y0nHf8Mb1JdKc5Tl4sZgIzNtEjPuSqOxDhYi3LrpQmGkFVbCh94ygh6");
    assertThat(dto.state()).isEqualTo("active");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).isNotNull();
  }

  @Test
  void authorizationDbQueryFilterRequest() throws Exception {
    AuthorizationDbQueryFilterRequest dto = new AuthorizationDbQueryFilterRequest("read", "mongo");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson)
      .contains("\"action\":\"read\"")
      .contains("\"database\":\"mongo\"");
  }

  @Test
  void createResourceRequestDto() throws Exception {
    CreateResourceRequestDto dto =
        new CreateResourceRequestDto(
            "test-app",
            "tenant1",
            "document",
            "/",
            "test-resource",
            true,
            Collections.singleton("pool1"));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson)
      .contains("\"application\":\"test-app\"")
      .contains("\"tenantID\":\"tenant1\"")
      .contains("\"resourceType\":\"document\"")
      .contains("\"path\":\"/\"")
      .contains("\"name\":\"test-resource\"")
      .contains("\"enabled\":true")
      .contains("\"poolIDs\":[\"pool1\"]");
  }

  @Test
  void createResourceTypeRequestDto() throws Exception {
    CreateResourceTypeRequestDto dto =
        new CreateResourceTypeRequestDto(
            "document", "Document resource type", "doc", Collections.singleton("read"));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson)
      .contains("\"type\":\"document\"")
      .contains("\"description\":\"Document resource type\"")
      .contains("\"actionPrefix\":\"doc\"")
      .contains("\"operations\":[\"read\"]");
  }

  @Test
  void deleteResourcesRequestDto() throws Exception {
    IRN irn = IRN.of("iamcore", "resource", "", "/pool", "document", "", "test-doc");
    DeleteResourcesRequestDto dto = new DeleteResourcesRequestDto(Collections.singletonList(irn));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).contains("\"resourceIDs\":[\"" + irn.toBase64() + "\"]");
  }

  @Test
  void evaluateResourceTypeRequest() throws Exception {
    EvaluateResourceTypeRequest dto =
        new EvaluateResourceTypeRequest("read", "test-app", "document", "tenant1");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson)
      .contains("\"action\":\"read\"")
      .contains("\"application\":\"test-app\"")
      .contains("\"resourceType\":\"document\"")
      .contains("\"tenantID\":\"tenant1\"");
  }

  @Test
  void evaluateResourcesRequest() throws Exception {
    EvaluateResourcesRequest dto =
        new EvaluateResourcesRequest("read", Collections.singletonList("resource1"));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson)
      .contains("\"action\":\"read\"")
      .contains("\"resources\":[\"resource1\"]");
  }

  @Test
  void poolResponse() throws Exception {
    String json =
        """
          {
            "id": "aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjpwb29sL3Byb2QvYWRtaW4=",
            "irn": "irn:rc73dbh7q0:iamcore:4atcicnisg::pool/prod/admin",
            "name": "admin",
            "resources": [
              "aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjp1c2VyL3RvbQ=="
            ]
          }
          """;

    PoolResponse dto = objectMapper.readValue(json, PoolResponse.class);
    assertThat(dto.id()).isEqualTo("aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjpwb29sL3Byb2QvYWRtaW4=");
    assertThat(dto.irn()).hasToString("irn:rc73dbh7q0:iamcore:4atcicnisg::pool/prod/admin");
    assertThat(dto.name()).isEqualTo("admin");
    assertThat(dto.resources()).containsExactly("aXJuOnJjNzNkYmg3cTA6aWFtY29yZTo0YXRjaWNuaXNnOjp1c2VyL3RvbQ==");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).isNotNull();
  }

  @Test
  void resourceTypeDto() throws Exception {
    String json =
        """
          {
            "id": "aXJuOnJjNzNkYmg3cTA6bXlhcHA6OjpyZXNvdXJjZS10eXBlL2RvY3VtZW50",
            "irn": "irn:rc73dbh7q0:myapp:::resource-type/document",
            "type": "document",
            "description": "Representation of the 'document' resource type",
            "actionPrefix": "document",
            "created": "2021-10-18T12:27:15.55267632Z",
            "updated": "2021-10-18T12:27:15.55267632Z",
            "operations": [
              "sign",
              "export"
            ]
          }
          """;

    objectMapper.findAndRegisterModules();
    ResourceTypeDto dto = objectMapper.readValue(json, ResourceTypeDto.class);
    assertThat(dto.getId()).isEqualTo("aXJuOnJjNzNkYmg3cTA6bXlhcHA6OjpyZXNvdXJjZS10eXBlL2RvY3VtZW50");
    assertThat(dto.getIrn()).isEqualTo("irn:rc73dbh7q0:myapp:::resource-type/document");
    assertThat(dto.getType()).isEqualTo("document");
    assertThat(dto.getDescription()).isEqualTo("Representation of the 'document' resource type");
    assertThat(dto.getActionPrefix()).isEqualTo("document");
    assertThat(dto.getOperations()).containsExactly("sign", "export");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).isNotNull();
  }

  @Test
  void updateResourceRequestDto() throws Exception {
    UpdateResourceRequestDto dto = new UpdateResourceRequestDto(Collections.singleton("pool1"));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).contains("\"poolIDs\":[\"pool1\"]");
  }
}
