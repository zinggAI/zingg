package zingg.client;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Row;

public class ZFrame <T> {
    T t;

    public  ZFrame <T> cache() {return null;};
    public    String[] columns() {return null;}
    public    ZFrame <T> select(Column... cols) {return null;};
    public    ZFrame <T> select(String... cols) {return null;};
    public    ZFrame <T> select(String col) {return null;};
    public    ZFrame <T> distinct() {return null;};
    public   java.util.List<Row> collectAsList() {return null;};
}