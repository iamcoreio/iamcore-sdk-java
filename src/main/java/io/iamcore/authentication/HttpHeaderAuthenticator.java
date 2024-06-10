package io.iamcore.authentication;

import io.iamcore.HttpHeader;
import io.iamcore.IRN;
import io.iamcore.StringUtils;
import io.iamcore.authentication.context.SecurityContext;
import io.iamcore.server.ServerClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public class HttpHeaderAuthenticator implements Authenticator {

  private final ServerClient serverClient;
  private final String authorizationHeaderName;

  public HttpHeaderAuthenticator(ServerClient serverClient, String authorizationHeaderName) {
    this.serverClient = serverClient;
    this.authorizationHeaderName = authorizationHeaderName;
  }
  
  @Override
  public Optional<SecurityContext> authenticate(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(authorizationHeaderName);
    if (StringUtils.isEmpty(authorizationHeader)) {
      return Optional.empty();
    }

    HttpHeader httpHeader = new HttpHeader(authorizationHeaderName, authorizationHeader);

    IRN principalIrn = serverClient.getPrincipalIrn(httpHeader);
    return Optional.of(new SecurityContext(httpHeader, principalIrn));
  }
}
