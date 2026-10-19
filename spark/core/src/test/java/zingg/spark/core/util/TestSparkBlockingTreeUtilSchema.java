package zingg.spark.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import zingg.common.client.util.ColName;

public class TestSparkBlockingTreeUtilSchema {

	private final SparkBlockingTreeUtil blockingTreeUtil = new SparkBlockingTreeUtil(null, null);

	private StructType getSchema() {
		return DataTypes.createStructType(new StructField[] {
			DataTypes.createStructField("id", DataTypes.StringType, true),
			DataTypes.createStructField("fname", DataTypes.StringType, true)
		});
	}

	@Test
	public void testAppendHashColAddsNonNullableIntAtEnd() {
		StructType schema = getSchema();
		StructType withHash = blockingTreeUtil.appendHashCol(schema);

		assertEquals(3, withHash.fields().length);
		assertEquals("id", withHash.fields()[0].name());
		assertEquals("fname", withHash.fields()[1].name());
		StructField hash = withHash.fields()[2];
		assertEquals(ColName.HASH_COL, hash.name());
		assertEquals(DataTypes.IntegerType, hash.dataType());
		assertFalse(hash.nullable());
		// input schema is left unchanged
		assertEquals(2, schema.fields().length);
	}
}
