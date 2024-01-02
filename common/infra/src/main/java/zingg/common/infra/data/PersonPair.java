package zingg.common.infra.data;

public class PersonPair extends Person {

    Integer z_zid;
    String z_name;
    String z_event;
    String z_comment;
    Integer z_year;
    Integer z_dob;
    Integer z_hash = -1;
    String z_source = "test";

    public PersonPair(Integer id, String name, String event, String comment, Integer year, Integer dob, Integer z_id, String z_name, String z_event, String z_comment, Integer z_year, Integer z_dob){
        super(id, name, event, comment, year, dob);
        this.z_zid = z_id;
        this.z_name = z_name;
        this.z_event = z_event;
        this.z_comment = z_comment;
        this.z_year = z_year;
        this.z_dob = z_dob;
    }

    public PersonPair(Integer id, String name, String event, String comment, Integer year, Integer dob, Integer hash, String source,
        Integer z_id, String z_name, String z_event, String z_comment, Integer z_year, Integer z_dob, Integer z_hash, String z_source){
        this(id, name, event, comment, year, dob, z_id, z_name, z_event, z_comment, z_year, z_dob);
        this.hash = hash;
        this.z_hash = z_hash;
        this.source = source;
        this.z_source = source;
    }
}
