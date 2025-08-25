package zingg.common.client.listener;

import zingg.common.client.event.events.IEvent;
import zingg.common.client.event.events.ZinggStartEvent;
import zingg.common.client.event.events.ZinggStopEvent;
import zingg.common.client.event.listeners.EventsListener;
import zingg.common.client.event.listeners.IEventListener;
import zingg.common.client.event.listeners.ZinggStartListener;
import zingg.common.client.event.listeners.ZinggStopListener;

public class ListenerManager {

    public void initializeListeners() {
        addListener(ZinggStartEvent.class, new ZinggStartListener());
        addListener(ZinggStopEvent.class, new ZinggStopListener());
    }

    protected void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        EventsListener.getInstance().addListener(eventClass, listener);
    }
}
