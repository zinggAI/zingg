package zingg.snowpark.util;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;

import zingg.client.SnowFrame;
import zingg.client.ZFrame;
import zingg.util.DSUtil;
import zingg.scala.DFUtil;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SnowDSUtil extends DSUtil<Session, DataFrame, Row, Column>{

    public SnowDSUtil(Session s) {
        super(s);
        //TODO Auto-generated constructor stub
    }



    public static final Log LOG = LogFactory.getLog(SnowDSUtil.class);	

    

    @Override
    public ZFrame<DataFrame, Row, Column> addClusterRowNumber(ZFrame<DataFrame, Row, Column> ds) {

        return new SnowFrame(DFUtil.addClusterRowNumber(((DataFrame)ds.df()), getSession()));
    }

	

	
	

	
}
