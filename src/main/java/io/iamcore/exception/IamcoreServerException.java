package io.iamcore.exception;

public class IamcoreServerException extends RuntimeException {

  private final int statusCode;

  public IamcoreServerException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
