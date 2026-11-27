package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.RuntimeDiagnostics;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/** Immutable JSON profiling evidence for one runtime operation. */
public record ProfileCapture(String profile, String build, String request,
    RuntimeDiagnostics runtime, String plan, long wallClockMillis,
    long outputBytes, long processResidentBytes, String inputDigest,
    String configurationDigest, Instant capturedAt) {
  public ProfileCapture {
    if (profile == null || profile.isBlank() || build == null || build.isBlank()
        || request == null || request.isBlank() || runtime == null || plan == null
        || inputDigest == null || configurationDigest == null || capturedAt == null)
      throw new IllegalArgumentException("profile capture identity and measurements are required");
    if (wallClockMillis < 0 || outputBytes < 0) throw new IllegalArgumentException("profile measurements cannot be negative");
  }

  public String filenameStem() { return profile + "-" + inputDigest + "-" + configurationDigest; }

  /** Writes once and refuses to overwrite an existing capture. */
  public Path write(Path directory) throws IOException {
    Objects.requireNonNull(directory, "directory");
    directory = directory.toAbsolutePath().normalize();
    Files.createDirectories(directory);
    Path target = directory.resolve(filenameStem() + ".json").normalize();
    if (!target.getParent().equals(directory)) throw new IOException("invalid capture filename");
    if (Files.exists(target)) throw new FileAlreadyExistsException(target.toString());
    Path temporary = Files.createTempFile(directory, ".profile-", ".partial");
    try {
      Files.writeString(temporary, json(), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
      try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
      catch (AtomicMoveNotSupportedException e) { Files.move(temporary, target); }
      return target;
    } finally { Files.deleteIfExists(temporary); }
  }

  private String json() {
    return "{\"profile\":"+q(profile)+",\"build\":"+q(build)+",\"request\":"+q(request)
        +",\"runtime\":{\"memoryLimit\":"+q(runtime.memoryLimit())+",\"maxTempDirectorySize\":"+q(runtime.maxTempDirectorySize())
        +",\"tempDirectory\":"+q(runtime.tempDirectory())+",\"threads\":"+q(runtime.threads())+",\"heapUsedBytes\":"+runtime.heapUsedBytes()
        +",\"heapMaxBytes\":"+runtime.heapMaxBytes()+",\"processResidentBytes\":"+runtime.processResidentBytes()
        +",\"tempDirectoryUsedBytes\":"+runtime.tempDirectoryUsedBytes()+"},\"plan\":"+q(plan)
        +",\"measurements\":{\"wallClockMillis\":"+wallClockMillis+",\"outputBytes\":"+outputBytes
        +",\"processResidentBytes\":"+processResidentBytes+"},\"inputDigest\":"+q(inputDigest)
        +",\"configurationDigest\":"+q(configurationDigest)+",\"capturedAt\":"+q(capturedAt.toString())+"}";
  }
  private static String q(String value) { return "\""+value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")+"\""; }
  public static String digest(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))).substring(0,16); } catch (Exception e) { throw new IllegalStateException(e); } }
}
