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
import zingg.spark.client.SparkFrame;
import zingg.spark.core.similarity.SparkTransformer;

public class SparkMLPipeline implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkMLPipeline.class);

	protected SparkFeatureCreators featureCreators;
	private List<PipelineStage> pipelineStage;
	private LogisticRegression lr;
	private Transformer transformer;
	private VectorValueExtractor vve;
	private ModelColumnHelper columnHelper;

	public SparkMLPipeline() {
	}

	public SparkMLPipeline(Map<FieldDefinition, Feature<DataType>> featurers, ModelColumnHelper columnHelper) {
		this.columnHelper = columnHelper;
		buildPipeline(featurers, columnHelper);
	}

	public void buildPipeline(Map<FieldDefinition, Feature<DataType>> featurers, ModelColumnHelper columnHelper) {
		pipelineStage = new ArrayList<>();

		featureCreators = new SparkFeatureCreators(featurers, columnHelper);
		pipelineStage.addAll(featureCreators.getTransformers());

		pipelineStage.add(getAssembler());
		pipelineStage.add(getPolyExpansion());
		pipelineStage.add(getLR());

		vve = new VectorValueExtractor(ColName.PROBABILITY_COL, ColName.SCORE_COL);
		columnHelper.getColumnsAdded().add(ColName.PROBABILITY_COL);
		columnHelper.getColumnsAdded().add(ColName.RAW_PREDICTION);
	}

	protected VectorAssembler getAssembler() {
		VectorAssembler assembler = new VectorAssembler();
		assembler.setInputCols(columnHelper.getColumnsAdded().toArray(new String[0]))
			.setOutputCol(ColName.FEATURE_VECTOR_COL);
		columnHelper.getColumnsAdded().add(ColName.FEATURE_VECTOR_COL);
		return assembler;
	}

	protected PolynomialExpansion getPolyExpansion() {
		PolynomialExpansion polyExpansion = new PolynomialExpansion()
			.setInputCol(ColName.FEATURE_VECTOR_COL)
			.setOutputCol(ColName.FEATURE_COL)
			.setDegree(3);
		columnHelper.getColumnsAdded().add(ColName.FEATURE_COL);
		return polyExpansion;
	}

	protected LogisticRegression getLR() {
		lr = new LogisticRegression();
		lr.setMaxIter(100);
		lr.setFeaturesCol(ColName.FEATURE_COL);
		lr.setLabelCol(ColName.MATCH_FLAG_COL);
		lr.setProbabilityCol(ColName.PROBABILITY_COL);
		lr.setPredictionCol(ColName.PREDICTION_COL);
		lr.setFitIntercept(true);
		return lr;
	}

	public ZFrame<Dataset<Row>, Row, Column> fit(ZFrame<Dataset<Row>, Row, Column> input) {
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[0]));
		LOG.debug("Pipeline is " + pipeline);

		ParamMap[] paramGrid = new ParamGridBuilder()
			.addGrid(lr.regParam(), ModelGrid.getGrid(0.0001, 1, 10, true))
			.addGrid(lr.threshold(), ModelGrid.getGrid(0.40, 0.55, 0.05, false))
			.build();

		BinaryClassificationEvaluator bce = new BinaryClassificationEvaluator();
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

	public ZFrame<Dataset<Row>, Row, Column> predict(ZFrame<Dataset<Row>, Row, Column> data) {
		LOG.info("threshold while predicting is " + lr.getThreshold());
		Dataset<Row> predictWithFeatures = transformer.transform(data.df());
		predictWithFeatures = vve.transform(predictWithFeatures);
		LOG.debug("Return schema is " + predictWithFeatures.schema());
		return new SparkFrame(predictWithFeatures);
	}

	public void register(SparkSession session) {
		featureCreators.register(session);
		vve.register(session);
	}

	public void load(String path) {
		transformer = CrossValidatorModel.load(path);
	}

	public void save(String path) throws IOException {
		((CrossValidatorModel) transformer).write().overwrite().save(path);
	}

	public List<SparkTransformer> getFeatureCreators() {
		return featureCreators.getTransformers();
	}
}
