package zingg.common.client.event.events;

import java.util.HashMap;

public class IEvent {

	protected HashMap<String, Object> eventDataProps;	

    public IEvent() {
		super();
	}

	public IEvent(HashMap<String, Object> eventDataProps) {
		super();
		this.eventDataProps = eventDataProps;
	}

	public HashMap<String, Object> getProps(){
        return eventDataProps;
    }

    public void setProps(HashMap<String, Object> props){
    	this.eventDataProps = props;
    }
}
