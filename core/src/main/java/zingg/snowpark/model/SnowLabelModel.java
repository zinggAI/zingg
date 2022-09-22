package zingg.snowpark.model;

import java.io.IOException;
import java.util.Map;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Column;

import zingg.client.FieldDefinition;
import zingg.client.ZFrame;
import zingg.feature.Feature;

// need Snow specific ml library
public class SnowLabelModel extends SnowModel{
	
	public SnowLabelModel(Map<FieldDefinition, Feature> f) {
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override	
	public void fit(ZFrame<DataFrame,Row,Column> pos, ZFrame<DataFrame,Row,Column> neg) {
		//create features
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[pipelineStage.size()]));
		PipelineModel pm = pipeline.fit(transform(pos.union(neg)).df().coalesce(1).cache());
		transformer = pm;	
	}
	
	
	@Override
	public void save(String path) throws IOException{
		((PipelineModel) transformer).write().overwrite().save(path);
	}


}
