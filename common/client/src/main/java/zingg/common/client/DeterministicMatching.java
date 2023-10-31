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
	
	public DeterministicMatching() {
		
	}
	
	public DeterministicMatching(HashMap<String, String>[] matchCondition) {
		this.matchCondition = matchCondition;
	}

	HashMap<String,String>[]  matchCondition;

	public HashMap<String, String>[] getMatchCondition() {
		return matchCondition;
	}

	public void setMatchCondition(HashMap<String, String>[] matchCondition) {
		this.matchCondition = matchCondition;
		System.out.println("Match condition set to: " + Arrays.toString(this.matchCondition));
	}

	
	public void setMatchCondition(String[] fieldArray) {
		this.matchCondition = new HashMap[fieldArray.length];
		for (int i = 0; i < fieldArray.length; i++) {
			this.matchCondition[i] = new HashMap<String, String>();
			this.matchCondition[i].put(fieldName, fieldArray[i]);
		}
	}
	
	@Override
	public String toString() {
		return Arrays.toString(matchCondition);
	}
	
}
