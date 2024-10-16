package zingg.common.core.zFrame.model;

public class PersonMixed {
    public final Integer recid;
    public final String givenname;
    public final String surname;
    public final Double cost;
    public final Integer postcode;

    public PersonMixed(Integer recid, String givename, String surname, Double cost, Integer postcode) {
        this.recid = recid;
        this.givenname = givename;
        this.surname = surname;
        this.cost = cost;
        this.postcode = postcode;
    }
}