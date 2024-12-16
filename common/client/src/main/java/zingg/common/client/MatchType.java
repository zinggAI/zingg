package zingg.common.client;

import java.io.Serializable;

/**
 * Field types used in defining the types of fields for matching. See the field
 * definitions and the user guide for more details
 */

public class MatchType implements IMatchType, Serializable{

	private static final long serialVersionUID = 1L;
	protected String name;

	public MatchType(String n){
		this.name = n;
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
	public boolean isEqual(String v) {
		if(this.getName().equalsIgnoreCase(v)){
			return true;
		}
		else
			return false;
	}


}
