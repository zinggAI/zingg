package zingg.spark.core.executor.blockingverifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import static org.apache.spark.sql.functions.*;

import zingg.common.client.ClientOptions;
import zingg.common.client.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.executor.blockingverifier.IVerifyBlockingPipes;
import zingg.common.core.executor.blockingverifier.VerifyBlocking;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.common.client.util.ColName;
import zingg.spark.core.preprocess.ISparkPreprocMapSupplier;

public class SparkVerifyBlocker extends VerifyBlocking<SparkSession,Dataset<Row>,Row,Column,DataType> implements ISparkPreprocMapSupplier{

    private static final long serialVersionUID = 1L;
	public static String name = SparkVerifyBlocker.class.getName();
	public static final Log LOG = LogFactory.getLog(SparkVerifyBlocker.class);    

    public SparkVerifyBlocker() {
        this(new ZinggSparkContext());
    }

    public SparkVerifyBlocker(ZinggSparkContext sparkContext) {
        setZinggOption(ZinggOptions.VERIFY_BLOCKING);
		setContext(sparkContext);
    }

    @Override
    public void init(IZArgs args, SparkSession s, ClientOptions options)  throws ZinggClientException {
        super.init(args,s,options);
        getContext().init(s);
    }

    @Override
    public double getNumComparisons(ZFrame<Dataset<Row>, Row, Column> blockCounts){
        ZFrame<Dataset<Row>, Row, Column> result = blockCounts.select(sum(pow(ColName.HASH_COUNTS_COL, 2)).alias("sum_of_squares"));
        return result.collectAsList().get(0).getDouble(0);
    }

    @Override
    public IVerifyBlockingPipes<SparkSession, Dataset<Row>, Row, Column> getVerifyBlockingPipeUtil() {
        if (verifyBlockingPipeUtil == null) {
            this.verifyBlockingPipeUtil = new SparkVerifyBlockingPipes(getPipeUtil(), timestamp, getModelHelper());
        }
        return verifyBlockingPipeUtil;
    }
    
}
