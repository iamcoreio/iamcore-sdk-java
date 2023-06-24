package io.iamcore;

import io.iamcore.exception.SdkException;

import java.net.URI;

public class ClientProperties {

  private static final String API_KEY_ENV_KEY = "IAMCORE_API_KEY";
  private static final String IAMCORE_URL_ENV_KEY = "IAMCORE_URL";
  private static final String DEFAULT_IAMCORE_URL = "https://cloud.iamcore.io";

  private String apiKey;
  private URI serverURL;
  private final boolean disabled;

  public ClientProperties(String apiKey, String serverURL, boolean disabled) {
    if (!disabled) {
      this.apiKey = getApiKey(apiKey);
      this.serverURL = getServerURL(serverURL);
    }

    this.disabled = disabled;
  }

  private static URI getServerURL(String serverURL) {
    if (StringUtils.isEmpty(serverURL)) {
      serverURL = System.getenv(IAMCORE_URL_ENV_KEY);
    }

    if (StringUtils.isEmpty(serverURL)) {
      serverURL = DEFAULT_IAMCORE_URL;
    }

    return URI.create(serverURL);
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

  public URI getServerURL() {
    return serverURL;
  }

  public boolean isDisabled() {
    return disabled;
  }
}
