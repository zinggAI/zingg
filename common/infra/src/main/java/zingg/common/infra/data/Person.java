package zingg.common.infra.data;

public class Person {

    protected Integer zid;
    protected String name;
    protected String event;
    protected String comment;
    protected Integer year;
    protected Integer dob;
    protected Integer hash = -1;
    protected String source = "test";

    public Person(Integer id, String name, String event, String comment, Integer year, Integer dob){
        this.zid = id;
        this.name = name;
        this.event = event;
        this.comment = comment;
        this.year = year;
        this.dob = dob;
    }

    public Person(Integer id, String name, String event, String comment, Integer year, Integer dob, Integer hash){
        this(id, name, event, comment, year, dob);
        this.hash = hash;
    }

    public Person(Integer id, String name, String event, String comment, Integer year, Integer dob, Integer hash, String source){
        this(id, name, event, comment, year, dob, hash);
        this.source = source;
    }
}
