package zingg.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestClientOption {

	@Test
	public void dummy() {
		//placeholder
	}
		/*
	
		@Test
		public void testParseArguments() {
				List<String> args = new ArrayList<String>();
				args.add("--conf");
				args.add("conf.json");
				args.add("--phase");
				args.add("train");
				ClientOptions co = new ClientOptions();
				co.parse(args);
				assertEquals("conf.json", co.get(co.CONF).value);
				assertEquals("train", co.get(co.PHASE).value);
				assertNull(co.get(co.HELP));
				
			
		}

		@Test(expected=UnsupportedOperationException.class)
		public void testParseUnsupportedArguments() {
					List<String> args = new ArrayList<String>();
					
					args.add("--conf1");
					args.add("conf.json");
					args.add("--phase");
					args.add("train");
					ClientOptions co = new ClientOptions();
					co.parse(args);
		}
		
		@Test(expected=UnsupportedOperationException.class)
		public void testParseUnsupportedArgumentsLast() {
					List<String> args = new ArrayList<String>();
					
					args.add("--conf");
					args.add("conf.json");
					args.add("--phase1");
					args.add("train");
					ClientOptions co = new ClientOptions();
					co.parse(args);
		}
		*/
}
