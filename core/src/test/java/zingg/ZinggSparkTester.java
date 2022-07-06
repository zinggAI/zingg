package zingg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import zingg.client.Arguments;
import zingg.preprocess.TestStopWords;

public class ZinggSparkTester {

    public static Arguments args;
    public static JavaSparkContext ctx;
    public static SparkSession spark;

    public static final Log LOG = LogFactory.getLog(ZinggSparkTester.class);

	protected static final String FIELD_INTEGER = "fieldInteger";
	protected static final String FIELD_DOUBLE = "fieldDouble";

    @BeforeAll
    public static void setup() {
    	try {
    		spark = SparkSession
    				.builder()
    				.master("local")
    				.appName("Zingg" + "Junit")
    				.getOrCreate();
    		ctx = new JavaSparkContext(spark.sparkContext());
    		JavaSparkContext.jarOfClass(TestStopWords.class);
    		args = new Arguments();
    	} catch (Throwable e) {
    		if (LOG.isDebugEnabled())
    			e.printStackTrace();
    		LOG.info("Problem in spark env setup");
    	}
    }

    @AfterAll
    public static void teardown() {
    	if (ctx != null)
    		ctx.stop();

    	if (spark != null)
    		spark.stop();
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

	protected Dataset<Row> createDFWithSampleNumerics() {
		StructType schema = new StructType(new StructField[] {
				new StructField(FIELD_DOUBLE, DataTypes.DoubleType, true, Metadata.empty()),
				new StructField(FIELD_INTEGER, DataTypes.IntegerType, true, Metadata.empty())
		});
		String a[] = new String[] { 
			"0.55,55",
			"1.234,1234",
			"34,gbp",
			"99.56,9956",
			"56gbp,56",
			"23,23gbp",
			",45",
			"65,",
			",",
			"0.5 gbp,23",
			"56.00,56",
			"$,64.0",
			"null,34",
			"78,null",
			"78,87",
		};
		Dataset<String> dsStr = spark.createDataset(Arrays.asList(a), Encoders.STRING());
		Dataset<Row> df = spark.read().schema(schema).csv(dsStr);
		return df;
	}
}
