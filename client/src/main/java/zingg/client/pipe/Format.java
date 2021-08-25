package zingg.client.pipe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Format implements Serializable{	
	
	
	CSV ("csv"),
	JSON("json"),
	JDBC("jdbc"),
	ELASTIC("org.elasticsearch.spark.sql"),
	CASSANDRA("org.apache.spark.sql.cassandra"),
	XLS("com.crealytics.spark.excel"),
	XLSX("com.crealytics.spark.excel"),
	PARQUET("PARQUET"),
	AVRO("avro");
	
	String type;
	static Map<String, Format> map;
	
	private Format(String type) {
		this.type = type;
	}
	
	static{
		map = new HashMap<String, Format>();
		for (Format p: Format.values()) {
			map.put(p.type.toLowerCase(), p);
		}
	}
	
	@JsonCreator
	public static Format getPipeType(String t) {
		return map.get(t.toLowerCase());
	}

	@JsonValue
	public String type() {
		return type;
	}
	
	
	
	
	
}
