package io.iamcore;

import java.util.List;

@FunctionalInterface
public interface Authorizer {

  /**
   * Returns authorized resource IRNs.
   */
  List<IRN> authorize(List<IRN> resourceIrns);
}
