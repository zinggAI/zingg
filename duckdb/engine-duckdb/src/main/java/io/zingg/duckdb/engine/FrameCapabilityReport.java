package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.Frame;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

/** Emits a machine-readable inventory of the public Frame API and implementation coverage. */
public final class FrameCapabilityReport {
  private FrameCapabilityReport() {}

  public static void main(String[] args) {
    var implementation = DuckFrame.class;
    var entries = Arrays.stream(Frame.class.getMethods())
        .filter(m -> m.getDeclaringClass() == Frame.class)
        .sorted(Comparator.comparing(Method::toGenericString))
        .map(method -> {
          boolean implemented;
          try {
            var candidate = implementation.getMethod(method.getName(), method.getParameterTypes());
            implemented = Modifier.isPublic(candidate.getModifiers());
          } catch (NoSuchMethodException e) {
            implemented = false;
          }
          return "{\"method\":" + quote(method.toGenericString()) + ",\"implemented\":" + implemented + "}";
        })
        .toList();
    long implemented = entries.stream().filter(e -> e.endsWith("true}")).count();
    System.out.println("{\"interface\":\"" + Frame.class.getName() + "\",\"implemented\":" + implemented
        + ",\"total\":" + entries.size() + ",\"methods\":[" + String.join(",", entries) + "]}");
  }

  private static String quote(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
