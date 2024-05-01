package zingg.common.client.event.listeners;

import java.util.List;

import zingg.common.client.ZinggClientException;
import zingg.common.client.event.events.IEvent;
import zingg.common.client.util.ListMap;

public class EventsListener {
    private static EventsListener _eventsListener = new EventsListener();
    private final ListMap<String, IEventListener> eventListenersList;

    private EventsListener() {
        eventListenersList = new ListMap<String, IEventListener>();
    }

    public static EventsListener getInstance() {
        return _eventsListener;
    }

    public void addListener(Class<? extends IEvent> eventClass, IEventListener listener) {
        eventListenersList.add(eventClass.getCanonicalName(), listener);
    }

    public void fireEvent(IEvent event) throws ZinggClientException {
        listen(event);
    }

    private void listen(IEvent event) throws ZinggClientException {
        Class<? extends IEvent> eventClass = event.getClass();
        List<IEventListener> listenerList = eventListenersList.get(eventClass.getCanonicalName());
		if (listenerList != null) {
			for (IEventListener listener : listenerList) {
				if (listener != null) {
					listener.listen(event);
				}
			} 
		}
    }
}
