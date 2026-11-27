package io.zingg.duckdb.api;
public record Column(String name,int jdbcType,String typeName,boolean nullable){public Column{if(name==null||name.isBlank())throw new IllegalArgumentException("column name is required");}}
