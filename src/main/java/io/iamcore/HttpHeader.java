package io.iamcore;

import io.iamcore.exception.SdkException;

public class HttpHeader {

  private final String name;
  private final String value;

  public HttpHeader(String name, String value) {
    this.name = validateNotEmpty(name);
    this.value = validateNotEmpty(value);
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  private static String validateNotEmpty(String str) {
    if (StringUtils.isEmpty(str)) {
      throw new SdkException("Trying initializing HTTP header with empty name or value");
    }

    return str;
  }
}
