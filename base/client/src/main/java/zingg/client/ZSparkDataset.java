package zingg.client;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class ZSparkDataset extends ZFrame <Dataset <Row> > {
    public ZSparkDataset(Dataset <Row> ds) {
        t = ds;
    } 
    public ZSparkDataset cache() {
        return new ZSparkDataset(t.cache());
    }
    public ZSparkDataset distinct() {
        return new ZSparkDataset(t.distinct());
    }
    public String[] columns() {
        return t.columns();
    }
    public ZSparkDataset select(Column... cols) {
        return new ZSparkDataset(t.select(cols));
    }
    public ZSparkDataset select(String col) {
        return new ZSparkDataset(t.select(col));
    }
    public java.util.List<Row> collectAsList() {
        return t.collectAsList();
    }
}