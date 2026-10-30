"""Generate/check the exact Zingg v0.7 ZFrame adapter disposition matrix."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INVENTORY = ROOT / "docs" / "zframe-v07-inventory.json"
ADAPTER = ROOT / "compat-zingg07-runtime" / "src" / "zingg07-contract" / "java" / "io" / "zingg" / "duckdb" / "compat" / "ZFrameDuckAdapter.java"
OUTPUT = ROOT / "docs" / "zframe-v07-adapter-matrix.json"
DIFFERENTIAL = ROOT / "reference-spark35-tests" / "src" / "test" / "java" / "io" / "zingg" / "duckdb" / "reference" / "ZFrameCoreDifferentialTest.java"
EXACT_CONTRACT = ROOT / "compat-zingg07-runtime" / "src" / "zingg07-contract-test" / "java" / "io" / "zingg" / "duckdb" / "compat" / "Zingg07ExactCompileChecks.java"
OVERLOAD_RESOLUTION = ROOT / "reference-spark35-tests" / "src" / "test" / "java" / "io" / "zingg" / "duckdb" / "reference" / "ZFrameOverloadResolutionTest.java"
UPSTREAM_COMMIT = "48cb157b4f35fcfa733e2e7f9699ea988e018738"

# Explicit evidence links are signature-level claims, not inferred from a method-name
# occurrence. Add a signature only after the named test invokes the same overload and
# asserts behavior; the Spark suite is the oracle, while exact-contract is local-only.
SPARK_DIFFERENTIALS = {
    "public abstract zingg.common.client.ZFrame<D, R, C> as(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> coalesce(int);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> drop(C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> drop(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> drop(java.lang.String...);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> dropDuplicates(java.lang.String, java.lang.String...);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> explode(java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> filterInCond(java.lang.String, zingg.common.client.ZFrame<D, R, C>, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> filterNotNullCond(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> filterNullCond(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> groupByMinMaxScore(C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> join(zingg.common.client.ZFrame<D, R, C>, java.lang.String, boolean, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> join(zingg.common.client.ZFrame<D, R, C>, java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> join(zingg.common.client.ZFrame<D, R, C>, java.lang.String, java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> joinOnCol(zingg.common.client.ZFrame<D, R, C>, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> repartition(int);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> repartition(int, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> repartition(int, scala.collection.Seq<C>);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> repartition(scala.collection.Seq<C>);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> sample(boolean, double);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> sample(boolean, float);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> select(C...);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> select(java.lang.String...);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> select(java.util.List<C>);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> selectExpr(java.lang.String...);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> split(java.lang.String, java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> withColumnRenamed(java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract <A> zingg.common.client.ZFrame<D, R, C> withColumn(java.lang.String, A);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> filter(C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C and(C, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C col(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C concat(C, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C equalTo(C, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C equalTo(java.lang.String, double);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C equalTo(java.lang.String, int);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C equalTo(java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C gt(C, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C gt(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C gt(java.lang.String, double);": "gtDoubleMatchesSpark",
    "public abstract C gt(zingg.common.client.ZFrame<D, R, C>, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C isNotNull(C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C not(C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C notEqual(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C notEqual(java.lang.String, int);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C notEqual(java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C or(C, C);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract double aggSum(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract double getAsDouble(R, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract int fieldIndex(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract int getAsInt(R, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.Object get(R, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.Object getMaxVal(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.Object getOnlyObjectFromRow(R);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract C[] getCols();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.String getAsString(R, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.String showSchema();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.String[] columns();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.lang.String[] fieldNames();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.util.List<java.lang.String> collectFirstColumn();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract long count();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract long getAsLong(R, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract java.util.List<R> collectAsList();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> cache();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> orderBy(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract D df();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract R head();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract boolean isEmpty();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.FieldData[] fields();": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> sortAscending(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> sortDescending(java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> toDF(java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> toDF(java.lang.String[]);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> withColumns(java.lang.String[], C[]);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> dropDuplicates(java.lang.String[]);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> limit(int);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> countDistinct(java.lang.String, java.lang.String, java.lang.String);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> union(zingg.common.client.ZFrame<D, R, C>);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> unionAll(zingg.common.client.ZFrame<D, R, C>);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
    "public abstract zingg.common.client.ZFrame<D, R, C> unionByName(zingg.common.client.ZFrame<D, R, C>, boolean);": "selectedOperationsMatchSparkForNullAndDuplicateFixtures",
}

# These tests isolate one exact overload and compare its observable result directly
# against Spark. Keep the method name per signature so the matrix cannot accidentally
# promote unrelated calls merely because they share a large fixture.
PER_OVERLOAD_SPARK_DIFFERENTIALS = {
    "public abstract zingg.common.client.ZFrame<D, R, C> as(java.lang.String);": "asStringMatchesSparkForQualifiedSelfJoin",
    "public abstract zingg.common.client.ZFrame<D, R, C> join(zingg.common.client.ZFrame<D, R, C>, C, java.lang.String);": "aliasedSelfJoinPreservesDuplicateNamesAndSqlNullEquality",
    "public abstract zingg.common.client.ZFrame<D, R, C> join(zingg.common.client.ZFrame<D, R, C>, java.lang.String);": "prefixedJoinMatchesSparkForDuplicatesAndNulls",
    "public abstract zingg.common.client.ZFrame<D, R, C> joinRight(zingg.common.client.ZFrame<D, R, C>, java.lang.String);": "rightJoinMatchesSparkForDuplicateNullAndUnmatchedKeys",
    "public abstract zingg.common.client.ZFrame<D, R, C> joinOnCol(zingg.common.client.ZFrame<D, R, C>, java.lang.String);": "joinOnColumnUsingMatchesSparkForDuplicatesAndNulls",
    "public abstract zingg.common.client.ZFrame<D, R, C> joinOnCol(zingg.common.client.ZFrame<D, R, C>, C);": "joinOnColumnExpressionPreservesSparkDuplicateNames",
    "public abstract zingg.common.client.ZFrame<D, R, C> distinct();": "distinctMatchesSparkForDuplicateAndNullRows",
    "public abstract zingg.common.client.ZFrame<D, R, C> except(zingg.common.client.ZFrame<D, R, C>);": "exceptAndIntersectMatchSparkSetSemanticsForDuplicatesAndNulls",
    "public abstract zingg.common.client.ZFrame<D, R, C> intersect(zingg.common.client.ZFrame<D, R, C>);": "exceptAndIntersectMatchSparkSetSemanticsForDuplicatesAndNulls",
    "public abstract zingg.common.client.ZFrame<D, R, C> groupByCount(java.lang.String, java.lang.String);": "groupByCountOverloadsMatchSparkForDuplicatesAndNullKeys",
    "public abstract zingg.common.client.ZFrame<D, R, C> groupByCount(java.lang.String, java.lang.String, java.lang.String);": "groupByCountOverloadsMatchSparkForDuplicatesAndNullKeys",
    "public abstract C substr(C, int, int);": "substrMatchesSparkForNegativeZeroAndLongRanges",
    "public abstract zingg.common.client.ZFrame<D, R, C> split(java.lang.String, java.lang.String, java.lang.String);": "splitMatchesSparkForRepeatedTrailingEmptyAndNullValues",
    "public abstract zingg.common.client.ZFrame<D, R, C> sample(boolean, double);": "sampleDoubleMatchesSparkForNoReplacementStatistics",
    "public abstract zingg.common.client.ZFrame<D, R, C> withColumnRenamed(java.lang.String, java.lang.String);": "withColumnRenamedMatchesSparkForExistingAndMissingNames",
    "public abstract zingg.common.client.ZFrame<D, R, C> explode(java.lang.String, java.lang.String);": "explodeMatchesSparkForNullEmptyAndDuplicateArrayElements",
    "public abstract zingg.common.client.ZFrame<D, R, C> select(java.lang.String...);": "selectStringVarargsMatchesSparkForOrderDuplicatesAndNulls",
    "public abstract zingg.common.client.ZFrame<D, R, C> limit(int);": "limitMatchesSpark",
    "public abstract zingg.common.client.ZFrame<D, R, C> dropDuplicates(java.lang.String[]);": "dropDuplicatesArrayMatchesSpark",
    "public abstract zingg.common.client.ZFrame<D, R, C> dropDuplicates(java.lang.String, java.lang.String...);": "dropDuplicatesVarargsMatchesSpark",
    "public abstract zingg.common.client.ZFrame<D, R, C> withColumns(java.lang.String[], C[]);": "withColumnsMatchesSpark",
    "public abstract <A> zingg.common.client.ZFrame<D, R, C> withColumn(java.lang.String, A);": "withColumnLiteralMatchesSpark",
    "public abstract C equalTo(java.lang.String, java.lang.String);": "equalToStringMatchesSpark",
    "public abstract C equalTo(java.lang.String, int);": "equalToIntMatchesSpark",
    "public abstract C equalTo(java.lang.String, double);": "equalToDoubleMatchesSpark",
    "public abstract C gt(java.lang.String);": "gtColumnToIdentityMatchesSpark",
    "public abstract C gt(java.lang.String, double);": "gtDoubleMatchesSpark",
    "public abstract C equalTo(C, C);": "equalToColumnsMatchesSpark",
    "public abstract C and(C, C);": "andColumnsMatchesSpark",
    "public abstract C not(C);": "notColumnMatchesSpark",
    "public abstract C or(C, C);": "orColumnsMatchesSpark",
    "public abstract C gt(C, C);": "gtColumnsMatchesSpark",
    "public abstract C[] getCols();": "getColsMatchesSpark",
    "public abstract zingg.common.client.FieldData[] fields();": "fieldsMatchesSpark",
    "public abstract int fieldIndex(java.lang.String);": "fieldIndexMatchesSpark",
    "public abstract zingg.common.client.ZFrame<D, R, C> filterNotNullCond(java.lang.String);": "filterNotNullCondMatchesSpark",
    "public abstract zingg.common.client.ZFrame<D, R, C> filterNullCond(java.lang.String);": "filterNullCondMatchesSpark",
    "public abstract C isNotNull(C);": "isNotNullColumnMatchesSpark",
    "public abstract C col(java.lang.String);": "colStringMatchesSpark",
    "public abstract C concat(C, C);": "concatColumnsMatchesSpark",
    "public abstract C notEqual(java.lang.String, java.lang.String);": "notEqualStringMatchesSpark",
    "public abstract C notEqual(java.lang.String, int);": "notEqualIntMatchesSpark",
    "public abstract C notEqual(java.lang.String);": "notEqualColumnToIdentityMatchesSpark",
    "public abstract zingg.common.client.ZFrame<D, R, C> unionByName(zingg.common.client.ZFrame<D, R, C>, boolean);": "unionByNameAllowMissingMatchesSpark",
    "public abstract R head();": "headMatchesSpark",
    "public abstract java.lang.Object getOnlyObjectFromRow(R);": "getOnlyObjectFromRowMatchesSpark",
    "public abstract java.util.List<R> collectAsList();": "collectAsListMatchesSpark",
    "public abstract double aggSum(java.lang.String);": "aggSumMatchesSpark",
    "public abstract double getAsDouble(R, java.lang.String);": "getAsDoubleMatchesSpark",
    "public abstract int getAsInt(R, java.lang.String);": "getAsIntMatchesSpark",
    "public abstract long getAsLong(R, java.lang.String);": "getAsLongMatchesSpark",
    "public abstract java.lang.String getAsString(R, java.lang.String);": "getAsStringMatchesSpark",
    "public abstract java.lang.Object get(R, java.lang.String);": "getMatchesSpark",
    "public abstract long count();": "countMatchesSpark",
    "public abstract java.lang.String showSchema();": "showSchemaMatchesSpark",
    "public abstract boolean isEmpty();": "isEmptyMatchesSpark",
    "public abstract java.lang.Object getMaxVal(java.lang.String);": "getMaxValMatchesSpark",
    "public abstract java.lang.String[] columns();": "columnsMatchesSpark",
    "public abstract java.lang.String[] fieldNames();": "fieldNamesMatchesSpark",
    "public abstract java.util.List<java.lang.String> collectFirstColumn();": "collectFirstColumnMatchesSpark",
    "public abstract void show();": "showNoArgsMatchesSpark",
    "public abstract void show(boolean);": "showBooleanMatchesSpark",
    "public abstract void show(int);": "showIntMatchesSpark",
    "public abstract void show(int, boolean);": "showIntBooleanMatchesSpark",
}

EXACT_ONLY = {}


def method_name(signature: str) -> str:
    match = re.search(r"\b([A-Za-z_][A-Za-z0-9_]*)\([^()]*\);$", signature)
    if not match:
        raise ValueError(f"cannot parse method name from signature: {signature}")
    return match.group(1)


def java_method_body(source: str, method: str) -> str:
    """Return one Java method body while respecting strings/comments and nested blocks."""
    declarations = list(re.finditer(
        rf"\b(?:(?:public|protected|private)\s+)?(?:static\s+)?[\w<>?,.\[\] ]+\s+"
        rf"{re.escape(method)}\s*\([^;{{}}]*\)\s*(?:throws\s+[\w., ]+\s*)?\{{",
        source,
    ))
    if len(declarations) != 1:
        raise ValueError(f"expected exactly one Java method declaration for {method}, found {len(declarations)}")
    start = declarations[0].end() - 1
    depth = 0
    state = "code"
    escaped = False
    index = start
    while index < len(source):
        char = source[index]
        next_char = source[index + 1] if index + 1 < len(source) else ""
        if state == "line-comment":
            if char == "\n":
                state = "code"
        elif state == "block-comment":
            if char == "*" and next_char == "/":
                state = "code"
                index += 1
        elif state in ("string", "char"):
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif (state == "string" and char == '"') or (state == "char" and char == "'"):
                state = "code"
        else:
            if char == "/" and next_char == "/":
                state = "line-comment"
                index += 1
            elif char == "/" and next_char == "*":
                state = "block-comment"
                index += 1
            elif char == '"':
                state = "string"
            elif char == "'":
                state = "char"
            elif char == "{":
                depth += 1
            elif char == "}":
                depth -= 1
                if depth == 0:
                    return source[start + 1:index]
        index += 1
    raise ValueError(f"unterminated Java method body for {method}")


def verify_differential_evidence(source: str, method: str) -> None:
    body = java_method_body(source, method)
    if not re.search(r"\b(?:assertEquals|assertTrue|assertFalse|assertNull|assertThrows|assertFrameContents|assertExpressionParity)\s*\(", body):
        raise ValueError(f"Spark differential method {method} has no recognized behavioral assertion")
    if "spark" not in body or "duck" not in body.lower():
        raise ValueError(f"Spark differential method {method} must contain both Spark and DuckDB evidence")


def assertion_arguments(body: str) -> list[str]:
    """Extract balanced arguments of supported JUnit assertions, including nested calls."""
    arguments = []
    pattern = re.compile(r"\b(assertEquals|assertTrue|assertFalse|assertNull|assertThrows)\s*\(")
    for match in pattern.finditer(body):
        start = match.end()
        depth = 1
        state = "code"
        escaped = False
        index = start
        while index < len(body) and depth:
            char = body[index]
            next_char = body[index + 1] if index + 1 < len(body) else ""
            if state == "line-comment":
                if char == "\n": state = "code"
            elif state == "block-comment":
                if char == "*" and next_char == "/": state = "code"; index += 1
            elif state in ("string", "char"):
                if escaped: escaped = False
                elif char == "\\": escaped = True
                elif (state == "string" and char == '"') or (state == "char" and char == "'"):
                    state = "code"
            else:
                if char == "/" and next_char == "/": state = "line-comment"; index += 1
                elif char == "/" and next_char == "*": state = "block-comment"; index += 1
                elif char == '"': state = "string"
                elif char == "'": state = "char"
                elif char == "(": depth += 1
                elif char == ")": depth -= 1
            index += 1
        if depth:
            raise ValueError(f"unterminated JUnit assertion {match.group(1)}")
        arguments.append(body[start:index - 1])
    return arguments


def assertion_references_overload(body: str, method: str) -> bool:
    call = re.compile(rf"(?:\.|::)\s*{re.escape(method)}\s*(?:\(|\b)")
    return any(call.search(arguments) for arguments in assertion_arguments(body))


def parameter_count(text: str) -> int:
    """Count comma-separated Java parameters/arguments at their top nesting level."""
    if not text.strip():
        return 0
    round_depth = square_depth = brace_depth = angle_depth = 0
    state = "code"
    escaped = False
    count = 1
    for index, char in enumerate(text):
        next_char = text[index + 1] if index + 1 < len(text) else ""
        if state == "line-comment":
            if char == "\n": state = "code"
        elif state == "block-comment":
            if char == "*" and next_char == "/": state = "code"
        elif state in ("string", "char"):
            if escaped: escaped = False
            elif char == "\\": escaped = True
            elif (state == "string" and char == '"') or (state == "char" and char == "'"):
                state = "code"
        else:
            if char == "/" and next_char == "/": state = "line-comment"
            elif char == "/" and next_char == "*": state = "block-comment"
            elif char == '"': state = "string"
            elif char == "'": state = "char"
            elif char == "(": round_depth += 1
            elif char == ")": round_depth -= 1
            elif char == "[": square_depth += 1
            elif char == "]": square_depth -= 1
            elif char == "{": brace_depth += 1
            elif char == "}": brace_depth -= 1
            elif char == "<": angle_depth += 1
            elif char == ">" and angle_depth: angle_depth -= 1
            elif char == "," and not (round_depth or square_depth or brace_depth or angle_depth):
                count += 1
    return count


def invocation_arities(body: str, method: str) -> set[int]:
    """Return argument counts for receiver-qualified calls to a Java method."""
    arities = set()
    arities.update(0 for _ in re.finditer(rf"::\s*{re.escape(method)}\b(?!\s*\()", body))
    for match in re.finditer(rf"(?:\.|::){re.escape(method)}\s*\(", body):
        start = match.end()
        depth = 1
        state = "code"
        escaped = False
        end = start
        while end < len(body) and depth:
            char = body[end]
            next_char = body[end + 1] if end + 1 < len(body) else ""
            if state == "line-comment":
                if char == "\n": state = "code"
            elif state == "block-comment":
                if char == "*" and next_char == "/": state = "code"; end += 1
            elif state in ("string", "char"):
                if escaped: escaped = False
                elif char == "\\": escaped = True
                elif (state == "string" and char == '"') or (state == "char" and char == "'"):
                    state = "code"
            else:
                if char == "/" and next_char == "/": state = "line-comment"; end += 1
                elif char == "/" and next_char == "*": state = "block-comment"; end += 1
                elif char == '"': state = "string"
                elif char == "'": state = "char"
                elif char == "(": depth += 1
                elif char == ")": depth -= 1
            end += 1
        if depth:
            raise ValueError(f"unterminated invocation of {method}")
        arities.add(parameter_count(body[start:end - 1]))
    return arities


def signature_arity(signature: str, method: str) -> int:
    match = re.search(rf"\b{re.escape(method)}\s*\(", signature)
    if not match:
        raise ValueError(f"cannot locate parameter list for {signature}")
    start = match.end()
    depth = 1
    end = start
    while end < len(signature) and depth:
        if signature[end] == "(": depth += 1
        elif signature[end] == ")": depth -= 1
        end += 1
    if depth:
        raise ValueError(f"unterminated parameter list for {signature}")
    return parameter_count(signature[start:end - 1])


def generate() -> dict:
    inventory = json.loads(INVENTORY.read_text(encoding="utf-8"))
    source = ADAPTER.read_text(encoding="utf-8")
    differential_source = DIFFERENTIAL.read_text(encoding="utf-8")
    exact_source = EXACT_CONTRACT.read_text(encoding="utf-8")
    overload_source = OVERLOAD_RESOLUTION.read_text(encoding="utf-8")
    if "differentialCallsResolveToThePinnedZFrameSignatures" not in overload_source or "JavacTask" not in overload_source or "trees.getElement" not in overload_source:
        raise ValueError("javac-attributed ZFrame overload-resolution test is missing or incomplete")
    handled_block = re.search(r"HANDLED_METHODS\s*=\s*Set\.of\((.*?)\);", source, re.S)
    if not handled_block:
        raise ValueError("adapter HANDLED_METHODS declaration was not found")
    handled = set(re.findall(r'"([A-Za-z_][A-Za-z0-9_]*)"', handled_block.group(1)))
    switch_labels = re.findall(r"case\s+([^\n]+?)\s*->", source)
    switch_cases = {name for label in switch_labels
                    for name in re.findall(r'"([A-Za-z_][A-Za-z0-9_]*)"', label)}
    inventory_methods = set(inventory["methods"])
    unknown_evidence = (set(SPARK_DIFFERENTIALS) | set(PER_OVERLOAD_SPARK_DIFFERENTIALS)
                        | set(EXACT_ONLY)) - inventory_methods
    if unknown_evidence:
        raise ValueError("evidence references signatures absent from pinned inventory: "
                         + "; ".join(sorted(unknown_evidence)))
    rows = []
    for signature in inventory["methods"]:
        name = method_name(signature)
        if name in handled and name in switch_cases:
            if signature in PER_OVERLOAD_SPARK_DIFFERENTIALS:
                test = PER_OVERLOAD_SPARK_DIFFERENTIALS[signature]
                verify_differential_evidence(differential_source, test)
                test_body = java_method_body(differential_source, test)
                observed_arities = invocation_arities(test_body, name)
                expected_arity = signature_arity(signature, name)
                minimum_arity = expected_arity - 1 if re.search(r"\.\.\.\s*\);$", signature) else expected_arity
                if not any(arity >= minimum_arity for arity in observed_arities):
                    raise ValueError(
                        f"isolated Spark differential {test} must invoke {name} with a compatible "
                        f"argument count (signature arity {expected_arity}): {signature}; "
                        f"observed {sorted(observed_arities)}")
                if not assertion_references_overload(test_body, name):
                    raise ValueError(
                        f"isolated Spark differential {test} must place the {name} invocation "
                        f"inside a recognized behavioral assertion: {signature}")
                evidence = {"level": "PER_OVERLOAD_SPARK_DIFFERENTIAL",
                            "testClass": "ZFrameCoreDifferentialTest", "testMethod": test,
                            "overloadResolution": "JAVAC_ATTRIBUTED_EXACT",
                            "semanticAssertionScope": "ISOLATED_TEST_METHOD",
                            "semanticCoverage": "DIRECT_SPARK_OUTPUT_COMPARISON",
                            "overloadResolutionTestClass": "ZFrameOverloadResolutionTest",
                            "overloadResolutionTestMethod": "differentialCallsResolveToThePinnedZFrameSignatures",
                            "source": "reference-spark35-tests/src/test/java/io/zingg/duckdb/reference/ZFrameCoreDifferentialTest.java"}
                disposition = "PER_OVERLOAD_SPARK_DIFFERENTIAL"
                rationale = "A dedicated differential test invokes this exact overload and directly compares its observable output with Spark; edge-case breadth and approved Zingg v0.7 phase parity remain separate requirements."
            elif signature in SPARK_DIFFERENTIALS:
                test = SPARK_DIFFERENTIALS[signature]
                verify_differential_evidence(differential_source, test)
                test_body = java_method_body(differential_source, test)
                observed_arities = invocation_arities(test_body, name)
                expected_arity = signature_arity(signature, name)
                minimum_arity = expected_arity - 1 if re.search(r"\.\.\.\s*\);$", signature) else expected_arity
                if not any(arity >= minimum_arity for arity in observed_arities):
                    raise ValueError(
                        f"Spark differential method {test} does not invoke {name} with a compatible "
                        f"argument count (signature arity {expected_arity}): {signature}; "
                        f"observed {sorted(observed_arities)}")
                evidence = {"level": "OVERLOAD_SELECTED_IN_SHARED_SPARK_FIXTURE", "testClass": "ZFrameCoreDifferentialTest",
                            "testMethod": test,
                            "overloadResolution": "JAVAC_ATTRIBUTED_EXACT",
                            "semanticAssertionScope": "NOT_ATTRIBUTABLE_TO_THIS_OVERLOAD",
                            "semanticCoverage": "UNPROVEN_PER_OVERLOAD",
                            "overloadResolutionTestClass": "ZFrameOverloadResolutionTest",
                            "overloadResolutionTestMethod": "differentialCallsResolveToThePinnedZFrameSignatures",
                            "source": "reference-spark35-tests/src/test/java/io/zingg/duckdb/reference/ZFrameCoreDifferentialTest.java"}
                disposition = "OVERLOAD_SELECTED_SEMANTICS_UNPROVEN"
                rationale = "Javac confirms this overload is called inside a shared Spark 3.5.5 fixture, but its behavioral assertions are not attributable to this overload; per-overload semantics, edge cases, and approved Zingg v0.7 phase parity remain unproven."
            elif signature in EXACT_ONLY:
                test = EXACT_ONLY[signature]
                if test not in exact_source:
                    raise ValueError(f"declared exact-contract test not found for: {signature}")
                evidence = {"level": "EXACT_CONTRACT_ONLY", "testClass": "Zingg07ExactCompileChecks",
                            "testMethod": test, "source": "compat-zingg07-runtime/src/zingg07-contract-test/java/io/zingg/duckdb/compat/Zingg07ExactCompileChecks.java"}
                disposition = "EXACT_CONTRACT_ONLY"
                rationale = "The pinned API overload has local exact-contract exercise but no linked Spark differential; semantic parity remains unproven."
            else:
                evidence = None
                disposition = "MAPPED_NOT_DIFFERENTIAL"
                rationale = "A DuckDB adapter route exists; no signature-specific test evidence is linked in this matrix."
        else:
            disposition = "UNMAPPED"
            rationale = "No explicit adapter route exists for this exact method name."
            evidence = None
        rows.append({"signature": signature, "method": name, "adapterDisposition": disposition,
                     "evidence": evidence, "rationale": rationale})
    disposition_names = ("PER_OVERLOAD_SPARK_DIFFERENTIAL", "OVERLOAD_SELECTED_SEMANTICS_UNPROVEN", "EXACT_CONTRACT_ONLY",
                         "MAPPED_NOT_DIFFERENTIAL", "CONDITIONAL", "UNSUPPORTED", "UNMAPPED")
    counts = {key: sum(row["adapterDisposition"] == key for row in rows) for key in disposition_names}
    if sum(counts.values()) != len(inventory["methods"]):
        raise ValueError("disposition counts do not reconcile to pinned method inventory")
    if counts["UNMAPPED"]:
        raise ValueError(f"pinned API has {counts['UNMAPPED']} unmapped signatures")
    return {"upstreamRepository": "https://github.com/zinggai/zingg",
            "upstreamCommit": UPSTREAM_COMMIT,
            "capsuleSha256": inventory["capsuleSha256"],
            "interface": inventory["upstreamClass"],
            "signatureCount": len(rows),
            "dispositionCounts": counts,
            "releaseParityProven": False,
            "methods": rows}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="fail if the checked-in matrix is stale")
    args = parser.parse_args()
    try:
        rendered = json.dumps(generate(), ensure_ascii=False, indent=2) + "\n"
    except (OSError, ValueError, KeyError) as error:
        print(f"cannot generate ZFrame adapter matrix: {error}", file=sys.stderr)
        return 2
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8-sig") != rendered:
            print("ZFrame adapter matrix is stale; run tools/generate-zframe-adapter-matrix.py", file=sys.stderr)
            return 1
        print("ZFRAME_ADAPTER_MATRIX_SUCCESS checked=93")
        return 0
    OUTPUT.write_text(rendered, encoding="utf-8")
    print(f"Wrote {OUTPUT.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
