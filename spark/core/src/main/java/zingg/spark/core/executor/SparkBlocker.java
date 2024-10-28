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
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;
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

    @Override
    public Pipe<Dataset<Row>, Row, Column> getPipeForVerifyBlockingLocation(IArguments args, PipeUtilBase<SparkSession ,Dataset<Row>, Row, Column> pipeUtil, long timestamp, String type){
        Pipe<Dataset<Row>, Row, Column> p = new Pipe<Dataset<Row>, Row, Column>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getName(args,timestamp,type));
		p = pipeUtil.setOverwriteMode(p);
		return p;
    }    

    private String getName(IArguments args, long timestamp, String type){
        return args.getZinggModelDir() + "/blocks/" + timestamp + "/" + type;
    }
    
}
