package io.zingg.duckdb.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

/** Uses javac's attributed AST to prove which pinned ZFrame overload each fixture invokes. */
class ZFrameOverloadResolutionTest {
  private static final String TEST_METHOD = "selectedOperationsMatchSparkForNullAndDuplicateFixtures";

  @Test
  void differentialCallsResolveToThePinnedZFrameSignatures() throws Exception {
    var compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "a full JDK is required for compiler-attributed overload evidence");
    Path source = Path.of("src/test/java/io/zingg/duckdb/reference/ZFrameCoreDifferentialTest.java")
        .toAbsolutePath().normalize();
    assertTrue(source.toFile().isFile(), "differential source not found: " + source);

    var diagnostics = new DiagnosticCollector<JavaFileObject>();
    try (var fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
      Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(source.toFile());
      List<String> options = List.of("--release", "21", "-proc:none", "-classpath",
          System.getProperty("java.class.path"));
      JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, options, null, units);
      var parsed = task.parse();
      task.analyze();
      List<String> errors = diagnostics.getDiagnostics().stream()
          .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
          .map(Object::toString).toList();
      assertTrue(errors.isEmpty(), () -> "javac could not attribute differential source:\n" + String.join("\n", errors));

      Trees trees = Trees.instance(task);
      Map<String, Set<String>> callsByMethod = new HashMap<>();
      new TreePathScanner<Void, Void>() {
        private String currentMethod;

        @Override
        public Void visitMethod(MethodTree node, Void unused) {
          String previous = currentMethod;
          currentMethod = node.getName().toString();
          try {
            return super.visitMethod(node, unused);
          } finally {
            currentMethod = previous;
          }
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
          recordResolvedElement(trees.getElement(getCurrentPath()), currentMethod, callsByMethod);
          return super.visitMethodInvocation(node, unused);
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Void unused) {
          recordResolvedElement(trees.getElement(getCurrentPath()), currentMethod, callsByMethod);
          return super.visitMemberReference(node, unused);
        }
      }.scan(parsed.iterator().next(), null);

      Map<String, List<String>> expectedByName = expectedSignatures();
      List<String> missing = new ArrayList<>();
      expectedByName.forEach((name, signatures) -> {
        Set<String> actual = callsByMethod.getOrDefault(name, Set.of());
        for (String signature : signatures) {
          if (!actual.contains(signature)) missing.add(signature + " (resolved: " + actual + ")");
        }
      });
      assertTrue(missing.isEmpty(), () -> "ZFrame overloads not resolved in the differential method:\n"
          + String.join("\n", missing));
      assertEquals(expectedByName.values().stream().mapToInt(List::size).sum(),
          expectedByName.values().stream().flatMap(List::stream).distinct().count(),
          "the fixture signature list must not contain duplicates");
    }
  }

  private static void recordResolvedElement(
      javax.lang.model.element.Element element, String currentMethod, Map<String, Set<String>> callsByMethod) {
    if (!TEST_METHOD.equals(currentMethod) || !(element instanceof ExecutableElement executable)
        || !(executable.getEnclosingElement() instanceof TypeElement owner)
        || !owner.getQualifiedName().contentEquals("zingg.common.client.ZFrame")) return;
    String parameters = executable.getParameters().stream()
        .map(parameter -> normalize(parameter.asType().toString()))
        .reduce((left, right) -> left + "," + right).orElse("");
    callsByMethod.computeIfAbsent(executable.getSimpleName().toString(), ignored -> new HashSet<>())
        .add(executable.getSimpleName() + "(" + parameters + ")");
  }

  private static Map<String, List<String>> expectedSignatures() {
    Map<String, List<String>> expected = new HashMap<>();
    for (var method : zingg.common.client.ZFrame.class.getDeclaredMethods()) {
      if (!java.lang.reflect.Modifier.isAbstract(method.getModifiers())) continue;
      String parameters = java.util.Arrays.stream(method.getGenericParameterTypes())
          .map(type -> normalize(type.getTypeName())).reduce((left, right) -> left + "," + right).orElse("");
      expected.computeIfAbsent(method.getName(), ignored -> new ArrayList<>())
          .add(method.getName() + "(" + parameters + ")");
    }
    return expected;
  }

  private static String normalize(String type) {
    return type.replace(" ", "").replace("...", "[]");
  }
}
