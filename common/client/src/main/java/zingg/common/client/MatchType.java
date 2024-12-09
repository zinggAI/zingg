package zingg.common.client;

/**
 * Field types used in defining the types of fields for matching. See the field
 * definitions and the user guide for more details
 */

public class MatchType implements IMatchType {

	private String value;
	private String name;

	MatchType(String n){
		this.name = n;
		this.value = n;
	}

	MatchType(String n, String v){
		this.name = n;
		this.value = v;
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
