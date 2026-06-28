package zingg.spark.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.core.feature.Feature;
import zingg.common.core.model.ModelColumnHelper;
import zingg.common.core.similarity.function.SimFunction;
import zingg.spark.core.similarity.SparkSimFunction;
import zingg.spark.core.similarity.SparkTransformer;

public class SparkFeatureCreators implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkFeatureCreators.class);

	private List<SparkTransformer> transformers;

	public SparkFeatureCreators(Map<FieldDefinition, Feature<DataType>> featurers, ModelColumnHelper columnHelper) {
		transformers = new ArrayList<>();
		int count = 0;
		for (FieldDefinition fd : featurers.keySet()) {
			Feature fea = featurers.get(fd);
			List<SimFunction> sfList = fea.getSimFunctions();
			for (SimFunction sf : sfList) {
				String outputCol = columnHelper.getColumnName(fd.fieldName, sf.getName(), count);
				columnHelper.getColumnsAdded().add(outputCol);
				transformers.add(new SparkTransformer(fd.fieldName, new SparkSimFunction(sf), outputCol));
				count++;
			}
		}
	}

	public void register(SparkSession session) {
		for (SparkTransformer t : transformers) {
			t.register(session);
		}
	}

	public List<SparkTransformer> getTransformers() {
		return transformers;
	}
}
