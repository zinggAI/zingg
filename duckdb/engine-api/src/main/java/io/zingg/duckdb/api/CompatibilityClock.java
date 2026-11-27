package io.zingg.duckdb.api;
public interface CompatibilityClock { long epochMillis(); static CompatibilityClock system(){return System::currentTimeMillis;} }
