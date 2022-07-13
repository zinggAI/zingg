package zingg.client.pipe;

public class FilePipe extends Pipe{
	
	public static final String LOCATION = "location";
	public static final String HEADER = "header";
	public static final String DELIMITER = "delimiter";
	
	public FilePipe(Pipe p) {
		clone(p);
	}
	
	

}
