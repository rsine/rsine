package at.punkt.lod2;

import eu.lod2.rsine.remotenotification.RemoteNotificationService;
import org.junit.Before;
import org.junit.Test;

public class RemoteNotificationServiceTest {

    private RemoteNotificationService reegleRemoteNotificationService, otherRemoteNotificationService;

    @Before
    public void setUp() {
        reegleRemoteNotificationService = new RemoteNotificationService();
        otherRemoteNotificationService = new RemoteNotificationService();

        reegleRemoteNotificationService.setAuthoritativeUri("http://reegle.info/");
    }

    @Test
    public void notificationReceived() {

    }

}
