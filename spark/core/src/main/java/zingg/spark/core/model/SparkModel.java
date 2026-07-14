package zingg.spark.core.model;

import java.io.IOException;
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
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.core.feature.Feature;
import zingg.common.core.model.Model;
import zingg.common.core.similarity.function.SimFunction;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.similarity.SparkSimFunction;
import zingg.spark.core.similarity.SparkTransformer;

public class SparkModel extends Model<SparkSession, Dataset<Row>, Row, Column, DataType>{
	
	public static final Log LOG = LogFactory.getLog(SparkModel.class);
	//private Map<FieldDefinition, Feature> featurers;
	List<PipelineStage> pipelineStage;
	List<SparkTransformer> featureCreators; 
	LogisticRegression lr;
	Transformer transformer;
	BinaryClassificationEvaluator binaryClassificationEvaluator;
	
	VectorValueExtractor vve;
	
	public SparkModel(SparkSession s, Map<FieldDefinition, Feature<DataType>> f) {
		super(s);
		featureCreators = new ArrayList<SparkTransformer>();
		pipelineStage = new ArrayList<PipelineStage> ();
		int count = 0;
		for (FieldDefinition fd : f.keySet()) {
			Feature fea = f.get(fd);
			List<SimFunction> sfList = fea.getSimFunctions();
			for (SimFunction sf : sfList) {
				String outputCol = getColumnName(fd.fieldName, sf.getName(), count);
				columnsAdded.add(outputCol);	
				SparkTransformer st = new SparkTransformer(fd.fieldName, new SparkSimFunction(sf), outputCol);
				count++;
				//pipelineStage.add(sf);
				featureCreators.add(st);
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
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																					
		vve = new VectorValueExtractor(ColName.PROBABILITY_COL,ColName.SCORE_COL);
		//vve.setInputCol(ColName.PROBABILITY_COL);
		//vve.setOutputCol(ColName.SCORE_COL);
		//pipelineStage.add(vve);
		columnsAdded.add(ColName.PROBABILITY_COL);	
		columnsAdded.add(ColName.RAW_PREDICTION);	
	}
	
	@Override
	public void fit(ZFrame<Dataset<Row>,Row,Column> pos, ZFrame<Dataset<Row>,Row,Column> neg) {
		fitCore(pos, neg);
	}

	public ZFrame<Dataset<Row>,Row,Column> transformTrainingData(ZFrame<Dataset<Row>,Row,Column> pos, ZFrame<Dataset<Row>,Row,Column> neg) {
		return pos.union(neg).coalesce(1).cache();
	}

	public ZFrame<Dataset<Row>,Row,Column> applyFitPipeline(ZFrame<Dataset<Row>,Row,Column> input) {
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
		return input;
	}
	
	public ZFrame<Dataset<Row>,Row,Column> fitCore(ZFrame<Dataset<Row>,Row,Column> pos, ZFrame<Dataset<Row>,Row,Column> neg) {
		//transform
		ZFrame<Dataset<Row>,Row,Column> input = transform(transformTrainingData(pos, neg));
		return applyFitPipeline(input);
	}
	
	
	public void load(String path) {
		transformer =  CrossValidatorModel.load(path);
	}
	
	public ZFrame<Dataset<Row>,Row,Column> predict(ZFrame<Dataset<Row>,Row,Column> data) {
		return predict(data, true);
	}
	
	@Override
	public ZFrame<Dataset<Row>,Row,Column> predict(ZFrame<Dataset<Row>,Row,Column> data, boolean isDrop) {
		return dropFeatureCols(predictCore(data), isDrop);
	}

	@Override
    public ZFrame<Dataset<Row>,Row,Column> predictCore(ZFrame<Dataset<Row>,Row,Column> data) {
		//create features
		LOG.info("threshold while predicting is " + lr.getThreshold());
		return transformAndPredict(transform(data));
	}

	public ZFrame<Dataset<Row>,Row,Column> transformAndPredict(ZFrame<Dataset<Row>,Row,Column> data) {
		Dataset<Row> predictWithFeatures = transformer.transform(data.df());
		//LOG.debug(predictWithFeatures.schema());
		predictWithFeatures = vve.transform(predictWithFeatures);
		//LOG.debug("Original schema is " + predictWithFeatures.schema());
		
		LOG.debug("Return schema is " + predictWithFeatures.schema());
		return new SparkFrame(predictWithFeatures);
	}

	public void save(String path) throws IOException{
		((CrossValidatorModel) transformer).write().overwrite().save(path);
	}

	public ZFrame<Dataset<Row>,Row,Column> transform(Dataset<Row> input) {
		for (SparkTransformer bsf: featureCreators) {
			input = bsf.transform(input);
		}
		return new SparkFrame(input); //.cache();
	}
	
	public ZFrame<Dataset<Row>,Row,Column> transform(ZFrame<Dataset<Row>,Row,Column> i) {
		return transform(i.df());
	}

	public List<SparkTransformer> getFeatureCreators() {
		return featureCreators;
	}



	@Override
	public void register() {
		if (featureCreators != null) {
			for (SparkTransformer bsf: featureCreators) {
				bsf.register(session);
			}
		}
		vve.register(session);
		
	}
	
}
