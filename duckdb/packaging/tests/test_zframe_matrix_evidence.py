import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest
from unittest.mock import patch


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "zframe_matrix_generator", ROOT / "tools" / "generate-zframe-adapter-matrix.py"
)
MATRIX = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(MATRIX)


class ZFrameMatrixEvidenceTest(unittest.TestCase):
    def test_extracts_method_body_with_nested_blocks_and_braces_in_literals(self):
        source = r'''class Example {
          void differential() {
            String text = "} not the end";
            /* } also not the end */
            if (true) { assertEquals("{", text); }
          }
        }'''
        body = MATRIX.java_method_body(source, "differential")
        self.assertIn('assertEquals("{", text)', body)
        self.assertNotIn("class Example", body)

    def test_rejects_missing_or_ambiguous_method_declaration(self):
        with self.assertRaisesRegex(ValueError, "found 0"):
            MATRIX.java_method_body("class Example {}", "missing")
        source = "class Example { void duplicate() {} void duplicate() {} }"
        with self.assertRaisesRegex(ValueError, "found 2"):
            MATRIX.java_method_body(source, "duplicate")

    def test_evidence_must_be_inside_named_method(self):
        source = "class Example { void differential() { sparkFrame.count(); } void other() { assertTrue(true); } }"
        with self.assertRaisesRegex(ValueError, "no recognized behavioral assertion"):
            MATRIX.verify_differential_evidence(source, "differential")

    def test_evidence_requires_both_oracles_and_behavior_assertion(self):
        MATRIX.verify_differential_evidence(
            "class Example { void differential() { assertEquals(spark.count(), duck.count()); } }",
            "differential",
        )
        with self.assertRaisesRegex(ValueError, "both Spark and DuckDB"):
            MATRIX.verify_differential_evidence(
                "class Example { void differential() { assertEquals(1, duck.count()); } }",
                "differential",
            )

    def test_isolated_evidence_requires_overload_call_inside_assertion(self):
        self.assertTrue(MATRIX.assertion_references_overload(
            'void probe() { assertEquals(expected, duck.aggSum("metric")); }', "aggSum"))
        self.assertTrue(MATRIX.assertion_references_overload(
            "void probe() { assertEquals(expected, capture(duck::show)); }", "show"))
        self.assertFalse(MATRIX.assertion_references_overload(
            'void probe() { duck.aggSum("metric"); assertEquals(expected, actual); }', "aggSum"))

    def test_counts_nested_signature_and_invocation_arguments(self):
        self.assertEqual(3, MATRIX.parameter_count("Map<A, B> value, String[] names, int count"))
        body = r'''void probe() { frame.equalTo("a,b", nested(call(1, 2), new int[] {3, 4})); frame::show; }'''
        self.assertEqual({2}, MATRIX.invocation_arities(body, "equalTo"))
        self.assertEqual({0}, MATRIX.invocation_arities(body, "show"))

    def test_varargs_signature_accepts_multi_argument_invocation(self):
        signature = "public abstract Frame select(java.lang.String...);"
        self.assertRegex(signature, r"\.\.\.\s*\);$")
        self.assertEqual(1, MATRIX.signature_arity(signature, "select"))

    def test_matrix_requires_exact_javac_overload_resolution(self):
        matrix = MATRIX.generate()
        linked = [row for row in matrix["methods"] if row["evidence"]]
        self.assertGreater(len(linked), 0)
        isolated = [row for row in linked
                    if row["adapterDisposition"] == "PER_OVERLOAD_SPARK_DIFFERENTIAL"]
        self.assertEqual(64, len(isolated))
        self.assertEqual({"asStringMatchesSparkForQualifiedSelfJoin",
                          "aggSumMatchesSpark", "getMaxValMatchesSpark", "countMatchesSpark",
                          "showSchemaMatchesSpark", "columnsMatchesSpark",
                          "fieldNamesMatchesSpark", "collectFirstColumnMatchesSpark", "isEmptyMatchesSpark",
                          "getMatchesSpark", "getAsIntMatchesSpark", "getAsLongMatchesSpark",
                          "getAsDoubleMatchesSpark", "getAsStringMatchesSpark",
                          "headMatchesSpark", "getOnlyObjectFromRowMatchesSpark",
                          "collectAsListMatchesSpark",
                          "limitMatchesSpark",
                          "dropDuplicatesArrayMatchesSpark",
                          "dropDuplicatesVarargsMatchesSpark",
                          "withColumnsMatchesSpark",
                          "withColumnLiteralMatchesSpark",
                          "equalToStringMatchesSpark",
                          "equalToIntMatchesSpark",
                          "equalToDoubleMatchesSpark",
                          "gtDoubleMatchesSpark",
                          "gtColumnToIdentityMatchesSpark",
                          "andColumnsMatchesSpark",
                          "notColumnMatchesSpark",
                          "orColumnsMatchesSpark",
                          "gtColumnsMatchesSpark",
                          "equalToColumnsMatchesSpark",
                          "getColsMatchesSpark",
                          "fieldsMatchesSpark",
                          "fieldIndexMatchesSpark",
                          "filterNotNullCondMatchesSpark",
                          "filterNullCondMatchesSpark",
                          "isNotNullColumnMatchesSpark",
                          "colStringMatchesSpark",
                          "concatColumnsMatchesSpark",
                          "notEqualStringMatchesSpark",
                          "notEqualIntMatchesSpark",
                          "notEqualColumnToIdentityMatchesSpark",
                          "unionByNameAllowMissingMatchesSpark",
                          "aliasedSelfJoinPreservesDuplicateNamesAndSqlNullEquality",
                          "prefixedJoinMatchesSparkForDuplicatesAndNulls",
                          "rightJoinMatchesSparkForDuplicateNullAndUnmatchedKeys",
                          "joinOnColumnUsingMatchesSparkForDuplicatesAndNulls",
                          "joinOnColumnExpressionPreservesSparkDuplicateNames",
                          "distinctMatchesSparkForDuplicateAndNullRows",
                          "exceptAndIntersectMatchSparkSetSemanticsForDuplicatesAndNulls",
                          "groupByCountOverloadsMatchSparkForDuplicatesAndNullKeys",
                          "substrMatchesSparkForNegativeZeroAndLongRanges",
                          "splitMatchesSparkForRepeatedTrailingEmptyAndNullValues",
                          "sampleDoubleMatchesSparkForNoReplacementStatistics",
                          "withColumnRenamedMatchesSparkForExistingAndMissingNames",
                          "explodeMatchesSparkForNullEmptyAndDuplicateArrayElements",
                          "selectStringVarargsMatchesSparkForOrderDuplicatesAndNulls",
                          "showNoArgsMatchesSpark", "showBooleanMatchesSpark", "showIntMatchesSpark",
                          "showIntBooleanMatchesSpark"},
                         {row["evidence"]["testMethod"] for row in isolated})
        self.assertTrue(all(row["evidence"].get("semanticAssertionScope") == "ISOLATED_TEST_METHOD"
                            for row in isolated))
        self.assertTrue(all(row["evidence"].get("semanticCoverage") == "DIRECT_SPARK_OUTPUT_COMPARISON"
                            for row in isolated))
        self.assertTrue(all(row["evidence"].get("overloadResolution") == "JAVAC_ATTRIBUTED_EXACT"
                            for row in isolated))
        self.assertTrue(all(row["evidence"].get("overloadResolution") == "JAVAC_ATTRIBUTED_EXACT"
                            for row in linked))
        self.assertTrue(all(row["evidence"].get("overloadResolutionTestClass") == "ZFrameOverloadResolutionTest"
                            for row in linked))
        selected_only = [row for row in linked if row not in isolated]
        self.assertTrue(all(row["evidence"].get("semanticAssertionScope")
                            == "NOT_ATTRIBUTABLE_TO_THIS_OVERLOAD" for row in selected_only))
        self.assertTrue(all(row["evidence"].get("semanticCoverage")
                            == "UNPROVEN_PER_OVERLOAD" for row in selected_only))
        self.assertTrue(all(row["evidence"].get("level")
                            == "OVERLOAD_SELECTED_IN_SHARED_SPARK_FIXTURE" for row in selected_only))
        self.assertTrue(all(row["adapterDisposition"] == "OVERLOAD_SELECTED_SEMANTICS_UNPROVEN"
                            for row in selected_only))

    def test_matrix_fails_closed_if_javac_resolver_is_missing(self):
        with TemporaryDirectory() as directory:
            incomplete = Path(directory) / "resolver.java"
            incomplete.write_text("class MissingResolver {}", encoding="utf-8")
            with patch.object(MATRIX, "OVERLOAD_RESOLUTION", incomplete):
                with self.assertRaisesRegex(ValueError, "javac-attributed"):
                    MATRIX.generate()


if __name__ == "__main__":
    unittest.main()
