package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Model;

public abstract class RemoteNotificationServiceBase {

    protected IRemoteServiceDetector remoteServiceDetector;

    public abstract void announce(Model changeSet);

    public final void setRemoteServiceDetector(IRemoteServiceDetector remoteServiceDetector) {
        this.remoteServiceDetector = remoteServiceDetector;
    }

}
