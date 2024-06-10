package io.iamcore.authentication.context;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.exception.SdkException;
import java.util.Optional;

public class SecurityContext {

  private final HttpHeader authorizationHeader;
  private final IRN principalIrn;

  public SecurityContext(HttpHeader authorizationHeader, IRN principalIrn) {
    if (principalIrn == null) {
      throw new SdkException("SecurityContext initialization failed: principal IRN is null");
    }

    this.authorizationHeader = authorizationHeader;
    this.principalIrn = principalIrn;
  }

  public Optional<HttpHeader> getAuthorizationHeader() {
    return Optional.ofNullable(authorizationHeader);
  }

  public IRN getPrincipalIrn() {
    return principalIrn;
  }
}
