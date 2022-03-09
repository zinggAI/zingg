package zingg.model;

import java.io.IOException;
import java.util.Map;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;

import zingg.client.FieldDefinition;
import zingg.feature.Feature;
import zingg.client.util.ColName;

public class LabelModel extends Model{
	
	public LabelModel(Map<FieldDefinition, Feature> f) {
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override	
	public void fit(DataFrame pos, DataFrame neg) {
		//create features
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[pipelineStage.size()]));
		PipelineModel pm = pipeline.fit(transform(pos.union(neg)).coalesce(1).cacheResult());
		transformer = pm;	
	}
	
	
	@Override
	public void save(String path) throws IOException{
		((PipelineModel) transformer).write().overwrite().save(path);
	}


}
