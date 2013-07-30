package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class RemoteNotificationService {

    private String authoritativeUri;

    public void notify(Graph changeSet) {
        Resource extResource = getExternalResource(changeSet);
        if (extResource != null) {
            URI remoteService = getRemoteService(extResource);
            postChangeSet(remoteService);
        }
    }

    private Resource getExternalResource(Graph changeSet) {
        return null;
    }

    private URI getRemoteService(Resource resource) {
        return null;
    }

    private void postChangeSet(URI remoteService) {

    }

    public void setAuthoritativeUri(String authoritativeUri) {
        this.authoritativeUri = authoritativeUri;
    }

}
