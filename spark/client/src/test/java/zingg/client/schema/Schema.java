package zingg.client.schema;

public class Schema {
	public final String recid;
	public final String givenname;
	public final String surname;
	public final String suburb;
	public final String postcode;
	
	public Schema(String recid, String givename, String surname, String suburb, String postcode) {
		this.recid = recid;
		this.givenname = givename;
		this.surname = surname;
		this.suburb = suburb;
		this.postcode = postcode;
	}
}
