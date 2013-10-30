package eu.lod2.rsine.remotenotification;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import javax.naming.ServiceUnavailableException;

public class UnavailableRemoteServiceDetector implements IRemoteServiceDetector {

    @Override
    public URI getRemoteService(Resource resource) throws ServiceUnavailableException {
        throw new ServiceUnavailableException("Not available by definition");
    }

}
