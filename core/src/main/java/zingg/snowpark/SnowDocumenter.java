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
// import org.apache.spark.sql.catalyst.encoders.RowEncoder;
// import org.apache.spark.sql.expressions.Window;
// import org.apache.spark.sql.expressions.WindowSpec;
import com.snowflake.snowpark_java.types.DataType;

import scala.collection.JavaConverters;
import zingg.Documenter;
import zingg.Linker;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.spark.model.SparkModel;
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


public class SnowDocumenter extends Documenter<Session, DataFrame, Row, Column,DataType> {

	public static String name = "zingg.SnowDocumenter";
	public static final Log LOG = LogFactory.getLog(SnowDocumenter.class);

	public SnowDocumenter() {
		setZinggOptions(ZinggOptions.GENERATE_DOCS);
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
