package zingg.snowpark;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.RelationalGroupedDataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.Functions;
// import com.snowflake.snowpark_java.catalyst.encoders.RowEncoder;
// import com.snowflake.snowpark_java.expressions.Window;
// import com.snowflake.snowpark_java.expressions.WindowSpec;
import com.snowflake.snowpark_java.types.DataType;

import scala.collection.JavaConverters;
import zingg.FindAndLabeller;
import zingg.TrainingDataFinder;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.snowpark.model.SnowModel;
import zingg.client.SnowFrame;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;


public class SnowFindAndLabeller extends FindAndLabeller<Session, DataFrame, Row, Column,DataType> {

	public static String name = "zingg.SnowFindAndLabeller";
	public static final Log LOG = LogFactory.getLog(SnowFindAndLabeller.class);

	private Session snowSession;
	@Override
	public void setSnow(Session session) {
		this.snowSession = session;
		
	}

	public SnowFindAndLabeller() {
		setZinggOptions(ZinggOptions.FIND_AND_LABEL);
	}

	
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSession(Session session) {
		// TODO Auto-generated method stub
		
	}
	
}
