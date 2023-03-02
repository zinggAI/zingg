package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.spark.client.SparkFrame;
import zingg.common.client.ZFrame;
import zingg.util.DSUtil;
import zingg.scala.DFUtil;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SparkDSUtil extends DSUtil<SparkSession, Dataset<Row>, Row, Column>{

    public SparkDSUtil(SparkSession s) {
        super(s);
        //TODO Auto-generated constructor stub
    }



    public static final Log LOG = LogFactory.getLog(SparkDSUtil.class);	

    

    @Override
    public ZFrame<Dataset<Row>, Row, Column> addClusterRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {

        return new SparkFrame(DFUtil.addClusterRowNumber(((Dataset<Row>)ds.df()), getSession()));
    }



    @Override
    public ZFrame<Dataset<Row>, Row, Column> addRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {
        return new SparkFrame(DFUtil.addRowNumber(((Dataset<Row>)ds.df()), getSession()));
    }

	

	
	

	
}
