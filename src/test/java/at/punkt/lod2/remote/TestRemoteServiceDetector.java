package at.punkt.lod2.remote;

import eu.lod2.rsine.remotenotification.IRemoteServiceDetector;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class TestRemoteServiceDetector implements IRemoteServiceDetector {

    private int remoteInstanceListeningPort;

    public TestRemoteServiceDetector(int remoteInstanceListeningPort) {
        this.remoteInstanceListeningPort = remoteInstanceListeningPort;
    }

    @Override
    public URI getRemoteService(Resource resource) {
        return new URIImpl("http://localhost:" +remoteInstanceListeningPort+ "/remote");
    }

}