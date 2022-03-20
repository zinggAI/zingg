package zingg.client.pipe;

public class JdbcPipe extends Pipe{

	public static final String DRIVER = "driver";
	public static final String URL = "url";
	public static final String USERNAME = "user";
	public static final String PASSWORD="password";
	public static final String TABLE="dbtable";
	public static final String QUERY="query";
	
	
	public JdbcPipe(Pipe p) {
		//clone(p);
	}
	
	@Override
	public Format getFormat() {
		return Format.JDBC;
	}
}
