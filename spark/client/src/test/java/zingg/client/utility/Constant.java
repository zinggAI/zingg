package zingg.client.utility;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import zingg.client.schema.Schema;
import zingg.client.schema.SchemaWithMixedDataType;
import zingg.common.client.util.ColName;
import zingg.spark.client.SparkFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static zingg.client.TestSparkDataFrame.setUpSpark;

public class Constant {
	public static final String STR_RECID = "recid";
	public static final String STR_GIVENNAME = "givenname";
	public static final String STR_SURNAME = "surname";
	public static final String STR_COST = "cost";
	public static final String STR_POSTCODE = "postcode";
	public static final String STR_SUBURB = "suburb";

	public static List<Schema> createSampleDataList() {
		List<Schema> sample = new ArrayList<Schema>();
		sample.add(new Schema("07317257", "erjc", "henson", "hendersonville", "2873g"));
		sample.add(new Schema("03102490", "jhon", "kozak", "henders0nville", "28792"));
		sample.add(new Schema("02890805", "david", "pisczek", "durham", "27717"));
		sample.add(new Schema("04437063", "e5in", "bbrown", "greenville", "27858"));
		sample.add(new Schema("03211564", "susan", "jones", "greenjboro", "274o7"));
		
		sample.add(new Schema("04155808", "jerome", "wilkins", "battleborn", "2780g"));
		sample.add(new Schema("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
		sample.add(new Schema("06087743", "william", "craven", "greenshoro", "27405"));
		sample.add(new Schema("00538491", "marh", "jackdon", "greensboro", "27406"));
		sample.add(new Schema("01306702", "vonnell", "palmer", "siler sity", "273q4"));

		return sample;
	}

	public static List<SchemaWithMixedDataType> createSampleDataListWithMixedDataType() {
		List<SchemaWithMixedDataType> sample = new ArrayList<SchemaWithMixedDataType>();
		sample.add(new SchemaWithMixedDataType(7317257, "erjc", "henson", 10.021, 2873));
		sample.add(new SchemaWithMixedDataType(3102490, "jhon", "kozak", 3.2434, 28792));
		sample.add(new SchemaWithMixedDataType(2890805, "david", "pisczek", 5436.0232, 27717));
		sample.add(new SchemaWithMixedDataType(4437063, "e5in", "bbrown", 67.0, 27858));
		sample.add(new SchemaWithMixedDataType(3211564, "susan", "jones", 7343.2324, 2747));

		sample.add(new SchemaWithMixedDataType(4155808, "jerome", "wilkins", 50.34, 2780));
		sample.add(new SchemaWithMixedDataType(5723231, "clarinw", "pastoreus", 87.2323, 27909));
		sample.add(new SchemaWithMixedDataType(6087743, "william", "craven", 834.123, 27405));
		sample.add(new SchemaWithMixedDataType(538491, "marh", "jackdon", 123.123, 27406));
		sample.add(new SchemaWithMixedDataType(1306702, "vonnell", "palmer", 83.123, 2734));

		return sample;
	}

	public static Dataset<Row> createSampleDataset(SparkSession spark) {

		if (spark==null) {
			setUpSpark();
		}

		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField("recid", DataTypes.StringType, false, Metadata.empty()),
				new StructField("givenname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("surname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("suburb", DataTypes.StringType, false, Metadata.empty()),
				new StructField("postcode", DataTypes.StringType, false, Metadata.empty())
		});

		Dataset<Row> sample = spark.createDataFrame(Arrays.asList(
				RowFactory.create("07317257", "erjc", "henson", "hendersonville", "2873g"),
				RowFactory.create("03102490", "jhon", "kozak", "henders0nville", "28792"),
				RowFactory.create("02890805", "david", "pisczek", "durham", "27717"),
				RowFactory.create("04437063", "e5in", "bbrown", "greenville", "27858"),
				RowFactory.create("03211564", "susan", "jones", "greenjboro", "274o7"),
				RowFactory.create("04155808", "jerome", "wilkins", "battleborn", "2780g"),
				RowFactory.create("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"),
				RowFactory.create("06087743", "william", "craven", "greenshoro", "27405"),
				RowFactory.create("00538491", "marh", "jackdon", "greensboro", "27406"),
				RowFactory.create("01306702", "vonnell", "palmer", "siler sity", "273q4")), schemaOfSample);

		return sample;
	}

	public static Dataset<Row> createSampleDatasetHavingMixedDataTypes(SparkSession spark) {
		if (spark==null) {
			setUpSpark();
		}

		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField(STR_RECID, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(STR_GIVENNAME, DataTypes.StringType, false, Metadata.empty()),
				new StructField(STR_SURNAME, DataTypes.StringType, false, Metadata.empty()),
				new StructField(STR_COST, DataTypes.DoubleType, false, Metadata.empty()),
				new StructField(STR_POSTCODE, DataTypes.IntegerType, false, Metadata.empty())
		});

		Dataset<Row> sample = spark.createDataFrame(Arrays.asList(
				RowFactory.create(7317, "erjc", "henson", 0.54, 2873),
				RowFactory.create(3102, "jhon", "kozak", 99.009, 28792),
				RowFactory.create(2890, "david", "pisczek", 58.456, 27717),
				RowFactory.create(4437, "e5in", "bbrown", 128.45, 27858)
		), schemaOfSample);

		return sample;
	}

	public static SparkFrame getZScoreDF(SparkSession spark) {
		Row[] rows = {
				RowFactory.create( 0,100,900),
				RowFactory.create( 1,100,1001),
				RowFactory.create( 1,100,1002),
				RowFactory.create( 1,100,2001),
				RowFactory.create( 1,100,2002),
				RowFactory.create( 11,100,9002),
				RowFactory.create( 3,300,3001),
				RowFactory.create( 3,300,3002),
				RowFactory.create( 3,400,4001),
				RowFactory.create( 4,400,4002)
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.CLUSTER_COLUMN, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SCORE_COL, DataTypes.IntegerType, false, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}

	public static SparkFrame getInputData(SparkSession spark) {
		Row[] rows = {
				RowFactory.create( 1,"fname1","b"),
				RowFactory.create( 2,"fname","a"),
				RowFactory.create( 3,"fna","b"),
				RowFactory.create( 4,"x","c"),
				RowFactory.create( 5,"y","c"),
				RowFactory.create( 11,"new1","b"),
				RowFactory.create( 22,"new12","a"),
				RowFactory.create( 33,"new13","b"),
				RowFactory.create( 44,"new14","c"),
				RowFactory.create( 55,"new15","c")
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField("fname", DataTypes.StringType, false, Metadata.empty()),
				new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}


	public static SparkFrame getClusterData(SparkSession spark) {
		Row[] rows = {
				RowFactory.create( 1,100,1001,"b"),
				RowFactory.create( 2,100,1002,"a"),
				RowFactory.create( 3,100,2001,"b"),
				RowFactory.create( 4,900,2002,"c"),
				RowFactory.create( 5,111,9002,"c")
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.CLUSTER_COLUMN, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SCORE_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}

	public static SparkFrame getClusterDataWithNull(SparkSession spark) {
		Row[] rows = {
				RowFactory.create( 1,100,1001,"b"),
				RowFactory.create( 2,100,1002,"a"),
				RowFactory.create( 3,100,2001,null),
				RowFactory.create( 4,900,2002,"c"),
				RowFactory.create( 5,111,9002,null)
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.COL_PREFIX+ ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.CLUSTER_COLUMN, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SCORE_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SOURCE_COL, DataTypes.StringType, true, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}
}
