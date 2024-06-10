package io.iamcore;

import io.iamcore.exception.SdkException;
import java.net.URI;

public class ClientProperties {

  private static final String API_KEY_ENV_KEY = "IAMCORE_API_KEY";
  private static final String IAMCORE_URL_ENV_KEY = "IAMCORE_URL";
  private static final String DEFAULT_IAMCORE_URL = "https://cloud.iamcore.io";

  private String apiKey;
  private URI serverUrl;
  private final boolean disabled;

  public ClientProperties(String apiKey, String serverUrl, boolean disabled) {
    if (!disabled) {
      this.apiKey = getApiKey(apiKey);
      this.serverUrl = getServerUrl(serverUrl);
    }

    this.disabled = disabled;
  }

  private static URI getServerUrl(String serverUrl) {
    if (StringUtils.isEmpty(serverUrl)) {
      serverUrl = System.getenv(IAMCORE_URL_ENV_KEY);
    }

    if (StringUtils.isEmpty(serverUrl)) {
      serverUrl = DEFAULT_IAMCORE_URL;
    }

    return URI.create(serverUrl);
  }

  public URI getServerUrl() {
    return serverUrl;
  }

  private static String getApiKey(String apiKey) {
    if (StringUtils.isEmpty(apiKey)) {
      apiKey = System.getenv(API_KEY_ENV_KEY);
    }

    if (StringUtils.isEmpty(apiKey)) {
      throw new SdkException("API key is empty");
    }

    return apiKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  public boolean isDisabled() {
    return disabled;
  }
}
