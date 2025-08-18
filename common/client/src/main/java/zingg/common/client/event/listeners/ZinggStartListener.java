package zingg.common.client.event.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.event.events.IEvent;

public class ZinggStartListener extends IEventListener {
    public static final Log LOG = LogFactory.getLog(ZinggStartListener.class);

    @Override
    public void listen(IEvent event) {
        LOG.warn("Zingg service started");
    }
    
}
