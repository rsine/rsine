package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * Assembles an Rsine instance from its components
 * TODO: use spring ioc for this
 */
public class Rsine {

    private RegistrationService registrationService;
    private ChangeSetStore changeSetStore;

    public Rsine() throws IOException, RepositoryException {
        registrationService = new RegistrationService();
        changeSetStore = new ChangeSetStore();

        ChangeSetService changeSetService = new ChangeSetService(8080);
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory();
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());
        requestHandlerFactory.setChangeSetStore(changeSetStore);

        QueryDispatcher queryDispatcher = new QueryDispatcher();
        queryDispatcher.setRegistrationService(registrationService);
        queryDispatcher.setRepository(changeSetStore.getRepository());
        requestHandlerFactory.setQueryDispatcher(queryDispatcher);

        changeSetService.setRequestHandlerFactory(requestHandlerFactory);

        changeSetService.start();
    }

    /**
     * @deprecated registration should be exposed as an HTTP service
     */
    public Subscription requestSubscription() {
        return registrationService.requestSubscription();
    }

    /**
     * @deprecated registration should be exposed as an HTTP service
     */
    public void registerSubscription(Subscription subscription) {
        registrationService.register(subscription);
    }

    /**
     * Loads the passed rdf content into the ChangeSetStore. This is done for the proof-of-concept only. Final versions
     * will access the managed triple store instance directly.
     * @deprecated rsine todo: change to work with an openrdf repository
     */
    public void setManagedTripleStoreContent(File rdfData) throws RepositoryException, IOException, RDFParseException {
        changeSetStore.getRepository().getConnection().add(rdfData, null, null, new URIImpl(Namespaces.VOCAB_CONTEXT));
    }

}
