package zingg.spark.core.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.feature.Feature;
import zingg.common.core.model.Model;

public class SparkModel extends Model<SparkSession, Dataset<Row>, Row, Column, DataType> {

	public static final Log LOG = LogFactory.getLog(SparkModel.class);
	protected SparkMLPipeline pipeline;

	public SparkModel(SparkSession s, Map<FieldDefinition, Feature<DataType>> f) {
		super(s);
		pipeline = new SparkMLPipeline(f, columnHelper);
	}

	@Override
	public ZFrame<Dataset<Row>, Row, Column> fit(ZFrame<Dataset<Row>, Row, Column> pos, ZFrame<Dataset<Row>, Row, Column> neg) {
		ZFrame<Dataset<Row>, Row, Column> training = pos.union(neg).coalesce(1).cache();
		return pipeline.fit(training);
	}

	

	@Override
	public void load(String path) {
		pipeline.load(path);
	}

	@Override
	public ZFrame<Dataset<Row>, Row, Column> predict(ZFrame<Dataset<Row>, Row, Column> data) {
		return predict(data, true);
	}

	@Override
	public ZFrame<Dataset<Row>, Row, Column> predict(ZFrame<Dataset<Row>, Row, Column> data, boolean isDrop) {
		ZFrame<Dataset<Row>, Row, Column> transformedData = pipeline.predict(data);
		return dropFeatureCols(transformedData, isDrop);
	}


	@Override
	public void save(String path) throws IOException {
		pipeline.save(path);
	}

	@Override
	public void register() {
		pipeline.register(session);
	}

}
