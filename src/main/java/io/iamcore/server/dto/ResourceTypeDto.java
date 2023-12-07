package io.iamcore.server.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ResourceTypeDto {
  private String id;
  private String irn;
  private String type;
  private String description;
  private String actionPrefix;
  private LocalDateTime created;
  private LocalDateTime updated;
  private Set<String> operations;

  public ResourceTypeDto() {
  }

  public ResourceTypeDto(String id, String irn, String type, String description, String actionPrefix,
      LocalDateTime created, LocalDateTime updated, Set<String> operations) {
    this.id = id;
    this.irn = irn;
    this.type = type;
    this.description = description;
    this.actionPrefix = actionPrefix;
    this.created = created;
    this.updated = updated;
    this.operations = operations;
  }

  public String getId() {
    return id;
  }

  public String getIrn() {
    return irn;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public String getActionPrefix() {
    return actionPrefix;
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public LocalDateTime getUpdated() {
    return updated;
  }

  public Set<String> getOperations() {
    return operations == null ? new HashSet<>() : operations;
  }
}