package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class NullRemoteServiceDetector implements IRemoteServiceDetector {

    @Override
    public URI getRemoteService(Resource resource) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
