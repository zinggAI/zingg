package zingg.common.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
		String[] args = {"--phase", "train",
				"--conf1", "conf.json",
				"--zinggDir", "/tmp/z_main",
				"--email", "zingg@zingg.ai",
				"--license", "zinggLicense.txt"};

		assertThrows(UnsupportedOperationException.class, () -> {
			new ClientOptions(args);
		});
	}
	
	@Test
	public void testParseUnsupportedArgumentsPhase() {
		String[] args = {"--phase1", "train",
				"--conf1", "conf.json",
				"--zinggDir", "/tmp/z_main",
				"--email", "zingg@zingg.ai",
				"--license", "zinggLicense.txt"};

		assertThrows(UnsupportedOperationException.class, () -> {
			new ClientOptions(args);
		});
	}
	
	
}
