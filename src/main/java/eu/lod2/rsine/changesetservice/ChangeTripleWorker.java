package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;

public class ChangeTripleWorker {

    private static final ChangeTripleWorker instance = new ChangeTripleWorker();

    private ChangeSetCreator changeSetCreator;
    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;
    private RemoteNotificationServiceBase remoteNotificationService;

    private ChangeTripleWorker() {
    }

    public static ChangeTripleWorker getInstance() {
        return instance;
    }

    public void handleChangeTripleRequest(
        Statement affectedStatement,
        Statement secondaryStatement,
        String changeType) throws IOException, RDFParseException, RDFHandlerException
    {
        Model changeSet = changeSetCreator.assembleChangeset(affectedStatement, secondaryStatement, changeType);
        new PersistAndNotify(changeSet, changeSetStore, queryDispatcher, remoteNotificationService).start();
    }

    public void setChangeSetCreator(ChangeSetCreator changeSetCreator) {
        this.changeSetCreator = changeSetCreator;
    }

    public void setChangeSetStore(ChangeSetStore changeSetStore) {
        this.changeSetStore = changeSetStore;
    }

    public void setQueryDispatcher(IQueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
    }

    public void setRemoteNotificationService(RemoteNotificationServiceBase remoteNotificationService) {
        this.remoteNotificationService = remoteNotificationService;
    }

}
