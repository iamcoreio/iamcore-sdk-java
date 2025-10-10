package io.iamcore.server.dto;

public enum Database {
  POSTGRES("postgres"),
  MONGO("mongo"),
  JPA("jpa");

  private final String value;

  Database(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
