package io.zingg.duckdb.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PathPolicyTest {
  @Test
  void acceptsOnlyPathsUnderConfiguredRoot(@TempDir Path temp) throws Exception {
    var policy = new PathPolicy(temp, temp.resolve("out"));
    Path accepted = temp.resolve("input.csv");
    assertEquals(accepted.toAbsolutePath().normalize(), policy.input(accepted));
    assertThrows(DuckException.class, () -> policy.input(temp.getParent().resolve("escape.csv")));
  }

  @Test
  void rejectsRemoteUris() {
    org.junit.jupiter.api.Assumptions.assumeTrue(
        java.io.File.separatorChar == '/', "default Windows Path rejects URI-shaped strings before PathPolicy receives them");
    var policy = new PathPolicy(null, null);
    for (String uri : java.util.List.of(
        "https://example.test/data.csv", "s3://bucket/data.csv", "file:///tmp/data.csv")) {
      assertThrows(DuckException.class, () -> policy.input(Path.of(uri)), uri);
      assertThrows(DuckException.class, () -> policy.output(Path.of(uri)), uri);
    }
  }

  @Test
  void acceptsWindowsDriveAbsolutePathOnEveryPlatform() {
    var policy = new PathPolicy(null, null);
    assertEquals(Path.of("C:\\data\\records.csv").toAbsolutePath().normalize(),
        policy.input(Path.of("C:\\data\\records.csv")));
  }

  @Test
  void rejectsSymlinkComponents(@TempDir Path temp) throws Exception {
    Path target = Files.createFile(temp.resolve("real.csv"));
    Path link = temp.resolve("link.csv");
    try {
      Files.createSymbolicLink(link, target.getFileName());
    } catch (UnsupportedOperationException | java.nio.file.FileSystemException e) {
      return;
    }
    var policy = new PathPolicy(temp, null);
    assertThrows(DuckException.class, () -> policy.input(link));
  }
}
