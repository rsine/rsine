package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Model;

public class NullRemoteNotificationService extends RemoteNotificationServiceBase {

    @Override
    public void announce(Model changeSet) {
    }

}
