package zingg.model;

public class EventCluster {
    public final Integer id;
    public final Integer year;
    public final String event;
    public final String comment;
    public final Integer z_year;
    public final String z_event;
    public final String z_comment;
    public final Long z_zid;

    public EventCluster(Integer id, Integer year, String event, String comment, Integer z_year, String z_event, String z_comment, Long z_zid) {
        this.id = id;
        this.year = year;
        this.event = event;
        this.comment = comment;
        this.z_year = z_year;
        this.z_event = z_event;
        this.z_comment = z_comment;
        this.z_zid = z_zid;
    }
}