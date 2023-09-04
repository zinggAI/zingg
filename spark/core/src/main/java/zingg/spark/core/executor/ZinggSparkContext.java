package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IZingg;
import zingg.common.client.ZinggClientException;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.Context;
import zingg.common.core.preprocess.PreprocUtil;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.DSUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;
import zingg.common.core.util.PipeUtilBase;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.preprocess.SparkPreprocUtil;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkDSUtil;
import zingg.spark.core.util.SparkGraphUtil;
import zingg.spark.core.util.SparkHashUtil;
import zingg.spark.core.util.SparkModelUtil;
import zingg.spark.core.util.SparkPipeUtil;


public class ZinggSparkContext implements Context<ZSparkSession, Dataset<Row>, Row,Column,DataType>{

    
    private static final long serialVersionUID = 1L;
	protected JavaSparkContext ctx;
	protected ZSparkSession zSession;
    protected PipeUtilBase<ZSparkSession, Dataset<Row>, Row, Column> pipeUtil;
    protected HashUtil<ZSparkSession,Dataset<Row>, Row, Column, DataType> hashUtil;
    protected DSUtil<ZSparkSession, Dataset<Row>, Row, Column> dsUtil;
    protected GraphUtil<Dataset<Row>, Row, Column> graphUtil;
    protected ModelUtil<ZSparkSession, DataType, Dataset<Row>, Row, Column> modelUtil;
    protected BlockingTreeUtil<ZSparkSession, Dataset<Row>, Row, Column, DataType> blockingTreeUtil;
    protected PreprocUtil<ZSparkSession, Dataset<Row>, Row, Column,DataType> preprocUtil;
    
	public static final String hashFunctionFile = "hashFunctions.json";
    

    public static final Log LOG = LogFactory.getLog(ZinggSparkContext.class);

    
    public ZSparkSession getSession() {
        return zSession;
    }

    public void setSession(ZSparkSession spark) {
        LOG.debug("Session passed to context is " + spark);
        this.zSession = spark;
    }



    @Override
    public void init(IZinggLicense license)
        throws ZinggClientException {
        try{
            if (zSession==null || zSession.getSession() == null) {
            	SparkSession spark = SparkSession
                    .builder()
                    .appName("Zingg")
                    .getOrCreate();
            	
            	zSession = new ZSparkSession(spark, license);
            }
            if (ctx==null) {
				ctx = JavaSparkContext.fromSparkContext(zSession.getSession().sparkContext());
				JavaSparkContext.jarOfClass(IZingg.class);
				LOG.debug("Context " + ctx.toString());
				//initHashFns();
				ctx.setCheckpointDir("/tmp/checkpoint");
				setUtils();
			}
        }
        catch(Throwable e) {
            if (LOG.isDebugEnabled()) e.printStackTrace();
            throw new ZinggClientException(e.getMessage());
        }
    }

	@Override
	public void cleanup() {
		try {
				if (ctx != null) {
					ctx.stop();
				}
				if (zSession!=null && zSession.getSession() != null) {
					zSession.getSession().stop();
				}
				ctx = null;
				zSession = null;
		} catch (Exception e) {
			// ignore any exception in cleanup
			e.printStackTrace();
		}
	}
    
    @Override
    public void setUtils() {
        LOG.debug("Session passed to utils is " + zSession.getSession());
        setPipeUtil(new SparkPipeUtil(zSession));
        setDSUtil(new SparkDSUtil(zSession));
        setHashUtil(new SparkHashUtil(zSession));
        setGraphUtil(new SparkGraphUtil());
        setModelUtil(new SparkModelUtil(zSession));
        setBlockingTreeUtil(new SparkBlockingTreeUtil(zSession, getPipeUtil()));
        setPreprocUtil(new SparkPreprocUtil(zSession, getPipeUtil()));
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


    
    public void setHashUtil(HashUtil<ZSparkSession,Dataset<Row>, Row, Column, DataType> t) {
        this.hashUtil = t;
    }

    public void setGraphUtil(GraphUtil<Dataset<Row>, Row, Column> t) {
        this.graphUtil = t;
    }

    
    
    public void setPipeUtil(PipeUtilBase<ZSparkSession, Dataset<Row>, Row, Column> pipeUtil) {
        this.pipeUtil = pipeUtil;        
    }

   
    public void setDSUtil(DSUtil<ZSparkSession, Dataset<Row>, Row, Column> pipeUtil) {
       this.dsUtil = pipeUtil;        
    }

    public void setBlockingTreeUtil(BlockingTreeUtil<ZSparkSession,Dataset<Row>, Row, Column, DataType> d) {
        this.blockingTreeUtil = d;
    }

    public void setModelUtil(ModelUtil<ZSparkSession, DataType, Dataset<Row>, Row, Column>  t) {
        this.modelUtil = t;
    }

    public ModelUtil<ZSparkSession, DataType, Dataset<Row>, Row, Column>   getModelUtil() {
        return modelUtil;
    }

   /*  @Override
    public void setSession(SparkSession session) {
        this.spark = session;        
    }
    */

    @Override
    public HashUtil<ZSparkSession,Dataset<Row>, Row, Column, DataType> getHashUtil() {
        return hashUtil;
    }

    @Override
    public GraphUtil<Dataset<Row>, Row, Column> getGraphUtil() {
        return graphUtil;
    }

    @Override
    public DSUtil<ZSparkSession, Dataset<Row>, Row, Column> getDSUtil() {
         return dsUtil;
    }

    @Override
    public PipeUtilBase<ZSparkSession, Dataset<Row>, Row, Column> getPipeUtil() {
        return pipeUtil;
    }

    @Override
    public BlockingTreeUtil<ZSparkSession, Dataset<Row>, Row, Column, DataType> getBlockingTreeUtil() {
        return blockingTreeUtil;
    }
    
    @Override
    public PreprocUtil<ZSparkSession, Dataset<Row>, Row, Column, DataType> getPreprocUtil() {
    	return preprocUtil;
    }
    
    @Override
    public void setPreprocUtil(PreprocUtil<ZSparkSession, Dataset<Row>, Row, Column, DataType> preprocUtil) {
    	this.preprocUtil = preprocUtil;	
    }
  
 }