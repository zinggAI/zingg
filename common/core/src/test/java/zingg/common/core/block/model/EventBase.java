package zingg.common.core.block.model;

public class EventBase {
    
    public final Integer id;
    public final Integer year;
    public final String event;
    public final String comment;

    public EventBase(Integer id, Integer year, String event, String comment) {
        this.id = id;
        this.year = year;
        this.event = event;
        this.comment = comment;
    }
}
