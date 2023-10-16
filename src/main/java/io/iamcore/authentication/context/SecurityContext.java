package io.iamcore.authentication.context;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.exception.SdkException;

import java.util.Optional;

public class SecurityContext {

  private final HttpHeader authorizationHeader;
  private final IRN principalIRN;

  public SecurityContext(HttpHeader authorizationHeader, IRN principalIRN) {
    if (principalIRN == null) {
      throw new SdkException("SecurityContext initialization failed: principal IRN is null");
    }

    this.authorizationHeader = authorizationHeader;
    this.principalIRN = principalIRN;
  }

  public Optional<HttpHeader> getAuthorizationHeader() {
    return Optional.ofNullable(authorizationHeader);
  }

  public IRN getPrincipalIRN() {
    return principalIRN;
  }
}
