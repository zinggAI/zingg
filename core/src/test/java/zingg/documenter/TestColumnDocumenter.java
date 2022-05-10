package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;
import zingg.client.MatchType;
import zingg.client.ZinggClientException;
import zingg.util.DSUtil;

public class TestColumnDocumenter extends TestBaseDocumenter {
  
	public static final Log LOG = LogFactory.getLog(TestColumnDocumenter.class);

	@BeforeEach
	public void setUp() {

		try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testConfig.json").getFile());
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateColumnDocuments() {
		ColumnDocumenter cd = new ColumnDocumenter(spark, args);
		try {
			cd.createColumnDocuments();
		} catch (ZinggClientException e) {
			e.printStackTrace();
		}

		File directory = new File(args.getZinggDocDir());
		LOG.info("Directory Name: " + args.getZinggDocDir());
		assertEquals(true, directory.exists(), "Zingg Directory does not exist. Docs haven't been generated. " + args.getZinggDocDir());

		File[] listOfFiles = directory.listFiles((dir, name) -> name.endsWith(".html"));
		int htmlDocsCount = DSUtil.getFieldDefinitionFiltered(args, MatchType.DONT_USE).size()
								+ cd.getZColumnList().size();
		assertEquals(listOfFiles.length, htmlDocsCount, "No. of generated html documents is not proper. Expected: " + htmlDocsCount + ", Actual: " + listOfFiles.length);

		String stopWordsDir = args.getZinggDocDir() + "/stopWords/";
		File directoryStopWord = new File(stopWordsDir);
		File[] listOfStopWordsFiles = directoryStopWord.listFiles((dir, name) -> name.endsWith(".csv"));
		int csvDocsCount = htmlDocsCount;
		assertEquals(listOfStopWordsFiles.length, csvDocsCount, "No. of generated stopWord files is not proper. Expected: " + csvDocsCount + ", Actual: " + listOfStopWordsFiles.length);
	}
}
