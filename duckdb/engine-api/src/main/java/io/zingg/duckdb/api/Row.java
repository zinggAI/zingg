package io.zingg.duckdb.api;
import java.util.*;
public record Row(List<String> columns,List<Object> values){public Row{columns=List.copyOf(columns);values=Collections.unmodifiableList(new ArrayList<>(values));if(columns.size()!=values.size())throw new IllegalArgumentException("row shape mismatch");}public Object get(String name){int i=ColumnResolver.lastIndexOf(columns,name);if(i<0)throw new DuckException("unknown column: "+name);return values.get(i);}}
