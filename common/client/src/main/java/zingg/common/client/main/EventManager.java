package zingg.common.client.main;

import zingg.common.client.event.events.IEvent;
import zingg.common.client.event.events.ZinggStartEvent;
import zingg.common.client.event.events.ZinggStopEvent;
import zingg.common.client.event.listeners.EventsListener;
import zingg.common.client.event.listeners.IEventListener;
import zingg.common.client.event.listeners.ZinggStartListener;
import zingg.common.client.event.listeners.ZinggStopListener;

public class EventManager {
    private final EventsListener eventsListener;

    public EventManager() {
        this.eventsListener = EventsListener.getInstance();
        initializeDefaultListeners();
    }

    public void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        eventsListener.addListener(eventClass, listener);
    }

    public void fireEvent(IEvent event) {
        eventsListener.fireEvent(event);
    }

    private void initializeDefaultListeners() {
        addListener(ZinggStartEvent.class, new ZinggStartListener());
        addListener(ZinggStopEvent.class, new ZinggStopListener());
    }
}
