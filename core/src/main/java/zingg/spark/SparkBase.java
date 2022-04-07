package zingg.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.ZinggBase;
import zingg.client.Arguments;
import zingg.client.IZingg;
import zingg.client.ZinggClientException;
import zingg.spark.util.SparkHashUtil;
import zingg.spark.util.SparkPipeUtil;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.HashUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;
import zingg.spark.util.SparkDSUtil;
import zingg.spark.util.SparkGraphUtil;

public class SparkBase extends ZinggBase<SparkSession, Dataset<Row>, Row,Column,DataType,DataType>{

    JavaSparkContext ctx;
    public static final Log LOG = LogFactory.getLog(SparkBase.class);
    

    @Override
    public void init(Arguments args, String license)
        throws ZinggClientException {
        startTime = System.currentTimeMillis();
        this.args = args;
        try{
            context = SparkSession
                .builder()
                .appName("Zingg"+args.getJobId())
                .getOrCreate();
            ctx = new JavaSparkContext(context.sparkContext());
            JavaSparkContext.jarOfClass(IZingg.class);
            LOG.debug("Context " + ctx.toString());
            initHashFns();
            loadFeatures();
            ctx.setCheckpointDir("/tmp/checkpoint");	
            setPipeUtil(new SparkPipeUtil(context));
            setDSUtil(new SparkDSUtil(context));
            setHashUtil(new SparkHashUtil());
            setGraphUtil(new SparkGraphUtil());
            
        }
        catch(Throwable e) {
            if (LOG.isDebugEnabled()) e.printStackTrace();
            throw new ZinggClientException(e.getMessage());
        }
    }

    protected void initHashFns() throws ZinggClientException {
		try {
			//functions = Util.getFunctionList(this.functionFile);
			hashFunctions = getHashUtil().getHashFunctionList(hashFunctionFile, getContext());
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			throw new ZinggClientException("Unable to initialize base functions");
		}		
	}


    @Override
    public void cleanup() throws ZinggClientException {
        if (ctx != null) ctx.stop();
    }

    @Override
    public void execute() throws ZinggClientException {

    }   

    
    public void setHashUtil(HashUtil<Dataset<Row>, Row, Column, DataType, DataType> t) {
        this.hashUtil = t;
    }

    public void setGraphUtil(GraphUtil<Dataset<Row>, Row, Column> t) {
        this.graphUtil = t;
    }

    
    
    public void setPipeUtil(PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> pipeUtil) {
        this.pipeUtil = pipeUtil;        
    }

   
    public void setDSUtil(DSUtil<SparkSession, Dataset<Row>, Row, Column> pipeUtil) {
       this.dsUtil = pipeUtil;        
    }

    public void setBlokingTreeUtil(BlockingTreeUtil<Dataset<Row>, Row, Column, DataType, DataType> d) {
        this.blockingTreeUtil = d;
    }

    public void setModelUtil(ModelUtil<SparkSession, Dataset<Row>, Row, Column> t) {
        this.modelUtil = t;
    }

    public ModelUtil<SparkSession, Dataset<Row>, Row, Column>  getModelUtil() {
        return modelUtil;
    }
  
 }