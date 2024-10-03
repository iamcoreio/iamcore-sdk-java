package io.iamcore.server;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.StringUtils;
import io.iamcore.exception.IamcoreServerException;
import io.iamcore.exception.SdkException;
import io.iamcore.server.dto.CreateResourceRequestDto;
import io.iamcore.server.dto.CreateResourceTypeRequestDto;
import io.iamcore.server.dto.Database;
import io.iamcore.server.dto.DeleteResourcesRequestDto;
import io.iamcore.server.dto.ResourceTypeDto;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerClientImpl implements ServerClient {

  private static final String USER_IRN_PATH = "/api/v1/users/me/irn";
  private static final String EVALUATE_PATH = "/api/v1/evaluate";
  private static final String RESOURCES_EVALUATE_PATH = "/api/v1/resources/evaluate";
  private static final String EVALUATE_RESOURCES_PATH = "/api/v1/evaluate/resources";
  private static final String AUTHORIZATION_QUERY_FILTER_PATH = "/api/v1/evaluate/database-query-filter";
  private static final String RESOURCE_PATH = "/api/v1/resources";
  private static final String APPLICATION_PATH = "/api/v1/applications";
  private static final String RESOURCE_TYPE_PATH = APPLICATION_PATH + "/%s/resource-types";
  private static final String API_KEY_PATH = "/api/v1/principals/%s/api-keys";
  private static final int PAGE_SIZE = 100000;

  private final URI serverUrl;
  private final ObjectMapper objectMapper;

  public ServerClientImpl(URI serverUrl, ObjectMapper objectMapper) {
    this.serverUrl = serverUrl;
    this.objectMapper = objectMapper;
  }

  @Override
  public IRN getPrincipalIrn(HttpHeader header) {
    try {
      HttpURLConnection connection = sendRequest(USER_IRN_PATH, "GET", header, null);
      int responseCode = connection.getResponseCode();

      if (responseCode == HTTP_OK) {
        JSONObject response = convertInputStream(connection);
        return IRN.from(response.getString("data"));
      }

      JSONObject response = convertErrorStream(connection);
      throw new IamcoreServerException(response.getString("message"), responseCode);
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public void authorizedOnIrns(HttpHeader authorizationHeader, String action, List<IRN> irns) {
    authorize(EVALUATE_PATH, authorizationHeader, action, irns);
  }

  @Override
  public void authorizedOnResources(HttpHeader authorizationHeader, String action,
      List<IRN> resources) {
    authorize(RESOURCES_EVALUATE_PATH, authorizationHeader, action, resources);
  }

  private void authorize(String url, HttpHeader authorizationHeader, String action,
      List<IRN> resources) {
    List<String> resourceIrns = resources.stream()
        .map(IRN::toString)
        .collect(Collectors.toList());

    JSONObject requestBody = new JSONObject();
    requestBody.put("action", action);
    requestBody.put("resources", resourceIrns);

    try {
      HttpURLConnection connection = sendRequest(url, "POST", authorizationHeader, requestBody);
      int responseCode = connection.getResponseCode();

      if (responseCode != HTTP_OK) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public List<IRN> authorizedOnResourceType(HttpHeader header, String action, String application,
      String tenantId, String resourceType) {
    JSONObject requestBody = new JSONObject();
    requestBody.put("action", action);
    requestBody.put("resourceType", resourceType);
    requestBody.put("application", application);

    if (!StringUtils.isEmpty(tenantId)) {
      requestBody.put("tenantID", tenantId);
    }

    String path = String.format("%s?pageSize=%s", EVALUATE_RESOURCES_PATH, PAGE_SIZE);

    try {
      HttpURLConnection connection = sendRequest(path, "POST", header, requestBody);
      int responseCode = connection.getResponseCode();

      if (responseCode == HTTP_OK) {
        JSONObject response = convertInputStream(connection);

        return Optional.ofNullable(response.getJSONArray("data"))
            .map(resources -> {
              List<IRN> resourceIrns = new ArrayList<>();
              for (int i = 0; i < resources.length(); i++) {
                resourceIrns.add(IRN.from(resources.getString(i)));
              }

              return resourceIrns;
            }).orElseGet(ArrayList::new);
      }

      JSONObject response = convertErrorStream(connection);
      throw new IamcoreServerException(response.getString("message"), responseCode);
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public String authorizationDbQueryFilter(HttpHeader authorizationHeader, String action,
      Database database) {
    JSONObject requestBody = new JSONObject();
    requestBody.put("action", action);
    requestBody.put("database", database.getValue());

    try {
      HttpURLConnection connection = sendRequest(AUTHORIZATION_QUERY_FILTER_PATH, "POST",
          authorizationHeader, requestBody);
      int responseCode = connection.getResponseCode();

      if (responseCode == HTTP_OK) {
        JSONObject response = convertInputStream(connection);
        return response.getString("data");
      }

      JSONObject response = convertErrorStream(connection);
      throw new IamcoreServerException(response.getString("message"), responseCode);
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public IRN createResource(HttpHeader authorizationHeader, CreateResourceRequestDto requestDto) {
    try {
      HttpURLConnection connection = sendRequest(RESOURCE_PATH, "POST", authorizationHeader,
          requestDto.toJson());
      int responseCode = connection.getResponseCode();

      if (responseCode == HTTP_CREATED) {
        JSONObject response = convertInputStream(connection);
        return IRN.from(response.getJSONObject("data").getString("irn"));
      }

      JSONObject response = convertErrorStream(connection);
      throw new IamcoreServerException(response.getString("message"), responseCode);
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public void deleteResource(HttpHeader header, IRN resourceIrn) {
    try {
      HttpURLConnection connection = sendRequest(RESOURCE_PATH + "/" + resourceIrn.toBase64(),
          "DELETE", header, null);
      int responseCode = connection.getResponseCode();

      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public void deleteResources(HttpHeader header, DeleteResourcesRequestDto requestDto) {
    try {
      HttpURLConnection connection = sendRequest(RESOURCE_PATH + "/delete", "POST", header,
          requestDto.toJson());
      int responseCode = connection.getResponseCode();

      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public void createResourceType(HttpHeader header, IRN application,
      CreateResourceTypeRequestDto requestDto) {
    String path = String.format(RESOURCE_TYPE_PATH, application.toBase64());

    try {
      HttpURLConnection connection = sendRequest(path, "POST", header, requestDto.toJson());
      int responseCode = connection.getResponseCode();

      if (responseCode != HTTP_CREATED) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public List<ResourceTypeDto> getResourceTypes(HttpHeader header, IRN applicationIrn) {
    String path = String.format(RESOURCE_TYPE_PATH + "?pageSize=%s", applicationIrn.toBase64(),
        PAGE_SIZE);

    try {
      HttpURLConnection connection = sendRequest(path, "GET", header, null);
      int responseCode = connection.getResponseCode();

      if (responseCode != HTTP_OK) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }

      JSONObject response = convertInputStream(connection);

      return Optional.ofNullable(response.get("data"))
          .map(resourceTypes -> {
            try {
              return objectMapper.readValue(resourceTypes.toString(),
                  new TypeReference<List<ResourceTypeDto>>() {
                  });
            } catch (JsonProcessingException ex) {
              throw new SdkException(ex.getMessage());
            }
          })
          .orElse(Collections.emptyList());
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public Optional<String> getPrincipalApiKey(HttpHeader header, IRN principalIrn) {
    String path = String.format(API_KEY_PATH + "?state=active&pageSize=1", principalIrn.toBase64());

    try {
      HttpURLConnection connection = sendRequest(path, "GET", header, null);
      int responseCode = connection.getResponseCode();

      if (connection.getResponseCode() != HTTP_OK) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }

      JSONObject response = convertInputStream(connection);
      JSONArray apiKey = response.getJSONArray("data");

      if (apiKey.length() != 1) {
        return Optional.empty();
      }

      return Optional.ofNullable(apiKey.getJSONObject(0).getString("apiKey"));
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public String createPrincipalApiKey(HttpHeader header, IRN principalIrn) {
    try {
      String url = String.format(API_KEY_PATH, principalIrn.toBase64());
      HttpURLConnection connection = sendRequest(url, "POST", header, null);
      int responseCode = connection.getResponseCode();

      if (connection.getResponseCode() != HTTP_CREATED) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }

      String location = connection.getHeaderField("Location");
      return location.substring(location.lastIndexOf('/') + 1);
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  private HttpURLConnection sendRequest(String path, String method, HttpHeader header,
      JSONObject body) throws IOException {
    URL requestUrl = getUrl(path);
    HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
    connection.setRequestMethod(method);

    if (header != null) {
      connection.setRequestProperty(header.getName(), header.getValue());
    }

    if (Arrays.asList("POST", "PUT", "OPTIONS").contains(method)) {
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "application/json");
    }

    if (body != null) {
      try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
        outputStream.writeBytes(body.toString());
        outputStream.flush();
      }
    }

    return connection;
  }

  private URL getUrl(String path) throws MalformedURLException {
    return this.serverUrl.resolve(path).toURL();
  }

  private static JSONObject convertInputStreamToJson(InputStream inputStream) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder stringBuilder = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }

      reader.close();
      inputStream.close();

      return new JSONObject(stringBuilder.toString());
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  private static JSONObject convertInputStream(HttpURLConnection connection) throws IOException {
    try (InputStream inputStream = connection.getInputStream()) {
      return convertInputStreamToJson(inputStream);
    }
  }

  private static JSONObject convertErrorStream(HttpURLConnection connection) throws IOException {
    try (InputStream inputStream = connection.getErrorStream()) {
      return convertInputStreamToJson(inputStream);
    }
  }
}
