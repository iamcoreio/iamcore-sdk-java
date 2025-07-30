package io.iamcore.server.dto;

import io.iamcore.IRN;

public record PoolsQueryFilter(IRN irn, String name, IRN resourceIrn) {}
