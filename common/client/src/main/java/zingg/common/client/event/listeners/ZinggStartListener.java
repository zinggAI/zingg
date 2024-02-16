package zingg.common.client.event.listeners;

import zingg.common.client.event.events.IEvent;

public class ZinggStartListener extends IEventListener {

    @Override
    public void listen(IEvent event) {
        System.out.println("ZinggStartListener: I am listening");
    }
    
}
