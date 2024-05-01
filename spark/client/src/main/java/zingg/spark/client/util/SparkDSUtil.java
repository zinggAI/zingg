package zingg.spark.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import zingg.common.client.ZFrame;
import zingg.common.client.util.DSUtil;
import zingg.scala.DFUtil;
import zingg.spark.client.SparkFrame;
import org.apache.spark.sql.SparkSession;

public class SparkDSUtil extends DSUtil<SparkSession, Dataset<Row>, Row, Column>{

    public SparkDSUtil(SparkSession s) {
        super(s);
        //TODO Auto-generated constructor stub
    }



    public static final Log LOG = LogFactory.getLog(SparkDSUtil.class);	

    

    @Override
    public ZFrame<Dataset<Row>, Row, Column> addClusterRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {

        SparkSession sparkSession = getSession();
		return new SparkFrame(DFUtil.addClusterRowNumber(((Dataset<Row>)ds.df()), sparkSession));
    }



    @Override
    public ZFrame<Dataset<Row>, Row, Column> addRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {
    	SparkSession SparkSession = getSession();
        return new SparkFrame(DFUtil.addRowNumber(((Dataset<Row>)ds.df()), getSession()));
    }

	

	
	

	
}
