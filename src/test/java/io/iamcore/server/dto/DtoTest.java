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
          "apiKey": "test-api-key",
          "state": "active"
        }
        """;

    ApiKeyResponse dto = objectMapper.readValue(json, ApiKeyResponse.class);
    assertThat(dto.apiKey()).isEqualTo("test-api-key");
    assertThat(dto.state()).isEqualTo("active");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).isNotNull();
  }

  @Test
  void authorizationDbQueryFilterRequest() throws Exception {
    AuthorizationDbQueryFilterRequest dto = new AuthorizationDbQueryFilterRequest("read", "mongo");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).contains("\"action\":\"read\"");
    assertThat(marshalledJson).contains("\"database\":\"mongo\"");
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
    assertThat(marshalledJson).contains("\"application\":\"test-app\"");
    assertThat(marshalledJson).contains("\"tenantID\":\"tenant1\"");
    assertThat(marshalledJson).contains("\"resourceType\":\"document\"");
    assertThat(marshalledJson).contains("\"path\":\"/\"");
    assertThat(marshalledJson).contains("\"name\":\"test-resource\"");
    assertThat(marshalledJson).contains("\"enabled\":true");
    assertThat(marshalledJson).contains("\"poolIDs\":[\"pool1\"]");
  }

  @Test
  void createResourceTypeRequestDto() throws Exception {
    CreateResourceTypeRequestDto dto =
        new CreateResourceTypeRequestDto(
            "document", "Document resource type", "doc", Collections.singleton("read"));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).contains("\"type\":\"document\"");
    assertThat(marshalledJson).contains("\"description\":\"Document resource type\"");
    assertThat(marshalledJson).contains("\"actionPrefix\":\"doc\"");
    assertThat(marshalledJson).contains("\"operations\":[\"read\"]");
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
    assertThat(marshalledJson).contains("\"action\":\"read\"");
    assertThat(marshalledJson).contains("\"application\":\"test-app\"");
    assertThat(marshalledJson).contains("\"resourceType\":\"document\"");
    assertThat(marshalledJson).contains("\"tenantID\":\"tenant1\"");
  }

  @Test
  void evaluateResourcesRequest() throws Exception {
    EvaluateResourcesRequest dto =
        new EvaluateResourcesRequest("read", Collections.singletonList("resource1"));

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).contains("\"action\":\"read\"");
    assertThat(marshalledJson).contains("\"resources\":[\"resource1\"]");
  }

  @Test
  void poolResponse() throws Exception {
    String json =
        """
        {
          "id": "pool1",
          "irn": "irn:iamcore:iamcore::/pool/pool1:pool/pool1",
          "name": "Test Pool",
          "resources": ["resource1"]
        }
        """;

    PoolResponse dto = objectMapper.readValue(json, PoolResponse.class);
    assertThat(dto.id()).isEqualTo("pool1");
    assertThat(dto.irn()).hasToString("irn:iamcore:iamcore::/pool/pool1:pool/pool1");
    assertThat(dto.name()).isEqualTo("Test Pool");
    assertThat(dto.resources()).containsExactly("resource1");

    String marshalledJson = objectMapper.writeValueAsString(dto);
    assertThat(marshalledJson).isNotNull();
  }

  @Test
  void resourceTypeDto() throws Exception {
    String json =
        """
        {
          "id": "type1",
          "irn": "irn:iamcore:application::resource-type/document",
          "type": "document",
          "description": "Document type",
          "actionPrefix": "doc",
          "created": "2023-01-01T12:00:00Z",
          "updated": "2023-01-01T12:00:00Z",
          "operations": ["read", "write"]
        }
        """;

    objectMapper.findAndRegisterModules();
    ResourceTypeDto dto = objectMapper.readValue(json, ResourceTypeDto.class);
    assertThat(dto.getId()).isEqualTo("type1");
    assertThat(dto.getIrn()).isEqualTo("irn:iamcore:application::resource-type/document");
    assertThat(dto.getType()).isEqualTo("document");
    assertThat(dto.getDescription()).isEqualTo("Document type");
    assertThat(dto.getActionPrefix()).isEqualTo("doc");
    assertThat(dto.getOperations()).containsExactly("read", "write");

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
