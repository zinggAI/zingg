# Test execution report

## Scope

This report records the WSL test run for all Maven Spark profiles declared in `pom.xml` and the repository PySpark tests.

## Environment

- WSL2
- Maven 3.8.7
- Java 21.0.11
- Python 3.12.3
- PySpark 3.5.5

## Results

| Area | Command/profile | Result |
|---|---|---|
| Common Java tests | `mvn -Dspark=3.5 test` | Common client: 36 passed; common core: 304 passed |
| Spark 3.5 | `mvn -Dspark=3.5 test` | Blocked before Spark tests by Scala compiler bridge failure |
| Spark 4.0 | `mvn -Dspark=4.0 ... test` | Not completed; setup was prohibitively slow after common modules |
| Spark 4.1 | `mvn -Dspark=4.1 ... test` | Not completed; same environment constraint |
| Spark 4.2 | `mvn -Dspark=4.2 ... test` | Not completed; same environment constraint |
| PySpark | `PYTHONPATH=python python3 -m unittest discover -s test -p "test*.py" -v` | 2 import errors during JVM initialization |

## Blocking failures

The Spark 3.5 build failed while compiling the Scala compiler bridge with `scala.reflect.internal.FatalError: bad constant pool index: 0`. The project uses Scala 2.12.10 and the WSL environment uses Java 21; this combination prevents the Spark modules from compiling.

The PySpark suite failed when constructing `zingg.common.client.arguments.model.Arguments`: Py4J resolved the package but the class was not present on the JVM classpath (`TypeError: 'JavaPackage' object is not callable`).

## Follow-up

Run the matrix with the project-supported Java/Scala toolchain and build/install the Java client artifact before invoking the PySpark tests. The failures above are environment/classpath blockers, not assertion failures.