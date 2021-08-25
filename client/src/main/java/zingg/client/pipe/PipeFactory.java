package zingg.client.pipe;

import java.util.HashMap;
import java.util.Map;

public class PipeFactory {
	
	public static Map<String, Pipe> pipes = new HashMap<String, Pipe>();
	
	
	private PipeFactory() {}
	
	public static Pipe getPipe(String name) {
		return pipes.get(name);
	}
	
	public static void register(String name, Pipe p) {
		pipes.put(name, getPipe(p));
	}
	
	private static final Pipe getPipe(Pipe p) {
		switch (p.format) {
		case CSV:
		case JSON:
		case XLS:
		case XLSX:
			return new FilePipe(p);
		case CASSANDRA:
			return p;
		case ELASTIC:
			return p;
		case JDBC:
			return new JdbcPipe(p);
		default:
			break;
		}
		return null;
		
	}

}
