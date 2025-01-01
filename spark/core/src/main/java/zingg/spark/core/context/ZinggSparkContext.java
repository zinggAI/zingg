package zingg.spark.core.context;

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
import zingg.common.client.util.DSUtil;
import zingg.common.client.util.PipeUtilBase;
//
import zingg.common.core.context.Context;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;
import zingg.spark.client.util.SparkDSUtil;
import zingg.spark.client.util.SparkModelHelper;
import zingg.spark.client.util.SparkPipeUtil;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkGraphUtil;
import zingg.spark.core.util.SparkHashUtil;
import zingg.spark.core.util.SparkModelUtil;


public class ZinggSparkContext extends Context<SparkSession, Dataset<Row>, Row,Column,DataType>{

    
    private static final long serialVersionUID = 1L;
	protected JavaSparkContext ctx;
    public static final Log LOG = LogFactory.getLog(ZinggSparkContext.class);

	
    @Override
    public void init(SparkSession session)
        throws ZinggClientException {
			this.session = session;
        	setUtils();
		
    }

	@Override
	public void cleanup() {
		try {
				if (ctx != null) {
					ctx.stop();
				}
				if (session!=null) {
					session.stop();
				}
				ctx = null;
				session = null;
		} catch (Exception e) {
			// ignore any exception in cleanup
			e.printStackTrace();
		}
	}
    
    @Override
    public void setUtils() {
        LOG.debug("Session passed to utils is " + session);
        setPipeUtil(new SparkPipeUtil(session));
        setDSUtil(new SparkDSUtil(session));
        setHashUtil(new SparkHashUtil(session));
        setGraphUtil(new SparkGraphUtil());
        setModelUtil(new SparkModelUtil(session));
        setBlockingTreeUtil(new SparkBlockingTreeUtil(session, getPipeUtil()));
		setModelHelper(new SparkModelHelper());
    }

    
 }