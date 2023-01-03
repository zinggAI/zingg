package zingg.snowpark.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.PolynomialExpansion;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.types.DataType;

import zingg.client.FieldDefinition;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.feature.Feature;
import zingg.model.Model;
import zingg.similarity.function.SimFunction;
import zingg.client.util.ColName;


//need Snow specific ml functions
public class SnowModel extends Model<Session, DataType, DataFrame, Row, Column>{
	
	public static final Log LOG = LogFactory.getLog(SnowModel.class);
	public static final Log DbLOG = LogFactory.getLog("WEB");
	//private Map<FieldDefinition, Feature> featurers;
	List<PipelineStage> pipelineStage;
	List<SimFunction> featureCreators; 
	LogisticRegression lr;
	Transformer transformer;
	BinaryClassificationEvaluator binaryClassificationEvaluator;
	List<String> columnsAdded;
	VectorValueExtractor vve;
	
	public SnowModel(Map<FieldDefinition, Feature> f) {
		featureCreators = new ArrayList<SimFunction>();
		pipelineStage = new ArrayList<PipelineStage> ();
		columnsAdded = new ArrayList<String> ();
		int count = 0;
		for (FieldDefinition fd : f.keySet()) {
			Feature fea = f.get(fd);
			List<SimFunction> sfList = fea.getSimFunctions();
			for (SimFunction sf : sfList) {
				sf.setInputCol(fd.fieldName);
				String outputCol = ColName.SIM_COL + count;
				columnsAdded.add(outputCol);	
				sf.setOutputCol(outputCol);
				count++;
				//pipelineStage.add(sf);
				featureCreators.add(sf);
			}
		}
		
		VectorAssembler assembler = new VectorAssembler();
		assembler.setInputCols(columnsAdded.toArray(new String[columnsAdded.size()]));
		assembler.setOutputCol(ColName.FEATURE_VECTOR_COL);
		columnsAdded.add(ColName.FEATURE_VECTOR_COL);
		pipelineStage.add(assembler);
		PolynomialExpansion polyExpansion = new PolynomialExpansion()
		  .setInputCol(ColName.FEATURE_VECTOR_COL)
		  .setOutputCol(ColName.FEATURE_COL)
		  .setDegree(3);	
		columnsAdded.add(ColName.FEATURE_COL);
		pipelineStage.add(polyExpansion);
		lr = new LogisticRegression();
		lr.setMaxIter(100);
		lr.setFeaturesCol(ColName.FEATURE_COL);
		lr.setLabelCol(ColName.MATCH_FLAG_COL);
		lr.setProbabilityCol(ColName.PROBABILITY_COL);
		lr.setPredictionCol(ColName.PREDICTION_COL);
		lr.setFitIntercept(true);
		pipelineStage.add(lr);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																					
		vve = new VectorValueExtractor();
		vve.setInputCol(ColName.PROBABILITY_COL);
		vve.setOutputCol(ColName.SCORE_COL);
		//pipelineStage.add(vve);
		columnsAdded.add(ColName.PROBABILITY_COL);	
		columnsAdded.add(ColName.RAW_PREDICTION);	
	}
	
	
	
	public void fit(ZFrame<DataFrame,Row,Column> pos, ZFrame<DataFrame,Row,Column> neg) {
		//transform
		ZFrame<DataFrame,Row,Column> input = transform(pos.union(neg)).coalesce(1).cache();
		//if (LOG.isDebugEnabled()) input.write().csv("/tmp/input/" + System.currentTimeMillis());
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[pipelineStage.size()]));
		
		LOG.debug("Pipeline is " + pipeline);
		//create lr params
		ParamMap[] paramGrid = new ParamGridBuilder()
		  .addGrid(lr.regParam(), getGrid(0.0001, 1, 10, true))
		  .addGrid(lr.threshold(), getGrid(0.40, 0.55, 0.05, false))
		  .build();
		
		binaryClassificationEvaluator = new BinaryClassificationEvaluator();
		binaryClassificationEvaluator.setLabelCol(ColName.MATCH_FLAG_COL);
		CrossValidator cv = new CrossValidator()
		  .setEstimator(pipeline)
		  .setEvaluator(binaryClassificationEvaluator)
		  .setEstimatorParamMaps(paramGrid)
		  .setNumFolds(2);  // Use 3+ in practice
		  //.setParallelism(2);
		CrossValidatorModel cvModel = cv.fit(input.df());
		transformer = cvModel;
		LOG.debug("threshold after fitting is " + lr.getThreshold());
	}
	
	
	public void load(String path) {
		transformer =  CrossValidatorModel.load(path);
	}
	
	
	public ZFrame<DataFrame,Row,Column> predict(ZFrame<DataFrame,Row,Column> data) {
		return predict(data, true);
	}
	
	@Override
	public ZFrame<DataFrame,Row,Column> predict(ZFrame<DataFrame,Row,Column> data, boolean isDrop) {
		//create features
		LOG.info("threshold while predicting is " + lr.getThreshold());
		//lr.setThreshold(0.95);
		//LOG.info("new threshold while predicting is " + lr.getThreshold());
		
		DataFrame predictWithFeatures = transformer.transform(transform(data).df());
		//LOG.debug(predictWithFeatures.schema());
		predictWithFeatures = vve.transform(predictWithFeatures);
		//LOG.debug("Original schema is " + predictWithFeatures.schema());
		if (isDrop) {
			DataFrame returnDS = predictWithFeatures.drop(columnsAdded.toArray(new String[columnsAdded.size()]));
			//LOG.debug("Return schema after dropping additional columns is " + returnDS.schema());
			return new SnowFrame(returnDS);
		}
		LOG.debug("Return schema is " + predictWithFeatures.schema());
		return new SnowFrame(predictWithFeatures);
		
	}

	public void save(String path) throws IOException{
		((CrossValidatorModel) transformer).write().overwrite().save(path);
	}

	public ZFrame<DataFrame,Row,Column> transform(DataFrame input) {
		for (SimFunction bsf: featureCreators) {
			input = bsf.transform(input);
		}
		return new SnowFrame(input); //.cache();
	}
	
	public ZFrame<DataFrame,Row,Column> transform(ZFrame<DataFrame,Row,Column> i) {
		return transform(i.df());
	}



	@Override
	public void register(Session snow) {
		if (featureCreators != null) {
			for (SimFunction bsf: featureCreators) {
				bsf.register(snow);
			}
		}
		vve.register(snow);
		
	}
	
}
