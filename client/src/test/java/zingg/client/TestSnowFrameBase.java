package zingg.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;


public class TestSnowFrameBase {

	public static Arguments args;
	public static Session snowSession;

	public static final Log LOG = LogFactory.getLog(TestSnowFrameBase.class);
	
	public static final String STR_RECID = "recid";
	public static final String STR_GIVENNAME = "givenname";
	public static final String STR_SURNAME = "surname";
	public static final String STR_COST = "cost";
	public static final String STR_POSTCODE = "postcode";
	public static final String STR_SUBURB = "suburb";


	@BeforeAll
	public static void setup() {
		try {
			Map<String, String> propMp = new HashMap<>();
			propMp.put("URL", "https://fd11025.ap-south-1.aws.snowflakecomputing.com:443");
			propMp.put("USER", "AKASHRR");
			propMp.put("PASSWORD", "PmV4bFC3RgFWrNa");
			propMp.put("ROLE", "ACCOUNTADMIN");
			propMp.put("WAREHOUSE", "COMPUTE_WH");
			propMp.put("DB", "TESTING_SNOWFRAME");
			propMp.put("SCHEMA","TEST_SCHEMA");
			snowSession = Session.builder().configs(propMp).create();// config:credentials for an account

			args = new Arguments();
		} catch (Throwable e) {
			if (LOG.isDebugEnabled())
				e.printStackTrace();
			LOG.info("Problem in snow env setup");
		}
	}

	@AfterAll
	public static void teardown() {
		if (snowSession != null) {
			snowSession = null;
		}
	}

	public DataFrame createSampleDataset() {

		DataFrame sample = snowSession.table("SAMPLE_DS");// uploaded from samples.csv
								
		return sample;
	}

	public DataFrame createSampleDatasetHavingMixedDataTypes() {
		
		DataFrame sample = snowSession.table("SAMPLE_DS_MIXED_TYPE");// uploaded from sample2.csv

		return sample;
	}

	public DataFrame createEmptyDataFrame(){
		
		DataFrame sample = snowSession.table("SAMPLE_DS_EMPTY");// same schema as EXAMPLE_TWO

		return sample;
	}

	protected void assertTrueCheckingExceptOutput(ZFrame<DataFrame, Row, Column> sf1, ZFrame<DataFrame, Row, Column> sf2, String message) {
		assertTrue(sf1.except(sf2).isEmpty(), message);
	}

	protected void assertTrueCheckingExceptOutput(ZFrame<DataFrame, Row, Column> sf1, DataFrame df2, String message) {
		SnowFrame sf2 = new SnowFrame(df2);
		assertTrue(sf1.except(sf2).isEmpty(), message);
	}

}
