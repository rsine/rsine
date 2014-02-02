package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.queryhandling.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersistAndNotifyProvider {

    private final Logger logger = LoggerFactory.getLogger(PersistAndNotifyProvider.class);

    @Autowired
    private ChangeSetStore changeSetStore;

    @Autowired
    private IQueryDispatcher queryDispatcher;

    @Autowired
    private RemoteNotificationServiceBase remoteNotificationService;

    public synchronized void persistAndNotify(Model changeSet, boolean notifyOnlyLocal) {
        try {
            changeSetStore.persistChangeSet(changeSet);
        }
        catch (RepositoryException e) {
            logger.error("Error persisting changeset to changeset store", e);
        }

        queryDispatcher.trigger();
        if (!notifyOnlyLocal) remoteNotificationService.announce(changeSet);
    }

}
