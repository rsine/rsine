package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PersistAndNotifyProvider {

    private final Logger logger = LoggerFactory.getLogger(PersistAndNotifyProvider.class);

    @Autowired
    private ChangeSetStore changeSetStore;

    @Autowired
    private IQueryDispatcher queryDispatcher;

    @Autowired
    private RemoteNotificationServiceBase remoteNotificationService;

    private ExecutorService executor;

    public PersistAndNotifyProvider() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void persistAndNotify(Model changeSet, boolean notifyOnlyLocal) {
        try {
            changeSetStore.persistChangeSet(changeSet);
        }
        catch (RepositoryException e) {
            logger.error("Error persisting changeset to changeset store", e);
        }

        //TODO: notification should work as a thread. currently there are some issues, we keep it synchronous by now
        //executor.execute(new NotifyWorker(changeSet, notifyOnlyLocal));
        new NotifyWorker(changeSet, notifyOnlyLocal).run();
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

    private class NotifyWorker implements Runnable {

        private Model changeSet;
        private boolean notifyOnlyLocal;

        NotifyWorker(Model changeSet, boolean notifyOnlyLocal) {
            this.changeSet = changeSet;
            this.notifyOnlyLocal = notifyOnlyLocal;
        }

        @Override
        public void run() {
            queryDispatcher.trigger();
            if (!notifyOnlyLocal) remoteNotificationService.announce(changeSet);
        }

    }

}
