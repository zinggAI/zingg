package io.zingg.duckdb.engine;
public final class DuckPath {private DuckPath(){} public static String sqlLiteral(java.nio.file.Path path){return "'"+path.toString().replace("'","''")+"'";}}
