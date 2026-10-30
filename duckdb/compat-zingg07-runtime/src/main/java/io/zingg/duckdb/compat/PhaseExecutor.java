package io.zingg.duckdb.compat;
import io.zingg.duckdb.api.Frame;
public interface PhaseExecutor { Frame execute(ZinggJob job,Frame input); }
