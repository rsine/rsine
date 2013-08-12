package eu.lod2.rsine.remotenotification;

import eu.lod2.rsine.changesetservice.PostRequestHandler;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RemoteChangeSetHandler extends PostRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(RemoteChangeSetHandler.class);

    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {
        try {
            RemoteChangeSetWorker.getInstance().handleRemoteChangeSet(request.getEntity().getContent());
        }
        catch (IOException e) {
            logger.error("Error reading remote changeset", e);
        }
        catch (RepositoryException e) {
            logger.error("Error persisting remote changeset", e);
        }
        catch (OpenRDFException e) {
            logger.error("Error parsing remote changeset", e);
        }
    }

}
