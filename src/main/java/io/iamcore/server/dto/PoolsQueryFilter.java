package io.iamcore.server.dto;

public class PoolsQueryFilter {

  private final String irn;
  private final String name;
  private final String resourceIrn;

  public PoolsQueryFilter(String irn, String name, String resourceIrn) {
    this.irn = irn;
    this.name = name;
    this.resourceIrn = resourceIrn;
  }

  public String getIrn() {
    return irn;
  }

  public String getName() {
    return name;
  }

  public String getResourceIrn() {
    return resourceIrn;
  }
}
