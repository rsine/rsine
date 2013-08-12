package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.NullRemoteNotificationService;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistAndNotify extends Thread {

    private final Logger logger = LoggerFactory.getLogger(PersistAndNotify.class);

    private Model changeSet;
    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;
    private RemoteNotificationServiceBase remoteNotificationService;

    public PersistAndNotify(Model changeSet,
                     ChangeSetStore changeSetStore,
                     IQueryDispatcher queryDispatcher)
    {
        this(changeSet, changeSetStore, queryDispatcher, new NullRemoteNotificationService());
    }

    public PersistAndNotify(Model changeSet,
                     ChangeSetStore changeSetStore,
                     IQueryDispatcher queryDispatcher,
                     RemoteNotificationServiceBase remoteNotificationService)
    {
        this.changeSet = changeSet;
        this.changeSetStore = changeSetStore;
        this.queryDispatcher = queryDispatcher;
        this.remoteNotificationService = remoteNotificationService;
    }

    @Override
    public void run() {
        try {
            changeSetStore.persistChangeSet(changeSet);
            queryDispatcher.trigger();
            remoteNotificationService.announce(changeSet);
        }
        catch (RepositoryException e) {
            logger.error("Error persisting changeset to changeset store", e);
        }
    }

}