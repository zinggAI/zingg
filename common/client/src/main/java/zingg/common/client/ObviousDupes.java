package zingg.common.client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObviousDupes implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(ObviousDupes.class);
	
	public static final String fieldName = "fieldName";
	
	public ObviousDupes() {
		
	}
	
	public ObviousDupes(HashMap<String, String>[] matchCondition) {
		this.matchCondition = matchCondition;
	}

	HashMap<String,String>[]  matchCondition;

	public HashMap<String, String>[] getMatchCondition() {
		return matchCondition;
	}

	public void setMatchCondition(HashMap<String, String>[] matchCondition) {
		this.matchCondition = matchCondition;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(matchCondition);
	}
	
}
