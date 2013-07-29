package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.dissemination.Notifier;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;

/**
 * Assembles an Rsine instance from its components
 * TODO: use spring ioc for this
 */
public class Rsine {

    private RegistrationService registrationService;
    private ChangeSetStore changeSetStore;
    private ChangeSetService changeSetService;
    private QueryDispatcher queryDispatcher;
    private String managedTripleStoreSparqlEndpoint;

    public Rsine(int port) throws IOException, RepositoryException {
        registrationService = new RegistrationService();
        changeSetStore = new ChangeSetStore();

        changeSetService = new ChangeSetService(port);
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory();
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());
        requestHandlerFactory.setChangeSetStore(changeSetStore);

        queryDispatcher = new QueryDispatcher();
        queryDispatcher.setRegistrationService(registrationService);
        queryDispatcher.setRepository(changeSetStore.getRepository());

        queryDispatcher.setNotifier(new Notifier());

        requestHandlerFactory.setQueryDispatcher(queryDispatcher);

        changeSetService.setRequestHandlerFactory(requestHandlerFactory);

        changeSetService.start();
    }

    public void stop() throws IOException, InterruptedException {
        changeSetService.stop();
    }

    public void setNotifier(Notifier notifier) {
        queryDispatcher.setNotifier(notifier);
    }

    public void setManagedTripleStore(String sparqlEndpoint) {
        managedTripleStoreSparqlEndpoint = sparqlEndpoint;
    }

    public static void main(String[] args) throws IOException, RepositoryException {
        Rsine rsine = new Rsine(8080);
        rsine.setManagedTripleStore("localhost");
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

}
