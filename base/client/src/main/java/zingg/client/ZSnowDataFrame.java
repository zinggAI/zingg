package zingg.client;

import com.snowflake.snowpark_java.DataFrame;

public class ZSnowDataFrame extends ZFrame <DataFrame> {
    public ZSnowDataFrame (DataFrame df) {
        t = df;
    }
    public ZSnowDataFrame cache() {
        return new ZSnowDataFrame(t.cacheResult());
    }
    public ZSnowDataFrame distinct() {
        return new ZSnowDataFrame(t.distinct());
    }
    public String[] columns() {
        return t.schema().names();
    }
    public ZSnowDataFrame select(String... cols) {
        return new ZSnowDataFrame(t.select(cols));
    }
}
