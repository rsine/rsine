package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RemoteNotificationServiceBase {

    @Autowired
    protected IRemoteServiceDetector remoteServiceDetector;

    public abstract void announce(Model changeSet);

    public final void setRemoteServiceDetector(IRemoteServiceDetector remoteServiceDetector) {
        this.remoteServiceDetector = remoteServiceDetector;
    }

}
