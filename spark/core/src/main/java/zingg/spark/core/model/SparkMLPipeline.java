package zingg.spark.core.model;

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
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.core.feature.Feature;
import zingg.common.core.model.ModelColumnHelper;
import zingg.common.core.model.ModelGrid;
import zingg.common.core.similarity.function.SimFunction;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.similarity.SparkSimFunction;
import zingg.spark.core.similarity.SparkTransformer;

public class SparkMLPipeline implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkMLPipeline.class);

	protected List<SparkTransformer> featureCreators;
	private List<PipelineStage> pipelineStage;
	private LogisticRegression lr;
	private Transformer transformer;
	private BinaryClassificationEvaluator bce;
	private VectorValueExtractor vve;

	public SparkMLPipeline(Map<FieldDefinition, Feature<DataType>> featurers, ModelColumnHelper columnHelper) {
		featureCreators = new ArrayList<>();
		pipelineStage = new ArrayList<>();
		int count = 0;
		for (FieldDefinition fd : featurers.keySet()) {
			Feature fea = featurers.get(fd);
			List<SimFunction> sfList = fea.getSimFunctions();
			for (SimFunction sf : sfList) {
				String outputCol = columnHelper.getColumnName(fd.fieldName, sf.getName(), count);
				columnHelper.getColumnsAdded().add(outputCol);
				featureCreators.add(new SparkTransformer(fd.fieldName, new SparkSimFunction(sf), outputCol));
				count++;
			}
		}

		VectorAssembler assembler = new VectorAssembler();
		assembler.setInputCols(columnHelper.getColumnsAdded().toArray(new String[0]));
		assembler.setOutputCol(ColName.FEATURE_VECTOR_COL);
		columnHelper.getColumnsAdded().add(ColName.FEATURE_VECTOR_COL);
		pipelineStage.add(assembler);

		PolynomialExpansion polyExpansion = new PolynomialExpansion()
			.setInputCol(ColName.FEATURE_VECTOR_COL)
			.setOutputCol(ColName.FEATURE_COL)
			.setDegree(3);
		columnHelper.getColumnsAdded().add(ColName.FEATURE_COL);
		pipelineStage.add(polyExpansion);

		lr = new LogisticRegression();
		lr.setMaxIter(100);
		lr.setFeaturesCol(ColName.FEATURE_COL);
		lr.setLabelCol(ColName.MATCH_FLAG_COL);
		lr.setProbabilityCol(ColName.PROBABILITY_COL);
		lr.setPredictionCol(ColName.PREDICTION_COL);
		lr.setFitIntercept(true);
		pipelineStage.add(lr);

		vve = new VectorValueExtractor(ColName.PROBABILITY_COL, ColName.SCORE_COL);
		columnHelper.getColumnsAdded().add(ColName.PROBABILITY_COL);
		columnHelper.getColumnsAdded().add(ColName.RAW_PREDICTION);
	}

	public ZFrame<Dataset<Row>, Row, Column> transformTrainingData(
			ZFrame<Dataset<Row>, Row, Column> pos, ZFrame<Dataset<Row>, Row, Column> neg) {
		return pos.union(neg).coalesce(1).cache();
	}

	public ZFrame<Dataset<Row>, Row, Column> applyFitPipeline(ZFrame<Dataset<Row>, Row, Column> input) {
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[0]));
		LOG.debug("Pipeline is " + pipeline);

		ParamMap[] paramGrid = new ParamGridBuilder()
			.addGrid(lr.regParam(), ModelGrid.getGrid(0.0001, 1, 10, true))
			.addGrid(lr.threshold(), ModelGrid.getGrid(0.40, 0.55, 0.05, false))
			.build();

		bce = new BinaryClassificationEvaluator();
		bce.setLabelCol(ColName.MATCH_FLAG_COL);
		CrossValidator cv = new CrossValidator()
			.setEstimator(pipeline)
			.setEvaluator(bce)
			.setEstimatorParamMaps(paramGrid)
			.setNumFolds(2);

		transformer = cv.fit(input.df());
		LOG.debug("threshold after fitting is " + lr.getThreshold());
		return input;
	}

	public ZFrame<Dataset<Row>, Row, Column> transformAndPredict(ZFrame<Dataset<Row>, Row, Column> data) {
		LOG.info("threshold while predicting is " + lr.getThreshold());
		Dataset<Row> predictWithFeatures = transformer.transform(data.df());
		predictWithFeatures = vve.transform(predictWithFeatures);
		LOG.debug("Return schema is " + predictWithFeatures.schema());
		return new SparkFrame(predictWithFeatures);
	}

	public ZFrame<Dataset<Row>, Row, Column> transform(Dataset<Row> input) {
		for (SparkTransformer bsf : featureCreators) {
			input = bsf.transform(input);
		}
		return new SparkFrame(input);
	}

	public void register(SparkSession session) {
		if (featureCreators != null) {
			for (SparkTransformer bsf : featureCreators) {
				bsf.register(session);
			}
		}
		vve.register(session);
	}

	public void load(String path) {
		transformer = CrossValidatorModel.load(path);
	}

	public void save(String path) throws IOException {
		((CrossValidatorModel) transformer).write().overwrite().save(path);
	}

	public List<SparkTransformer> getFeatureCreators() {
		return featureCreators;
	}
}
