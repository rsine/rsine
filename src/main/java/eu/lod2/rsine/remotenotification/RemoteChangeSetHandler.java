package eu.lod2.rsine.remotenotification;

import eu.lod2.rsine.changesetservice.PostRequestHandler;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class RemoteChangeSetHandler extends PostRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(RemoteChangeSetHandler.class);

    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;

    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {
        try {
            Model changeSet = parseChangeSet(request.getEntity().getContent());
            changeSetStore.persistChangeSet(changeSet);
            queryDispatcher.trigger();
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

    private Model parseChangeSet(InputStream remoteChangeSetContent) throws OpenRDFException, IOException {
        RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
        Model changeSet = new TreeModel();
        StatementCollector collector = new StatementCollector(changeSet);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(remoteChangeSetContent, "");
        return changeSet;
    }

    public void setQueryDispatcher(IQueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
    }

    public void setChangeSetStore(ChangeSetStore changeSetStore) {
        this.changeSetStore = changeSetStore;
    }

}
