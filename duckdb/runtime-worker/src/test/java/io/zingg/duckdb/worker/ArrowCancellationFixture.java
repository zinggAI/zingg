package io.zingg.duckdb.worker;

import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Test-fixture generator callable by the packaged-worker Python integration test. */
public final class ArrowCancellationFixture {
  private ArrowCancellationFixture() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("output path required");
    Path path = Path.of(args[0]);
    try (var allocator = new org.apache.arrow.memory.RootAllocator();
         var root = org.apache.arrow.vector.VectorSchemaRoot.create(
             new org.apache.arrow.vector.types.pojo.Schema(List.of(
                 new org.apache.arrow.vector.types.pojo.Field("value",
                     org.apache.arrow.vector.types.pojo.FieldType.nullable(
                         new org.apache.arrow.vector.types.pojo.ArrowType.Int(32, true)), null))), allocator);
         var channel = Channels.newChannel(Files.newOutputStream(path));
         var writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, channel)) {
      var vector = (org.apache.arrow.vector.IntVector) root.getVector("value");
      writer.start();
      for (int base = 0; base < 1_048_576; base += 65_536) {
        int count = Math.min(65_536, 1_048_576 - base);
        for (int row = 0; row < count; row++) vector.setSafe(row, base + row);
        root.setRowCount(count);
        writer.writeBatch();
      }
      writer.end();
    }
  }
}
