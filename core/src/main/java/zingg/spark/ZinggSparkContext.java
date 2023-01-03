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
import zingg.common.Context;
import zingg.spark.util.SparkHashUtil;
import zingg.spark.util.SparkModelUtil;
import zingg.spark.util.SparkPipeUtil;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.HashUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;
import zingg.spark.util.SparkBlockingTreeUtil;
import zingg.spark.util.SparkDSUtil;
import zingg.spark.util.SparkGraphUtil;


public class ZinggSparkContext implements Context<SparkSession, Dataset<Row>, Row,Column,DataType>{

    
    protected JavaSparkContext ctx;
	protected SparkSession spark;
    protected PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> pipeUtil;
    protected HashUtil<Dataset<Row>, Row, Column, DataType> hashUtil;
    protected DSUtil<SparkSession, Dataset<Row>, Row, Column> dsUtil;
    protected GraphUtil<Dataset<Row>, Row, Column> graphUtil;
    protected ModelUtil<SparkSession, DataType, Dataset<Row>, Row, Column> modelUtil;
    protected BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType> blockingTreeUtil;

	public static final String hashFunctionFile = "hashFunctions.json";
    

    public static final Log LOG = LogFactory.getLog(ZinggSparkContext.class);
    

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
            setBlokingTreeUtil(new SparkBlockingTreeUtil(spark, getPipeUtil()));
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


    
    public void setHashUtil(HashUtil<Dataset<Row>, Row, Column, DataType> t) {
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

    public void setBlokingTreeUtil(BlockingTreeUtil<SparkSession,Dataset<Row>, Row, Column, DataType> d) {
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
    public HashUtil<Dataset<Row>, Row, Column, DataType> getHashUtil() {
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