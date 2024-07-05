package zingg.spark.client.util;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import zingg.common.client.ZFrame;
import zingg.common.client.util.DFObjectUtil;
import zingg.spark.client.SparkFrame;

public class SparkDFObjectUtil extends DFObjectUtil<SparkSession, Dataset<Row>, Row, Column> {

    public SparkDFObjectUtil(SparkSession s) {
        super(s);
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getDFFromObjectList(List objList, Class objClass) throws Exception {
        if(objList==null || objClass==null) return null;

        SparkStructTypeFromPojoClass stpc = new SparkStructTypeFromPojoClass();

        List<Row> rows = Arrays.asList(RowsFromObjectList.getRows(objList));
        StructType structType = stpc.getStructType(objClass);
        return new SparkFrame(getSession().createDataFrame(rows, structType));
    }


}
