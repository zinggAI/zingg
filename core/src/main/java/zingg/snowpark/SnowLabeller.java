package zingg.snowpark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.types.DataType;

import zingg.Labeller;


import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;




public class SnowLabeller extends Labeller<Session, DataFrame, Row, Column,DataType> {

	public static String name = "zingg.SnowLabeller";
	public static final Log LOG = LogFactory.getLog(SnowLabeller.class);
	Session snowSession;

	public SnowLabeller() {
		setZinggOptions(ZinggOptions.LABEL);
	}

	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSession(Session session) {
		this.snowSession = session;
		
	}
	
}
