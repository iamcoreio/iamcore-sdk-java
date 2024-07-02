package io.iamcore;

@FunctionalInterface
public interface TriFunction<T, U, O> {
  void apply(T t, U u, O o);
}
