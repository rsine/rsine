package eu.lod2.rsine.remotenotification;

public class RemoteNotificationServiceFactory {

    private String authoritativeUri;

    public RemoteNotificationServiceFactory(String authoritativeUri) {
        this.authoritativeUri = authoritativeUri;
    }

    public RemoteNotificationServiceBase createRemoteNotificationService() {
        if (authoritativeUri == null || authoritativeUri.trim().isEmpty()) {
            return new NullRemoteNotificationService();
        }
        else {
            return new RemoteNotificationService(authoritativeUri);
        }

    }

}
