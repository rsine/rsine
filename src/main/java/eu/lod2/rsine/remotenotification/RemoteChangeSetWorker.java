package eu.lod2.rsine.remotenotification;

import eu.lod2.rsine.changesetservice.PersistAndNotify;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.IOException;
import java.io.InputStream;

public class RemoteChangeSetWorker {

    private static final RemoteChangeSetWorker instance = new RemoteChangeSetWorker();

    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;

    private RemoteChangeSetWorker() {
    }

    public static RemoteChangeSetWorker getInstance() {
        return instance;
    }

    public void handleRemoteChangeSet(InputStream postedRemoteChangeSet) throws IOException, OpenRDFException {
        Model changeSet = parseChangeSet(postedRemoteChangeSet);
        new PersistAndNotify(changeSet, changeSetStore, queryDispatcher).start();
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
