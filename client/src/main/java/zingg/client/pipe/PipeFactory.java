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
		try {
			switch (p.format) {
			case Pipe.FORMAT_CSV:
			case Pipe.FORMAT_JSON:
			case Pipe.FORMAT_XLS:
				return new FilePipe(p);
			case Pipe.FORMAT_CASSANDRA:
				return p;
			case Pipe.FORMAT_ELASTIC:
				return p;
			case Pipe.FORMAT_JDBC:
				return new JdbcPipe(p);
			case Pipe.FORMAT_INMEMORY:
				return new InMemoryPipe(p);
			default:
				break;
			}
		}
		catch (Exception e) {LOG.warn("given format not found, defaulting");}
		return new Pipe();
		
	}

}
