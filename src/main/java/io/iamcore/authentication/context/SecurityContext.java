package io.iamcore.authentication.context;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.exception.SdkException;

public class SecurityContext {

  private final HttpHeader authorizationHeader;
  private final IRN principalIRN;

  public SecurityContext(HttpHeader authorizationHeader, IRN principalIRN) {
    if (authorizationHeader == null || principalIRN == null) {
      throw new SdkException("SecurityContext initialization failed: principal IRN or authorization header is null");
    }

    this.authorizationHeader = authorizationHeader;
    this.principalIRN = principalIRN;
  }

  public HttpHeader getAuthorizationHeader() {
    return authorizationHeader;
  }

  public IRN getPrincipalIRN() {
    return principalIRN;
  }
}
