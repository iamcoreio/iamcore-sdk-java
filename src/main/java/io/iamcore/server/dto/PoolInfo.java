package io.iamcore.server.dto;

import java.util.List;

public class PoolInfo {

  private final String id;
  private final String irn;
  private final String name;
  private final List<String> resources;

  public PoolInfo(String id, String irn, String name, List<String> resources) {
    this.id = id;
    this.irn = irn;
    this.name = name;
    this.resources = resources;
  }

  public String getId() {
    return id;
  }

  public String getIrn() {
    return irn;
  }

  public String getName() {
    return name;
  }

  public List<String> getResources() {
    return resources;
  }
}
