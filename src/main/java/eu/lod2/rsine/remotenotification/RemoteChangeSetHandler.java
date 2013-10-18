package eu.lod2.rsine.remotenotification;

import eu.lod2.rsine.changesetservice.PersistAndNotifyProvider;
import eu.lod2.rsine.changesetservice.PostRequestHandler;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Scope("prototype")
public class RemoteChangeSetHandler extends PostRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(RemoteChangeSetHandler.class);

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {
        try {
            Model changeSet = parseChangeSet(request.getEntity().getContent());
            persistAndNotifyProvider.persistAndNotify(changeSet, true);
        }
        catch (IOException e) {
            logger.error("Error reading remote changeset", e);
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

}
