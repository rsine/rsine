package at.punkt.lod2.remote;

import eu.lod2.rsine.remotenotification.IRemoteServiceDetector;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class LoopBackRemoteServiceDetector implements IRemoteServiceDetector {

    private int remoteInstanceListeningPort;

    public LoopBackRemoteServiceDetector(int remoteInstanceListeningPort) {
        this.remoteInstanceListeningPort = remoteInstanceListeningPort;
    }

    @Override
    public URI getRemoteService(Resource resource) {
        return new URIImpl("http://localhost:" +remoteInstanceListeningPort);
    }

}