package io.zingg.duckdb.engine;

import java.io.IOException;
import java.nio.file.*;

/** Best-effort independent process and spill-directory measurements. */
final class ProcessResourceSnapshot {
  private ProcessResourceSnapshot() {}

  static long residentBytes() {
    Path status = Path.of("/proc/self/status");
    if (Files.isRegularFile(status)) try {
      for (String line : Files.readAllLines(status)) {
        if (line.startsWith("VmRSS:")) {
          String[] parts = line.trim().split("\\s+");
          long kib = Long.parseLong(parts[1]);
          return kib * 1024L;
        }
      }
    } catch (IOException | RuntimeException ignored) { }
    long pid=ProcessHandle.current().pid();
    try {
      boolean windows=System.getProperty("os.name","").toLowerCase(java.util.Locale.ROOT).contains("win");
      var process=windows
          ? new ProcessBuilder("tasklist","/FI","PID eq "+pid,"/FO","CSV","/NH").redirectErrorStream(true).start()
          : new ProcessBuilder("ps","-o","rss=","-p",Long.toString(pid)).redirectErrorStream(true).start();
      if(!process.waitFor(2,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();return -1;}
      String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).trim();
      if(output.isBlank())return -1;
      if(windows){var matcher=java.util.regex.Pattern.compile("([0-9.,]+)\\s*K[B]?").matcher(output);if(matcher.find())return Long.parseLong(matcher.group(1).replaceAll("[^0-9]", ""))*1024L;}
      else {String value=output.replaceAll("[^0-9].*$","").trim();if(!value.isBlank())return Long.parseLong(value)*1024L;}
    } catch (Exception ignored) { }
    return -1;
  }

  static long directoryBytes(String value) {
    if (value == null || value.isBlank() || ":memory:".equals(value)) return 0;
    Path directory;
    try { directory = Path.of(value).toAbsolutePath().normalize(); }
    catch (RuntimeException e) { return -1; }
    if (!Files.isDirectory(directory)) return 0;
    long total = 0;
    try (var paths = Files.walk(directory)) {
      for (Path path : paths.filter(Files::isRegularFile).toList()) {
        try { total = Math.addExact(total, Files.size(path)); }
        catch (IOException | ArithmeticException e) { return -1; }
      }
      return total;
    } catch (IOException e) { return -1; }
  }
}
