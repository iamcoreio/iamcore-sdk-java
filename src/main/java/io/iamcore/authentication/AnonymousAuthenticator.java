package io.iamcore.authentication;

import io.iamcore.IRN;
import io.iamcore.authentication.context.SecurityContext;
import io.iamcore.server.ServerClient;

public class AnonymousAuthenticator {

  private final ServerClient serverClient;

  public AnonymousAuthenticator(ServerClient serverClient) {
    this.serverClient = serverClient;
  }
  
  public SecurityContext authenticate() {
    IRN principalIRN = serverClient.getPrincipalIRN(null);
    return new SecurityContext(null, principalIRN);
  }
}
