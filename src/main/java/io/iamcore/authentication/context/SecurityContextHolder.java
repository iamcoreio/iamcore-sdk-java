package io.iamcore.authentication.context;

import io.iamcore.exception.SdkException;

import java.util.Optional;

/**
 * The SecurityContextHolder class provides a centralized mechanism for managing the security context in a multithreaded environment.
 */
public class SecurityContextHolder {

  private static final ThreadLocal<SecurityContext> requestContext = new ThreadLocal<>();

  /**
   * Checks whether the security context has been initialized for the current thread.
   *
   * @return true if the security context has been initialized, false otherwise.
   */
  public static boolean isSecurityContextInitialized() {
    return requestContext.get() != null;
  }

  /**
   * Initializes the security context for the current thread.
   *
   * @param context the security context to be initialized.
   * @throws SdkException if the security context is not initialized.
   */
  public static void initializeSecurityContext(SecurityContext context) {
    if (requestContext.get() != null) {
      throw new SdkException("Trying initialize already initialized security context");
    }
    requestContext.set(context);
  }

  /**
   * Retrieves the security context for the current thread.
   *
   * @return the initialized security context.
   * @throws SdkException if the security context is not initialized.
   */
  public static SecurityContext getSecurityContext() {
    return Optional.ofNullable(requestContext.get())
        .orElseThrow(() -> new SdkException("Security context is not initialized"));
  }

  /**
   * Clears the security context for the current thread.
   */
  public static void clearSecurityContext() {
    requestContext.remove();
  }
}
