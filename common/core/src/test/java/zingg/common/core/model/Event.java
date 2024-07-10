package zingg.common.core.model;

public class Event {
    public final Integer id;
    public final Integer year;
    public final String event;
    public final String comment;

    public Event(Integer id, Integer year, String event, String comment) {
        this.id = id;
        this.year = year;
        this.event = event;
        this.comment = comment;
    }
}