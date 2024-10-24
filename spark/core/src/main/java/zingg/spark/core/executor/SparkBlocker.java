package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.executor.verifyblocking.VerifyBlocking;
import zingg.spark.core.context.ZinggSparkContext;

public class SparkBlocker extends VerifyBlocking<SparkSession,Dataset<Row>,Row,Column,DataType> {

    private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkBlocker";
	public static final Log LOG = LogFactory.getLog(SparkBlocker.class);    

    public SparkBlocker() {
        this(new ZinggSparkContext());
    }

    public SparkBlocker(ZinggSparkContext sparkContext) {
        setZinggOption(ZinggOptions.VERIFY_BLOCKING);
		setContext(sparkContext);
    }

    @Override
    public void init(IArguments args, SparkSession s, ClientOptions options)  throws ZinggClientException {
        super.init(args,s,options);
        getContext().init(s);
    }
    
}
