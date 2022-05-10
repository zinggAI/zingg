package zingg.documenter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import zingg.BaseSparkTest;

public class TestBaseDocumenter extends BaseSparkTest {

    public static final int DEFAULT_FILE_SIZE = 2000;

	protected void assertModelFleSize(Boolean condition, long length) {
		assertTrue(condition, "Size of file " + args.getZinggDocFile() + " is " + length + ". Expected size " + DEFAULT_FILE_SIZE);
	}

	protected String getRootDir() {
		File resourcesDirectory = new File("src/test/resources");
		String absolutePath = resourcesDirectory.getAbsolutePath();
		String rootDir = absolutePath.substring( 0, absolutePath.indexOf( "core/src/test/resources" ) );
		return rootDir;
	}

}