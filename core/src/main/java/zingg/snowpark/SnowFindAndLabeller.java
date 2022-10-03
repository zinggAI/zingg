package zingg.snowpark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.types.DataType;

import zingg.FindAndLabeller;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;



public class SnowFindAndLabeller extends FindAndLabeller<Session, DataFrame, Row, Column,DataType> {

	public static String name = "zingg.SnowFindAndLabeller";
	public static final Log LOG = LogFactory.getLog(SnowFindAndLabeller.class);
	Session snowSession;

	@Override
	public void setSession(Session session) {
		this.snowSession = session;
		
	}

	public SnowFindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
	}

	
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}
	
}
