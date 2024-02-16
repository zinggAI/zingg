package zingg.common.client.event.listeners;

import zingg.common.client.event.events.IEvent;
import zingg.common.client.util.ListMap;

public class EventsListener {
    private static EventsListener eventsListener = null;
    private final ListMap<Class<? extends IEvent>, IEventListener> eventListeners;

    private EventsListener() {
        eventListeners = new ListMap<>();
    }

    public static EventsListener getInstance() {
        if (eventsListener == null)
            eventsListener = new EventsListener();
        return eventsListener;
    }

    public void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        eventListeners.add(eventClass, listener);
    }

    public void fireEvent(IEvent event) {
        listen(event);
    }

    private void listen(IEvent event) {
        Class<? extends IEvent> eventClass = event.getClass();
        for (IEventListener listener : eventListeners.get(eventClass)) {
            listener.listen(event);
        }
    }
}
