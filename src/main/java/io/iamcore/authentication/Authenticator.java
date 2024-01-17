package io.iamcore.authentication;

import io.iamcore.authentication.context.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface Authenticator {

  Optional<SecurityContext> authenticate(HttpServletRequest request);
}
