package zingg;

import java.io.File;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.ZinggClientException;
import scala.Tuple2;

public class TestFebrlDataset {
	
	private transient JavaSparkContext sc;
	
	@Before
    public void setUp() throws Exception, ZinggClientException{
      sc = new JavaSparkContext("local", "JavaAPISuite");
    }

    @After
    public void tearDown() {
      sc.stop();
      sc = null;
      // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
      System.clearProperty("spark.driver.port");
    }

	public void deleteFolder(String name) throws Exception {
		if (name.startsWith("/tmp")) {
			File model = new File(name);
			if (model.exists() && model.isDirectory()) {
				for (File f: model.listFiles()) {
					System.out.println("Deleting " + f);
					if (f.isDirectory()) deleteFolder(f.getAbsolutePath());
					else f.delete();
				}
				System.out.println("Deleting " + model);
				model.delete();
			}
		}
	}
	
	@Test
	public void testFebrlEndToEnd() throws Exception, ZinggClientException {
		/*String testFileBase = System.getProperty("dataDir") + "/multiField/" ;
		String json = testFileBase + "config.json";
		String modelDir = "/tmp/testFebrlModel";
		String outDir = "/tmp/testOut";
		String resultsDir = "/tmp/testFebrl/";
		String goldFile = resultsDir + "gold";
		String outputPrettyFile = resultsDir + "actual";

		deleteFolder(modelDir);
		deleteFolder(outDir);
		deleteFolder(goldFile);
		deleteFolder(outputPrettyFile);
		deleteFolder(resultsDir);
		Arguments args = Arguments.createArgumentsFromJSON(json);
		
		args.setZinggDir(modelDir);
		args.setMatchOutputDir(outDir);
		//delete old files left from last run
		
		Zingg zingg = new Zingg(args, sc, System.getProperty("license"));
		zingg.train(sc);
		zingg.execute(sc);
		//create valid pair data
		JavaPairRDD<String, FebrlRecord> goldRecords = sc.textFile(args.getMatchDataFile()).
				mapToPair(FebrlRecord::parse);
		JavaRDD<Tuple2<FebrlRecord, FebrlRecord>> gold = goldRecords.join(goldRecords).values()
				.filter(FebrlRecord::isValidPair);
		JavaRDD<String> goldPretty = gold.map(FebrlRecord::prettyPair).distinct();
		goldPretty.saveAsTextFile(goldFile);
		
		JavaPairRDD<String, FebrlRecord> output = sc.textFile(args.getMatchOutputDir()).zipWithUniqueId().
				mapToPair(FebrlRecord::parseOutput);
		JavaRDD<Tuple2<FebrlRecord, FebrlRecord>> outputPairs = output.join(output).values()
				.filter(FebrlRecord::isValidPair);
		JavaRDD<String> outputPretty = outputPairs.map(FebrlRecord::prettyPair).distinct();
		goldPretty.cache();
		outputPretty.cache();
		long countGold = goldPretty.count();
		long countOut = outputPretty.count();
		System.out.println("Counts are gold:" + countGold + " and Zingg output:" + countOut);
		outputPretty.saveAsTextFile(outputPrettyFile);
		JavaRDD<String> falsePos = outputPretty.subtract(goldPretty);
		falsePos.saveAsTextFile(resultsDir + "falsePos");
		JavaRDD<String> truePos = outputPretty.intersection(goldPretty);
		JavaRDD<String> falseNeg = goldPretty.subtract(outputPretty);
		falseNeg.saveAsTextFile(resultsDir + "falseNeg");
		double precision = (double) truePos.count()/outputPairs.count();
		double recall = (double) truePos.count()/gold.count();
		System.out.println("Precision " + precision);
		System.out.println("Recall " + recall);
		System.out.println("Accuracy " + 2*precision*recall/(precision+recall));
		
		/*deleteFolder(modelDir);
		deleteFolder(outDir);
		deleteFolder(goldFile);
		deleteFolder(outputPrettyFile);
		*/
	}
	
}
