package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.executor.LabelUpdater;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;
import org.apache.spark.sql.SparkSession;
	import zingg.spark.core.preprocess.ISparkPreprocMapSupplier;


/**
 * Spark specific implementation of LabelUpdater
 * 
 *
 */
public class SparkLabelUpdater extends LabelUpdater<SparkSession, Dataset<Row>, Row, Column,DataType> implements ISparkPreprocMapSupplier {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkLabelUpdater";
	public static final Log LOG = LogFactory.getLog(SparkLabelUpdater.class);

	public SparkLabelUpdater() {
		this(new ZinggSparkContext());
	}

	@Override
	protected DFObjectUtil<SparkSession, Dataset<Row>, Row, Column> getDfObjectUtil() {
		IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
		iWithSession.setSession(getContext().getSession());
		return new SparkDFObjectUtil(iWithSession);
	}

	public SparkLabelUpdater(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.UPDATE_LABEL);
		setContext(sparkContext);
	}

    @Override
    public void init(IZArgs args, SparkSession s, ClientOptions options)  throws ZinggClientException {
        super.init(args,s,options);
        getContext().init(s);
    }
    	
    public Pipe setSaveModeOnPipe(Pipe p) {
		p.setMode(SaveMode.Overwrite.toString());
		return p;
	}
}
	

	

