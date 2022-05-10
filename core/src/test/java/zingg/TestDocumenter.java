package zingg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Pipe;
import zingg.documenter.TestBaseDocumenter;

public class TestDocumenter extends TestBaseDocumenter {

	@BeforeEach
	public void setUp() {

		try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testConfig.json").getFile());
			//fail("Exception was expected for missing config file");
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

	@Test
	public void testOutput() throws Throwable {
		try {
			args = Arguments.createArgumentsFromJSON(getClass().getResource("/testConfig.json").getFile());
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}

		Documenter doc = new Documenter();
		doc.init(args, "");
		doc.setArgs(args);
		doc.execute();
	}

	@Test
	public void testDocumenterWhenModelIsNotYetAvailable() {
		String rootDir = getRootDir();
		try {
			args = Arguments.createArgumentsFromJSON(rootDir + "/examples/amazon-google/config.json");

			Pipe p = args.getData()[0]; 
			p.setProp(FilePipe.LOCATION, "../examples/amazon-google/Amazon.csv");
			Pipe[] newData = {p};
			args.setData(newData);

		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
		
		Documenter doc = new Documenter();
		// Set a new model id that is not used. Model Doc should have any data but generation of other docs should not be impacted
		args.setModelId("9999");
		args.setZinggDir("../models");
		try {
			doc.init(args, "");
			doc.setArgs(args);
			doc.execute();

			File modelFile = new File(args.getZinggDocFile());
			assertEquals(true, modelFile.exists());
			assertModelFleSize(modelFile.length() < DEFAULT_FILE_SIZE, modelFile.length());
			
			File directory = new File(args.getZinggDocDir());
			assertEquals(true, directory.exists(), "Zingg Directory does not exist. Docs haven't been generated. " + args.getZinggDocDir());

			File[] listOfFiles = directory.listFiles((dir, name) -> name.endsWith(".html"));
			assertNotEquals(listOfFiles.length, 0, "No. of generated html documents is not proper. Length: " + listOfFiles.length);
		} catch (ZinggClientException e) {	
			e.printStackTrace();
		}
	}


	@Test
	public void testDocumenterForAmazonGoogleModel() {
		String rootDir = getRootDir();
		try {
			args = Arguments.createArgumentsFromJSON(rootDir + "/examples/amazon-google/config.json");
			
			Pipe p = args.getData()[0]; 
			p.setProp(FilePipe.LOCATION, "../examples/amazon-google/Amazon.csv");
			Pipe[] newData = {p};
			args.setData(newData);
			args.setZinggDir("../models");
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}

		Documenter doc = new Documenter();
		try {
			doc.init(args, "");
			doc.setArgs(args);
			doc.execute();

			File modelFile = new File(args.getZinggDocFile());
			assertEquals(true, modelFile.exists(), "Doc file hasn't been generated. " + args.getZinggDocFile());
			assertModelFleSize(modelFile.length() > DEFAULT_FILE_SIZE, modelFile.length());

			File directory = new File(args.getZinggDocDir());
			assertEquals(true, directory.exists(), "Zingg Directory does not exist. Column docs haven't been generated. " + args.getZinggDocDir());

			File[] listOfFiles = directory.listFiles((dir, name) -> name.endsWith(".html"));
			if(listOfFiles.length == 0) {
				fail("Zingg Directory does exist but Column docs haven't been generated.");
			}
		} catch (ZinggClientException e) {
			e.printStackTrace();
		}
	}
}
