package zingg.client.pipe;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PipeFactory {
	
	public static Map<String, Pipe> pipes = new HashMap<String, Pipe>();
	public static final Log LOG = LogFactory.getLog(PipeFactory.class);
	
	
	private PipeFactory() {}
	
	public static Pipe getPipe(String name) {
		return pipes.get(name);
	}
	
	public static void register(String name, Pipe p) {
		pipes.put(name, getPipe(p));
	}
	
	private static final Pipe getPipe(Pipe p) {
		return new Pipe();
	}

}