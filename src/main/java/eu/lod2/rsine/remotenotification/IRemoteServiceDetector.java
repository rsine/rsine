package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public interface IRemoteServiceDetector {

    public URI getRemoteService(Resource resource);

}
