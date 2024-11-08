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

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.Trainer;
import zingg.common.core.executor.validate.TrainerValidator;

public class SparkTrainerValidator extends TrainerValidator<SparkSession,Dataset<Row>,Row,Column,DataType> {
	
	public static final Log LOG = LogFactory.getLog(SparkTrainerValidator.class);

	public SparkTrainerValidator(Trainer<SparkSession,Dataset<Row>,Row,Column,DataType> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
		// check that model is created
		LOG.info("Zingg Model Dir : "+ executor.getArgs().getZinggModelDir());
		
		File modelDir = new File(executor.getArgs().getZinggModelDir());
		assertTrue(modelDir.exists(),"check if model has been created");
	}
	
}
