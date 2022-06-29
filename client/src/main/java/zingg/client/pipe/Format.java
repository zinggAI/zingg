package zingg.client.pipe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Format implements Serializable{

	public static final Format CSV = new Format("csv");
	public static final Format JSON = new Format("json");
	public static final Format JDBC = new Format("jdbc");
	public static final Format ELASTIC = new Format("org.elasticsearch.spark.sql");
	public static final Format CASSANDRA = new Format("org.apache.spark.sql.cassandra");
	public static final Format XLS = new Format("com.crealytics.spark.excel");
	public static final Format XLSX = new Format("com.crealytics.spark.excel");
	public static final Format PARQUET = new Format("PARQUET");
	public static final Format AVRO = new Format("avro");
	public static final Format SNOWFLAKE = new Format("net.snowflake.spark.snowflake");
	public static final Format TEXT = new Format("text");
	public static final Format BIGQUERY = new Format("bigquery");
	public static final Format INMEMORY = new Format("inMemory");
	
	String type;
	static Map<String, Format> map;
	
	private Format(String type) {
		this.type = type;
	}
	
	static {
		map = new HashMap<String, Format>();
		addFormat(CSV);
		addFormat(JSON);
		addFormat(JDBC);
		addFormat(ELASTIC);
		addFormat(CASSANDRA);
		addFormat(XLS);
		addFormat(XLSX);
		addFormat(PARQUET);
		addFormat(AVRO);
		addFormat(SNOWFLAKE);
		addFormat(TEXT);
		addFormat(BIGQUERY);
		addFormat(INMEMORY);
	}

	@JsonCreator
	public static Format getPipeType(String t) {
		return map.get(t.toLowerCase());
	}

	public static void setPipeType(String t) {
		map.put(t.toLowerCase(), new Format(t.toLowerCase()));
	}

	public static Format getFormat(String t) {
		Format format = map.get(t.toLowerCase());
		if (format == null) {
			format = new Format(t);
			addFormat(format);
 		}
		return format;
	}

	public static void addFormat(Format f) {
		map.put(f.type().toLowerCase(), f);
	}

	@JsonValue
	public String type() {
		return type;
	}

	@Override
	public String toString() {
		return type;
	}
	
	
	
}
