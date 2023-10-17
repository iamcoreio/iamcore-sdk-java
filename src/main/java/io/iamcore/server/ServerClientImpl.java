package io.iamcore.server;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.StringUtils;
import io.iamcore.exception.IamcoreServerException;
import io.iamcore.exception.SdkException;
import io.iamcore.server.dto.CreateResourceRequestDto;

import org.json.JSONObject;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class ServerClientImpl implements ServerClient {

  private static final String USER_IRN_PATH = "/api/v1/users/me/irn";
  private static final String EVALUATE_ON_RESOURCES_PATH = "/api/v1/evaluate";
  private static final String EVALUATE_ON_RESOURCE_TYPE_PATH = "/api/v1/evaluate/resources";
  private static final String RESOURCE_PATH = "/api/v1/resources";
  private static final int PAGE_SIZE = 100000;

  private final URI serverURL;

  public ServerClientImpl(URI serverURL) {
    this.serverURL = serverURL;
  }

  @Override
  public IRN getPrincipalIRN(HttpHeader header) {
    try {
      HttpURLConnection connection = sendRequest(USER_IRN_PATH, "GET", header, null);
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
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
  public void authorizeOnResources(HttpHeader principalAuthorizationHeader, String action, List<IRN> resources) {
    List<String> resourceIRNs = resources.stream()
        .map(IRN::toString)
        .collect(Collectors.toList());

    JSONObject requestBody = new JSONObject();
    requestBody.put("action", action);
    requestBody.put("resources", resourceIRNs);

    try {
      HttpURLConnection connection = sendRequest(EVALUATE_ON_RESOURCES_PATH, "POST", principalAuthorizationHeader, requestBody);
      int responseCode = connection.getResponseCode();

      if (responseCode != HttpURLConnection.HTTP_OK) {
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

    String url = String.format("%s?pageSize=%s", EVALUATE_ON_RESOURCE_TYPE_PATH, PAGE_SIZE);

    try {
      HttpURLConnection connection = sendRequest(url, "POST", header, requestBody);
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        JSONObject response = convertInputStream(connection);

        return Optional.ofNullable(response.getJSONArray("data"))
            .map(resources -> {
              List<IRN> resourceIRNs = new ArrayList<>();
              for (int i = 0; i < resources.length(); i++) {
                resourceIRNs.add(IRN.from(resources.getString(i)));
              }

              return resourceIRNs;
            }).orElseGet(ArrayList::new);
      }

      JSONObject response = convertErrorStream(connection);
      throw new IamcoreServerException(response.getString("message"), responseCode);
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public void createResource(HttpHeader header, CreateResourceRequestDto requestDto) {
    try {
      HttpURLConnection connection = sendRequest(RESOURCE_PATH, "POST", header, requestDto.toJson());
      int responseCode = connection.getResponseCode();

      if (responseCode != HttpURLConnection.HTTP_CREATED) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  @Override
  public void deleteResource(HttpHeader header, IRN resourceIrn) {
    try {
      HttpURLConnection connection = sendRequest(RESOURCE_PATH + "/" + resourceIrn.toBase64(), "DELETE", header, null);
      int responseCode = connection.getResponseCode();

      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        JSONObject response = convertErrorStream(connection);
        throw new IamcoreServerException(response.getString("message"), responseCode);
      }
    } catch (IOException ex) {
      throw new SdkException(ex.getMessage());
    }
  }

  private HttpURLConnection sendRequest(String path, String method, HttpHeader header, JSONObject body) throws IOException {
    URL requestUrl = getURL(path);
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

  private URL getURL(String path) throws MalformedURLException {
    return this.serverURL.resolve(path).toURL();
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
