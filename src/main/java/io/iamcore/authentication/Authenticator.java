package io.iamcore.authentication;

import io.iamcore.authentication.context.SecurityContext;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public interface Authenticator {

  Optional<SecurityContext> authenticate(HttpServletRequest request);
}
