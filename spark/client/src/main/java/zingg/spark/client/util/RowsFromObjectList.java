package zingg.spark.client.util;

import java.util.List;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import zingg.common.client.util.PojoToArrayConverter;

public class RowsFromObjectList {

    public static <T> Row[] getRows(List<T> t) throws Exception{
        Row[] rows = new Row[t.size()];
        for (int i=0; i < t.size(); ++i){
            rows[i] = RowFactory.create(PojoToArrayConverter.getObjectArray(t.get(i)));
        }
        return rows;
    }
}
