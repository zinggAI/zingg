# Documentation hub

This is the canonical map of the Zingg DuckDB documentation.

![Zingg DuckDB logo](../assets/zingg-duckdb-logo.png)

```mermaid
mindmap
  root((Zingg DuckDB))
    Start here
      Project README
      Quick start
      Repository map
    Understand
      Architecture
      Worker protocol
      Output semantics
      Model format
    Integrate
      Python client
      Compatibility profiles
      Legacy import
      Migration guide
    Operate
      Operations runbook
      Profiling
      Resource limits
      Packaging
    Verify
      Validation status
      Test plan
      Build plan
      Benchmarks
```

## Recommended reading paths

### New user

1. [Project README](../README.md)
2. [Worker protocol](worker-protocol.md)
3. [Operations runbook](operations-runbook.md)

### Integrator

1. [Architecture](architecture.md)
2. [Compatibility profiles](compatibility-profiles.md)
3. [Model format](model-format.md)
4. [Migration guide](migration-guide.md)

### Operator or release engineer

1. [Packaging](../packaging/README.md)
2. [Operations runbook](operations-runbook.md)
3. [Profiling](profiling.md)
4. [Validation status](validation-status.md)

### Contributor

1. [Build plan](../build-plan.md)
2. [Test plan](../test-plan.md)
3. [Architecture](architecture.md)
4. [Benchmarks](../benchmarks/README.md)

## Cross-cutting mental model

```mermaid
flowchart LR
    Contract[Contract] --> Implementation[Implementation]
    Implementation --> Validation[Validation]
    Validation --> Evidence[Recorded evidence]
    Evidence --> Release[Packaged release]
    Release --> Contract
```

Every feature should have a contract, an implementation boundary, an executable validation path, and a recorded operational or release consequence.
