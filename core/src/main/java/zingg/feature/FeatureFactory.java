package zingg.feature;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
public class FeatureFactory implements Serializable {

	public static final Log LOG = LogFactory.getLog(FeatureFactory.class);

	private static Map<DataType, Class> map;

	private static void init() {
		map = new HashMap<DataType, Class>();
		map.put(DataTypes.StringType, StringFeature.class);
		map.put(DataTypes.IntegerType, IntFeature.class);
		map.put(DataTypes.DateType, DateFeature.class);
		map.put(DataTypes.DoubleType, DoubleFeature.class);
	}

	public static Object get(DataType dataType) throws Exception {
		if (map == null)
			init();
		return map.get(dataType).newInstance();
	}

}
