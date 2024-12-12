package zingg.common.client;

import java.io.Serializable;

/**
 * Field types used in defining the types of fields for matching. See the field
 * definitions and the user guide for more details
 */

public class MatchType implements IMatchType, Serializable{

	private static final long serialVersionUID = 1L;
	private String value;
	private String name;

	public MatchType(String n){
		this.name = n;
		this.value = n;
		MatchTypes.put(this);
	}

	public MatchType(String n, String v){
		this.name = n;
		this.value = v;
		MatchTypes.put(this);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}


}
