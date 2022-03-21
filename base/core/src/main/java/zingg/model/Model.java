package zingg.model;

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
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.FieldDefinition;
import zingg.feature.Feature;
import zingg.similarity.function.BaseSimilarityFunction;
import zingg.client.util.ColName;

public class Model implements Serializable {
	
	public static final Log LOG = LogFactory.getLog(Model.class);
	public static final Log DbLOG = LogFactory.getLog("WEB");
	//private Map<FieldDefinition, Feature> featurers;
	List<PipelineStage> pipelineStage;
	List<BaseSimilarityFunction> featureCreators; 
	LogisticRegression lr;
	Transformer transformer;
	BinaryClassificationEvaluator binaryClassificationEvaluator;
	List<String> columnsAdded;
	VectorValueExtractor vve;
	
	public Model(Map<FieldDefinition, Feature> f) {
		featureCreators = new ArrayList<BaseSimilarityFunction>();
		pipelineStage = new ArrayList<PipelineStage> ();
		columnsAdded = new ArrayList<String> ();
		int count = 0;
		for (FieldDefinition fd : f.keySet()) {
			Feature fea = f.get(fd);
			List<BaseSimilarityFunction> sfList = fea.getSimFunctions();
			for (BaseSimilarityFunction sf : sfList) {
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
	
	
	public void register(SparkSession spark) {
		if (featureCreators != null) {
			for (BaseSimilarityFunction bsf: featureCreators) {
				bsf.register(spark);
			}
		}
		vve.register(spark);
		
	}
	
	public static double[] getGrid(double begin, double end, double jump, boolean isMultiple) {
		List<Double> alphaList = new ArrayList<Double>();
		if (isMultiple) {
			for (double alpha =begin; alpha <= end; alpha *= jump) {
				alphaList.add(alpha);
			}
		}
		else {
			for (double alpha =begin; alpha <= end; alpha += jump) {
				alphaList.add(alpha);
			}
		}
		double[] retArr = new double[alphaList.size()];
		for (int i=0; i < alphaList.size(); ++i) {
			retArr[i] = alphaList.get(i);
		}
		return retArr;
	}
	
	public void fit(Dataset<Row> pos, Dataset<Row> neg) {
		//transform
		Dataset<Row> input = transform(pos.union(neg)).coalesce(1).cache();
		if (LOG.isDebugEnabled()) input.write().csv("/tmp/input/" + System.currentTimeMillis());
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
		CrossValidatorModel cvModel = cv.fit(input);
		transformer = cvModel;
		LOG.debug("threshold after fitting is " + lr.getThreshold());
	}
	
	
	public void load(String path) {
		transformer =  CrossValidatorModel.load(path);
	}
	
	
	public Dataset<Row> predict(Dataset<Row> data) {
		return predict(data, true);
	}
	
	public Dataset<Row> predict(Dataset<Row> data, boolean isDrop) {
		//create features
		LOG.info("threshold while predicting is " + lr.getThreshold());
		//lr.setThreshold(0.95);
		//LOG.info("new threshold while predicting is " + lr.getThreshold());
		
		Dataset<Row> predictWithFeatures = transformer.transform(transform(data));
		LOG.debug(predictWithFeatures.schema());
		predictWithFeatures = vve.transform(predictWithFeatures);
		LOG.debug("Original schema is " + predictWithFeatures.schema());
		if (isDrop) {
			Dataset<Row> returnDS = predictWithFeatures.drop(columnsAdded.toArray(new String[columnsAdded.size()]));
			LOG.debug("Return schema after dropping additional columns is " + returnDS.schema());
			return returnDS;
		}
		LOG.debug("Return schema is " + predictWithFeatures.schema());
		return predictWithFeatures;
		
	}

	public void save(String path) throws IOException{
		((CrossValidatorModel) transformer).write().overwrite().save(path);
	}
	
	public Dataset<Row> transform(Dataset<Row> input) {
		for (BaseSimilarityFunction bsf: featureCreators) {
			input = bsf.transform(input);
		}
		return input; //.cache();
	}
	
}
