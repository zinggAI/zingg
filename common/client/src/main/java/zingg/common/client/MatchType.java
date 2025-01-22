package zingg.common.client;

import java.io.Serializable;

/**
 * Field types used in defining the types of fields for matching. See the field
 * definitions and the user guide for more details
 */

public class MatchType implements IMatchType, Serializable{

	private static final long serialVersionUID = 1L;
	public String name;

	public MatchType(){
		
	}

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		MatchType other = (MatchType) obj;
		if (name == null) {
			if (other.name != null){
				return false;
			}
		} 
		else if (!name.equalsIgnoreCase(other.name)){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
