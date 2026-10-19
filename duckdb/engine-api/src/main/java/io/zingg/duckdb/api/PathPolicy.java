package io.zingg.duckdb.api;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public final class PathPolicy {
  private static final java.util.regex.Pattern URI_SCHEME =
      java.util.regex.Pattern.compile("(?i)^[a-z][a-z0-9+.-]*:[/\\\\].*");

  private final Path inputRoot;
  private final Path outputRoot;

  public PathPolicy(Path inputRoot, Path outputRoot) {
    this.inputRoot = normalize(inputRoot);
    this.outputRoot = normalize(outputRoot);
  }

  public Path input(Path path) { return checked(path, inputRoot, "input"); }
  public Path output(Path path) { return checked(path, outputRoot, "output"); }

  private static Path checked(Path path, Path root, String kind) {
    if (path == null) throw new DuckException(kind + " path is required");
    String raw = path.toString();
    // Linux Path implementations collapse https://host into https:/host, so
    // reject any URI-like scheme followed by a slash or backslash. Preserve
    // Windows drive-absolute paths (C:\\...) as ordinary filesystem paths.
    if (!isWindowsDrivePath(raw) && URI_SCHEME.matcher(raw).matches())
      throw new DuckException(kind + " remote URI schemes are not allowed: " + raw);
    Path normalized = normalize(path);
    if (root != null && !normalized.startsWith(root))
      throw new DuckException(kind + " path escapes configured root: " + normalized);
    if (hasSymbolicLinkComponent(normalized))
      throw new DuckException(kind + " symbolic links are not allowed: " + normalized);
    return normalized;
  }

  private static boolean hasSymbolicLinkComponent(Path path) {
    for (Path current = path; current != null; current = current.getParent()) {
      if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) return true;
    }
    return false;
  }

  private static boolean isWindowsDrivePath(String raw) {
    return raw.length() >= 3 && Character.isLetter(raw.charAt(0)) && raw.charAt(1) == ':'
        && (raw.charAt(2) == '\\' || raw.charAt(2) == '/');
  }

  private static Path normalize(Path path) { return path == null ? null : path.toAbsolutePath().normalize(); }
}
