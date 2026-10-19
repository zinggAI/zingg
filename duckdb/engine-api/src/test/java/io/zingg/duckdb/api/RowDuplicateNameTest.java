package io.zingg.duckdb.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class RowDuplicateNameTest {
  @Test
  void nameLookupUsesLastDuplicateOrdinalLikeTheZinggSparkContract() {
    var row = new Row(List.of("same", "same"), List.of("first", "last"));
    assertEquals("last", row.get("same"));
  }
}
