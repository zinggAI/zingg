package zingg.common.client.event.listeners;

import zingg.common.client.event.events.IEvent;

public class ZinggStopListener extends IEventListener {

    @Override
    public void listen(IEvent event) {
        System.out.println("ZinggStopListener: I am listening");
    }
}
