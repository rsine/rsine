package eu.lod2.rsine.service;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.queryhandling.IQueryDispatcher;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import eu.lod2.util.Namespaces;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersistAndNotifyProvider {

    private final Logger logger = LoggerFactory.getLogger(PersistAndNotifyProvider.class);
    private URI lastPersistedChangeSetUri;
    
    @Autowired
    private ChangeSetStore changeSetStore;

    @Autowired
    private IQueryDispatcher queryDispatcher;

    @Autowired
    private RemoteNotificationServiceBase remoteNotificationService;

    public synchronized void persistAndNotify(Model changeSet, boolean notifyOnlyLocal) {
        try {
            persistChangeSet(changeSet);
            
            queryDispatcher.trigger();
            if (!notifyOnlyLocal) remoteNotificationService.announce(changeSet);            
        }
        catch (RepositoryException e) {
            logger.error("Error persisting changeset to changeset store", e);
        }
    }
    
    private void persistChangeSet(Model changeSet) throws RepositoryException {
        addPrecedingChangeSet(changeSet);
        changeSetStore.persistChangeSet(changeSet);            
        lastPersistedChangeSetUri = getChangeSetUri(changeSet);
    }
    
    private void addPrecedingChangeSet(Model changeSet) {
        if (lastPersistedChangeSetUri != null) {
            changeSet.add(new StatementImpl(getChangeSetUri(changeSet),
                                            new URIImpl(Namespaces.CS_NAMESPACE.getName() + "precedingChangeSet"),
                                            lastPersistedChangeSetUri));            
        }
    }
    
    private URI getChangeSetUri(Model changeSet) {
        Model typeSubjects = changeSet.filter(null, RDF.TYPE, new URIImpl(Namespaces.CS_NAMESPACE.getName() + "ChangeSet"));
        return (URI) typeSubjects.subjects().iterator().next();
    }

}
