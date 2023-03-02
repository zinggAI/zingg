package zingg.common.client;

import java.io.Serializable;


public class LabelSamples implements Serializable{
	String id;
	String line;
	

	public LabelSamples(String id, String line) {
		super();
		this.id = id;
		this.line = line;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	
}
