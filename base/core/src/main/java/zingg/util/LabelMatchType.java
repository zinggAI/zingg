package zingg.util;

import zingg.client.util.ColValues;

public enum LabelMatchType {
	UNDEFINED(ColValues.IS_NOT_KNOWN_PREDICTION, "ARE NOT KNOWN IF MATCH"), 
	DO_NOT_MATCH(ColValues.IS_NOT_A_MATCH_PREDICTION, "DO NOT MATCH"), 
	MATCH(ColValues.IS_MATCH_PREDICTION, "MATCH");

	private Double value;  
	public String msg;

	private LabelMatchType(Double value, String msg){  
		this.value=value; 
		this.msg = msg;
	}

	public static LabelMatchType get(double value) {
		for (LabelMatchType t: LabelMatchType.values()) {
			if (t.value.equals(value)) return t;
		}
		return null;
	}

}