package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Pipe;

public class TestModelDocumenter extends TestBaseDocumenter {
    @Test
	public void testCreateModelDocument(){
		String rootDir = getRootDir();
		try {
			args = Arguments.createArgumentsFromJSON(rootDir + "/examples/iTunes-amazon/config.json");
			
			Pipe p = args.getData()[0]; 
			p.setProp(FilePipe.LOCATION, "../examples/iTunes-amazon/iTunesMusic.csv");
			Pipe[] newData = {p};
			args.setData(newData);
			args.setZinggDir("../models");
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}

		ModelDocumenter md = new ModelDocumenter(spark, args);
		try {
			md.process();
		} catch (ZinggClientException e) {
			e.printStackTrace();
		}

		File modelFile = new File(args.getZinggDocFile());
		assertEquals(true, modelFile.exists(), "Doc file hasn't been generated. " + args.getZinggDocFile());
		assertModelFleSize(modelFile.length() > DEFAULT_FILE_SIZE, modelFile.length());

		File directory = new File(args.getZinggDocDir());
		assertEquals(true, directory.exists(), "Zingg Directory does not exist. Column docs haven't been generated. " + args.getZinggDocDir());
	}
}
