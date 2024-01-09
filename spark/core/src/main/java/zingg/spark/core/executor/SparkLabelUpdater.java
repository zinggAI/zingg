package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

import zingg.common.client.pipe.Pipe;
import zingg.common.core.executor.LabelUpdater;
import zingg.spark.core.context.ZinggSparkContext;
import org.apache.spark.sql.SparkSession;


/**
 * Spark specific implementation of LabelUpdater
 * 
 *
 */
public class SparkLabelUpdater extends LabelUpdater<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkLabelUpdater";
	public static final Log LOG = LogFactory.getLog(SparkLabelUpdater.class);

	public SparkLabelUpdater() {
		this(new ZinggSparkContext());
	}

	public SparkLabelUpdater(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.UPDATE_LABEL);
		setContext(sparkContext);
	}

    @Override
    public void init(IArguments args)  throws ZinggClientException {
        super.init(args);
        getContext().init();
    }
    	
	protected Pipe setSaveModeOnPipe(Pipe p) {
		p.setMode(SaveMode.Overwrite.toString());
		return p;
	}
}
	

	

