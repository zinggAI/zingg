package zingg.spark.core.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.IZingg;

import zingg.session.SparkSessionProvider;
import zingg.spark.core.context.ZinggSparkContext;

public class ZinggSparkTester {

    public static IArguments args;
    public static JavaSparkContext ctx;
    public static SparkSession spark;
    public static ZinggSparkContext zsCTX;
    public ArgumentsUtil argsUtil = new ArgumentsUtil();
    public static final Log LOG = LogFactory.getLog(ZinggSparkTester.class);

	protected static final String FIELD_INTEGER = "fieldInteger";
	protected static final String FIELD_DOUBLE = "fieldDouble";

    @BeforeAll
    public static void setup() {
		SparkSessionProvider sparkSessionProvider = SparkSessionProvider.getInstance();
		spark = sparkSessionProvider.getSparkSession();
		ctx = sparkSessionProvider.getJavaSparkContext();
		args = sparkSessionProvider.getArgs();
		zsCTX = sparkSessionProvider.getZinggSparkContext();
    }

	public Dataset<Row> createDFWithDoubles(int numRows, int numCols) {
	
		StructType structType = new StructType();

		List<Double> rowValues = new ArrayList<Double>();

		for (int n = 0; n < numCols; ++n) {
			structType = structType.add("col" + n, DataTypes.DoubleType, false);
			rowValues.add(0d);
		};
		
		List<Row> nums = new ArrayList<Row>();

		IntStream.range(0, numRows).forEachOrdered(n -> {
			nums.add(RowFactory.create(rowValues));
		});


		return spark.createDataFrame(nums, structType);
	}
}
