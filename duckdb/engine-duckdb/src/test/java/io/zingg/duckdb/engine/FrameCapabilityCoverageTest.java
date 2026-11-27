package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.Frame;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Executable API coverage guard: every Frame operation must have a DuckFrame implementation. */
class FrameCapabilityCoverageTest {
  @Test
  void everyFrameMethodIsImplementedByDuckFrame() {
    var missing = Arrays.stream(Frame.class.getMethods())
        .filter(m -> m.getDeclaringClass() == Frame.class)
        .filter(m -> {
          try {
            Method implementation = DuckFrame.class.getMethod(m.getName(), m.getParameterTypes());
            return !Modifier.isPublic(implementation.getModifiers());
          } catch (NoSuchMethodException e) {
            return true;
          }
        })
        .map(Method::toGenericString)
        .toList();
    assertTrue(missing.isEmpty(), () -> "Frame methods without DuckFrame implementation: " + missing);
  }
}
