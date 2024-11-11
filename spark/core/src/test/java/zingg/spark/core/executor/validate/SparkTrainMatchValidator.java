package zingg.spark.core.executor.validate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.validate.TrainMatchValidator;
import zingg.common.core.executor.validate.TrainerValidator;
import zingg.common.core.executor.TrainMatcher;
import zingg.common.core.executor.Trainer;

public class SparkTrainMatchValidator extends TrainMatchValidator<SparkSession,Dataset<Row>,Row,Column,DataType> {
    
    public static final Log LOG = LogFactory.getLog(SparkTrainMatchValidator.class);
	SparkTrainerValidator stv;
	
	public SparkTrainMatchValidator(TrainMatcher<SparkSession,Dataset<Row>,Row,Column,DataType> executor) {
		super(executor);
		stv = new SparkTrainerValidator(executor.getTrainer());
	}

    @Override
	public void validateResults() throws ZinggClientException {
		stv.validateResults();
	}

	@Override
	protected TrainerValidator<SparkSession, Dataset<Row>, Row, Column, DataType> getTrainerValidator(Trainer<SparkSession, Dataset<Row>, Row, Column, DataType> trainer) {
		return new SparkTrainerValidator(trainer);
	}

}
