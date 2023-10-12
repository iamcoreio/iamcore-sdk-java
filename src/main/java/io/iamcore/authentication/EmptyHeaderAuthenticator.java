package io.iamcore.authentication;

import io.iamcore.IRN;
import io.iamcore.authentication.context.SecurityContext;
import io.iamcore.server.ServerClient;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public class EmptyHeaderAuthenticator implements Authenticator {

  private final ServerClient serverClient;

  public EmptyHeaderAuthenticator(ServerClient serverClient) {
    this.serverClient = serverClient;
  }
  
  @Override
  public Optional<SecurityContext> authenticate(HttpServletRequest request) {
    IRN principalIRN = serverClient.getPrincipalIRN(null);
    return Optional.of(new SecurityContext(null, principalIRN));
  }
}
