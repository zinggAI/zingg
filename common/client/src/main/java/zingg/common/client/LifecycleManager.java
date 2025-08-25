package zingg.common.client;

import zingg.common.client.event.events.ZinggStartEvent;
import zingg.common.client.event.events.ZinggStopEvent;
import zingg.common.client.event.listeners.EventsListener;

public class LifecycleManager<S, D, R, C> {

    private final IErrorNotifier errorNotifier = new EmailErrorNotifier();

    public void run(Client<S, D, R, C> client) throws ZinggClientException {
        client.init();
        EventsListener.getInstance().fireEvent(new ZinggStartEvent());
        client.execute();
        client.postMetrics();
        client.stop();
    }

    protected void cleanup(Client<S, D, R, C> client, boolean success, ClientOptions options) {
        try {
            EventsListener.getInstance().fireEvent(new ZinggStopEvent());
            if (client != null) client.stop();
            if (!success) System.exit(1);
        } catch (ZinggClientException e) {
            errorNotifier.notify(options, "Error during cleanup", e.getMessage());
            if (!success) System.exit(1);
        }
    }
}
