package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class TestClientOption {

	
	@Test
	public void testParseArguments() {
		String[] args = {"--phase", "train", 
				"--conf", "conf.json", 
				"--zinggDir", "/tmp/z_main", 
				"--email", "zingg@zingg.ai", 
				"--license", "zinggLicense.txt"};
		
		ClientOptions co = new ClientOptions(args);
		assertEquals("conf.json", co.get(ClientOptions.CONF).value);
		assertEquals("train", co.get(ClientOptions.PHASE).value);
		assertEquals("/tmp/z_main", co.get(ClientOptions.ZINGG_DIR).value);
		assertNull(co.get(ClientOptions.HELP));
	}
	
	@Test
	public void testParseUnsupportedArgumentsConf() {
		try {
			String[] args = {"--phase", "train", 
					"--conf1", "conf.json", 
					"--zinggDir", "/tmp/z_main", 
					"--email", "zingg@zingg.ai", 
					"--license", "zinggLicense.txt"};
			
			ClientOptions co = new ClientOptions(args);
			fail("exception should have been raised due to invalid conf option");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testParseUnsupportedArgumentsPhase() {
		try {
			String[] args = {"--phase1", "train", 
					"--conf1", "conf.json", 
					"--zinggDir", "/tmp/z_main", 
					"--email", "zingg@zingg.ai", 
					"--license", "zinggLicense.txt"};
			
			ClientOptions co = new ClientOptions(args);
			fail("exception should have been raised due to invalid phase option");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	
}
