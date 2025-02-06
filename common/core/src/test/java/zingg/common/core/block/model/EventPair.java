package zingg.common.core.block.model;

import zingg.common.core.model.EventBase;

public class EventPair extends EventBase{
    public final Integer z_year;
    public final String z_event;
    public final String z_comment;
    public final Long z_zid;

    public EventPair(Integer id, Integer year, String event, String comment, Integer z_year, String z_event, String z_comment, Long z_zid) {
        super(id, year, event, comment);
        this.z_year = z_year;
        this.z_event = z_event;
        this.z_comment = z_comment;
        this.z_zid = z_zid;
    }
}