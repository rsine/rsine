package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.dissemination.Notifier;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.NullRemoteNotificationService;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;

/**
 * Assembles an Rsine instance from its components
 * TODO: use spring ioc for this
 */
public class Rsine {

    private ChangeSetService changeSetService;
    private RegistrationService registrationService;
    private QueryDispatcher queryDispatcher;

    public Rsine(int managedStoreChangesListeningPort,
                 String managedStoreSparqlEndpoint)
    {
        changeSetService = new ChangeSetService(managedStoreChangesListeningPort);
        registrationService = new RegistrationService();
        queryDispatcher = new QueryDispatcher();
        ChangeSetStore changeSetStore = new ChangeSetStore();

        queryDispatcher.setNotifier(new Notifier());
        queryDispatcher.setRegistrationService(registrationService);
        queryDispatcher.setManagedTripleStore(managedStoreSparqlEndpoint);
        queryDispatcher.setChangeSetStore(changeSetStore);

        RequestHandlerFactory requestHandlerFactory = RequestHandlerFactory.getInstance();
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());
        requestHandlerFactory.setChangeSetStore(changeSetStore);
        requestHandlerFactory.setQueryDispatcher(queryDispatcher);
        requestHandlerFactory.setRemoteNotificationService(new NullRemoteNotificationService());
    }


    public Rsine(int managedStoreChangesListeningPort,
                 String managedStoreSparqlEndpoint,
                 int remoteChangeSetListeningPort,
                 URI authoritativeUri)
    {
        this(managedStoreChangesListeningPort, managedStoreSparqlEndpoint);
    }


    public void start() throws IOException, RepositoryException {
        changeSetService.start();
    }

    public void stop() throws IOException, InterruptedException {
        changeSetService.stop();
    }

    public static void main(String[] args) throws IOException, RepositoryException {
        Rsine rsine = null;

        try {
            switch (args.length) {
                case 2:
                    new Rsine(Integer.parseInt(args[0]), args[1]);
                    break;

                case 4:
                    new Rsine(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), new URIImpl(args[3]));
                    break;

                default:
                    throw new Exception("Illegal parameter count");
            }
            rsine.start();
        }
        catch (Exception e) {
            usage();
        }
    }

    private static void usage() {
        System.out.println("Parameters: managedStoreChangesListeningPort managedStoreSparqlEndpoint [remoteChangeSetListeningPort authoritativeUri]");
    }

    public void setNotifier(Notifier notifier) {
        queryDispatcher.setNotifier(notifier);
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
