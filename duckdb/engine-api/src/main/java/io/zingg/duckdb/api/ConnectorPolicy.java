package io.zingg.duckdb.api;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Explicit gate for DuckDB extensions and network-backed connectors. */
public record ConnectorPolicy(boolean offlineMode, Set<String> allowedExtensions) {
  public ConnectorPolicy {
    var configured = allowedExtensions == null ? Set.<String>of() : allowedExtensions;
    if (configured.stream().anyMatch(x -> x == null || x.isBlank() || !x.toLowerCase(Locale.ROOT).matches("[a-z0-9_]+")))
      throw new IllegalArgumentException("invalid extension name");
    allowedExtensions = configured.stream()
        .map(x -> x.toLowerCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }

  public static ConnectorPolicy offline() { return new ConnectorPolicy(true, Set.of()); }

  public void requireAllowed(String extension) {
    if (extension == null || extension.isBlank()) throw new IllegalArgumentException("extension is required");
    String normalized = extension.toLowerCase(Locale.ROOT);
    if (offlineMode || !allowedExtensions.contains(normalized))
      throw new DuckException("extension is not enabled by the runtime policy: " + normalized);
  }
}
