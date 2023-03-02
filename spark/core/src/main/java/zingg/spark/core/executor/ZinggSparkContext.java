package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.IZingg;
import zingg.common.client.ZinggClientException;
import zingg.common.core.Context;
import zingg.common.core.executor.ZinggBase;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkDSUtil;
import zingg.spark.core.util.SparkGraphUtil;
import zingg.spark.core.util.SparkHashUtil;
import zingg.spark.core.util.SparkModelUtil;
import zingg.spark.core.util.SparkPipeUtil;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.DSUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;
import zingg.common.core.util.PipeUtilBase;


public class ZinggSparkContext implements Context<SparkSession, Dataset<Row>, Row,Column,DataType>{

    
    protected JavaSparkContext ctx;
	protected SparkSession spark;
    protected PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> pipeUtil;
    protected HashUtil<SparkSession,Dataset<Row>, Row, Column, DataType> hashUtil;
    protected DSUtil<SparkSession, Dataset<Row>, Row, Column> dsUtil;
    protected GraphUtil<Dataset<Row>, Row, Column> graphUtil;
    protected ModelUtil<SparkSession, DataType, Dataset<Row>, Row, Column> modelUtil;
    protected BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType> blockingTreeUtil;

	public static final String hashFunctionFile = "hashFunctions.json";
    

    public static final Log LOG = LogFactory.getLog(ZinggSparkContext.class);

    
    

    public SparkSession getSession() {
        return spark;
    }



    @Override
    public void init(String license)
        throws ZinggClientException {
        try{
            spark = SparkSession
                .builder()
                .appName("Zingg")
                .getOrCreate();
            ctx = new JavaSparkContext(spark.sparkContext());
            JavaSparkContext.jarOfClass(IZingg.class);
            LOG.debug("Context " + ctx.toString());
            //initHashFns();
            
            ctx.setCheckpointDir("/tmp/checkpoint");	
            setPipeUtil(new SparkPipeUtil(spark));
            setDSUtil(new SparkDSUtil(spark));
            setHashUtil(new SparkHashUtil(spark));
            setGraphUtil(new SparkGraphUtil());
            setModelUtil(new SparkModelUtil(spark));
            setBlockingTreeUtil(new SparkBlockingTreeUtil(spark, getPipeUtil()));
        }
        catch(Throwable e) {
            if (LOG.isDebugEnabled()) e.printStackTrace();
            throw new ZinggClientException(e.getMessage());
        }
    }

    /** 
    public void initHashFns() throws ZinggClientException {
		try {
			//functions = Util.getFunctionList(this.functionFile);
			hashFunctions = getHashUtil().getHashFunctionList(hashFunctionFile, getContext());
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			throw new ZinggClientException("Unable to initialize base functions");
		}		
	}
    */


    
    public void setHashUtil(HashUtil<SparkSession,Dataset<Row>, Row, Column, DataType> t) {
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

    public void setBlockingTreeUtil(BlockingTreeUtil<SparkSession,Dataset<Row>, Row, Column, DataType> d) {
        this.blockingTreeUtil = d;
    }

    public void setModelUtil(ModelUtil<SparkSession, DataType, Dataset<Row>, Row, Column>  t) {
        this.modelUtil = t;
    }

    public ModelUtil<SparkSession, DataType, Dataset<Row>, Row, Column>   getModelUtil() {
        return modelUtil;
    }

   /*  @Override
    public void setSession(SparkSession session) {
        this.spark = session;        
    }
    */

    @Override
    public HashUtil<SparkSession,Dataset<Row>, Row, Column, DataType> getHashUtil() {
        return hashUtil;
    }

    @Override
    public GraphUtil<Dataset<Row>, Row, Column> getGraphUtil() {
        return graphUtil;
    }

    @Override
    public DSUtil<SparkSession, Dataset<Row>, Row, Column> getDSUtil() {
         return dsUtil;
    }

    @Override
    public PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> getPipeUtil() {
        return pipeUtil;
    }

    @Override
    public BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType> getBlockingTreeUtil() {
        return blockingTreeUtil;
    }
  
 }