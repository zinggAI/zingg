package zingg.snowpark;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session; 
import com.snowflake.snowpark_java.types.DataType;

import zingg.ZinggBase;
import zingg.client.Arguments;
import zingg.client.IZingg;
import zingg.client.ZinggClientException;
import zingg.snowpark.util.SnowHashUtil;
import zingg.snowpark.util.SnowPipeUtil;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.HashUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;
import zingg.snowpark.util.SnowDSUtil;
import zingg.snowpark.util.SnowGraphUtil;

public class SnowBase extends ZinggBase<Session,DataFrame,Row,Column,DataType>{

    Session ctx;
    public static final Log LOG = LogFactory.getLog(SnowBase.class);
    

    @Override
    public void init(Arguments args, String license)
        throws ZinggClientException {
        startTime = System.currentTimeMillis();
        this.args = args;
        try{
            Map<String, String> propMp = new HashMap<>();
			propMp.put("URL", "https://fd11025.ap-south-1.aws.snowflakecomputing.com:443");
			propMp.put("USER", "AKASHRR");
			propMp.put("PASSWORD", "PmV4bFC3RgFWrNa");
			propMp.put("ROLE", "ACCOUNTADMIN");
			propMp.put("WAREHOUSE", "COMPUTE_WH");
			propMp.put("DB", "TESTING_SNOWFRAME");
			propMp.put("SCHEMA","TEST_SCHEMA");
			Session SnowSession = Session.builder().configs(propMp).create();// config:credentials for an account
            context = SnowSession;
            
            ctx = context;
            // JavaSparkContext.jarOfClass(IZingg.class);
            // LOG.debug("Context " + ctx.toString());
            initHashFns();
            loadFeatures();
            // ctx.setCheckpointDir("/tmp/checkpoint");	
            setPipeUtil(new SnowPipeUtil(context));
            setDSUtil(new SnowDSUtil(context));
            setHashUtil(new SnowHashUtil());
            // setGraphUtil(new SnowGraphUtil());
            
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
        if (ctx != null){ 
            ctx = null;
        }
    }

    @Override
    public void execute() throws ZinggClientException {

    }   

    
    public void setHashUtil(HashUtil<DataFrame, Row, Column, DataType> t) {
        this.hashUtil = t;
    }

    public void setGraphUtil(GraphUtil<DataFrame, Row, Column> t) {
        this.graphUtil = t;
    }

    
    
    public void setPipeUtil(PipeUtilBase<Session, DataFrame, Row, Column> pipeUtil) {
        this.pipeUtil = pipeUtil;        
    }

   
    public void setDSUtil(DSUtil<Session, DataFrame, Row, Column> pipeUtil) {
       this.dsUtil = pipeUtil;        
    }

    public void setBlokingTreeUtil(BlockingTreeUtil<DataFrame, Row, Column, DataType> d) {
        this.blockingTreeUtil = d;
    }

    public void setModelUtil(ModelUtil<Session, DataFrame, Row, Column> t) {
        this.modelUtil = t;
    }

    public ModelUtil<Session, DataFrame, Row, Column>  getModelUtil() {
        return modelUtil;
    }

    @Override
    public void setSnow(Session session) {
        // TODO Auto-generated method stub
        
    }
  
 }