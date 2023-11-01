package zingg.common.client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeterministicMatching implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(DeterministicMatching.class);
	
	public static final String fieldName = "fieldName";
	
	HashMap<String,String>[]  matchCondition;
	
	public DeterministicMatching() {
		
	}	
	
	public DeterministicMatching(HashMap<String, String>[] matchCondition) {
		this.matchCondition = matchCondition;
	}
	
	public DeterministicMatching(String[] fieldNamesArray) {
		this.matchCondition = new HashMap[fieldNamesArray.length];
		for (int i = 0; i < fieldNamesArray.length; i++) {
			this.matchCondition[i] = new HashMap<String, String>();
			this.matchCondition[i].put(fieldName, fieldNamesArray[i]);
		}
	}

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
