package zingg;

import java.io.Serializable;
import java.util.List;

import zingg.common.client.util.Util;
import scala.Tuple2;

public class FebrlRecord implements Serializable{
	String key;
	String line;
	//id is 0 for org, 1,2 for dup-0 etc
	int id;
	
	FebrlRecord(String key, int id, String line) {
		this.key = key;
		this.id = id;
		this.line = line;
	}
	
	/**
	 * (1020,FebrlRecord [key=1020, line=rec-1020-org, id=100])
		(1021,FebrlRecord [key=1021, line=rec-1021-dup-0, id=1])
		(1021,FebrlRecord [key=1021, line=rec-1021-org, id=100])
		(1022,FebrlRecord [key=1022, line=rec-1022-dup-0, id=1])
	 * parses the gold record into
	 * key - portion from rec-1024-org, key in this case is 1024
	 * id denotes org/dup0 etc. for org it is 100, dup-0 is 1, dup-2 is 21 etc
	 * line is full rec id
	 * 
	 * <String,FebrlRecord> - here String is the key
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	static Tuple2<String,FebrlRecord> parse(String line) throws Exception{
		List<String> cols = Util.parse(line, ",");
		String[] firstColSplit = cols.get(0).split("-"); 
		String key = firstColSplit[1];
		int id = 100;
		if (firstColSplit.length > 3) id = Integer.parseInt(firstColSplit[3]+1); 
		return new Tuple2<String, FebrlRecord>(key, new FebrlRecord(key, id, cols.get(0)));		
	}
	
	/*
	 * (0,FebrlRecord [key=0, line=rec-1022-dup-0, id=0])
	 * id is zipIndex
	 * key is cluster id
	 */
	static Tuple2<String,FebrlRecord> parseOutput(Tuple2<String, Long>t) throws Exception{
		List<String> cols = Util.parse(t._1(), ",");
		int id = t._2().intValue();
		String rec = cols.get(1);
		return new Tuple2<String, FebrlRecord>(cols.get(0), new FebrlRecord(cols.get(0), id, rec));		
	}

	/**
	 * prevent self record join
	 * @param rec
	 * @return
	 */
	static boolean isValidPair(Tuple2<FebrlRecord, FebrlRecord> rec) {
		if (rec._1().getId() != rec._2().getId()) return true;
		return false;
		//return true;
	}
	
	static String prettyPair(Tuple2<FebrlRecord,FebrlRecord> t) {
		FebrlRecord first = t._1();
		FebrlRecord second = t._2();
		return first.getLine() + ";" + second.getLine();
	}
	
	public int getId() {
		return id;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FebrlRecord other = (FebrlRecord) obj;
		if (id != other.id)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FebrlRecord [key=" + key + ", line=" + line + ", id=" + id
				+ "]";
	}
	
	
	
	
	
}