package io.zingg.duckdb.worker;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.zingg.duckdb.protocol.ProtocolCodec;
import io.zingg.duckdb.protocol.WorkerMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainOptionsTest {
  @Test
  void tempDirectoryOptionIsAppliedToTheWorkerRuntime(@TempDir Path temp) throws Exception {
    Path spill = Files.createDirectory(temp.resolve("dedicated-spill"));
    String requests = ProtocolCodec.encode(new WorkerMessage("status", "status", "")) + "\n"
        + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
    var input = new ByteArrayInputStream(requests.getBytes(StandardCharsets.UTF_8));
    var bytes = new ByteArrayOutputStream();
    var output = new PrintStream(bytes, true, StandardCharsets.UTF_8);
    var oldIn = System.in;
    var oldOut = System.out;
    try {
      System.setIn(input);
      System.setOut(output);
      Main.main(new String[] {"--url", "jdbc:duckdb:", "--threads", "1",
          "--temp-directory", spill.toString(), "--max-temp-bytes", "1048576"});
    } finally {
      System.setIn(oldIn);
      System.setOut(oldOut);
      output.close();
    }
    var responses = bytes.toString(StandardCharsets.UTF_8).lines().map(ProtocolCodec::decode).toList();
    assertTrue(responses.get(0).payload().contains("temp_directory=" + spill.toAbsolutePath()),
        responses.get(0).payload());
    assertTrue(responses.get(0).payload().contains("max_temp_directory_size=1.0 MiB"),
        responses.get(0).payload());
  }

  @Test
  void maxInputBytesOptionIsExposedInWorkerStatus() throws Exception {
    long limit = 1234;
    String requests = ProtocolCodec.encode(new WorkerMessage("status", "status", "")) + "\n"
        + ProtocolCodec.encode(new WorkerMessage("stop", "shutdown", "")) + "\n";
    var input = new ByteArrayInputStream(requests.getBytes(StandardCharsets.UTF_8));
    var bytes = new ByteArrayOutputStream();
    var output = new PrintStream(bytes, true, StandardCharsets.UTF_8);
    var oldIn = System.in;
    var oldOut = System.out;
    try {
      System.setIn(input);
      System.setOut(output);
      Main.main(new String[] {"--url", "jdbc:duckdb:", "--threads", "1",
          "--max-input-bytes", Long.toString(limit)});
    } finally {
      System.setIn(oldIn);
      System.setOut(oldOut);
      output.close();
    }
    var responses = bytes.toString(StandardCharsets.UTF_8).lines().map(ProtocolCodec::decode).toList();
    assertEquals("ok", responses.get(0).operation());
    assertTrue(responses.get(0).payload().contains("max_input_bytes=" + limit), responses.get(0).payload());
    assertEquals(new WorkerMessage("stop", "ok", "stopping"), responses.get(1));
  }
}
