package zingg.spark.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.extension.ExtendWith;

import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.TestSparkBase;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.util.TestDSUtil;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkDSUtil extends TestDSUtil<SparkSession, Dataset<Row>, Row, Column, DataType> {

	public static final Log LOG = LogFactory.getLog(TestSparkDSUtil.class);
	private ZinggSparkContext zinggSparkContext;
	private IWithSession<SparkSession> iWithSession;

	public TestSparkDSUtil(SparkSession sparkSession) throws ZinggClientException {
		this.zinggSparkContext = new ZinggSparkContext();
		this.iWithSession = new WithSession<SparkSession>();
		iWithSession.setSession(sparkSession);
		zinggSparkContext.init(sparkSession);
		initialize(new SparkDFObjectUtil(iWithSession), zinggSparkContext);
	}

	@Override
	public List<String> getColNames(List<Column> col) {
		List<String> colNamesList = new ArrayList<String>();
		for (int i = 0; i < col.size(); i++) {
			String s = col.get(i).toString();
			colNamesList.add(i,s);
		}
		return colNamesList;
	}

	@Override
	public List<String> getExpectedColNames(List<String> col) {
		return col;
	}

}
