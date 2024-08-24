package zingg.common.core.zFrame.model;

public class Person {
    public final String recid;
    public final String givenname;
    public final String surname;
    public final String suburb;
    public final String postcode;

    public Person(String recid, String givename, String surname, String suburb, String postcode) {
        this.recid = recid;
        this.givenname = givename;
        this.surname = surname;
        this.suburb = suburb;
        this.postcode = postcode;
    }
}
