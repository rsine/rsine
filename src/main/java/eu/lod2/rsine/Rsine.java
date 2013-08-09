package eu.lod2.rsine;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.NullRemoteNotificationService;
import eu.lod2.rsine.remotenotification.RemoteNotificationService;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
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
    private RequestHandlerFactory requestHandlerFactory;
    private RemoteNotificationServiceBase remoteNotificationService;

    /**
     * Creates an Rsine instance for local use only
     */
    public Rsine(int managedStoreChangesListeningPort,
                 String managedStoreSparqlEndpoint)
    {
        changeSetService = new ChangeSetService(managedStoreChangesListeningPort);
        registrationService = new RegistrationService();
        queryDispatcher = new QueryDispatcher();
        remoteNotificationService = new NullRemoteNotificationService();
        ChangeSetStore changeSetStore = new ChangeSetStore();

        queryDispatcher.setRegistrationService(registrationService);
        queryDispatcher.setManagedTripleStore(managedStoreSparqlEndpoint);
        queryDispatcher.setChangeSetStore(changeSetStore);

        requestHandlerFactory = RequestHandlerFactory.getInstance();
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());
        requestHandlerFactory.setChangeSetStore(changeSetStore);
        requestHandlerFactory.setQueryDispatcher(queryDispatcher);
        requestHandlerFactory.setRemoteNotificationService(remoteNotificationService);
    }

    /**
     * Creates an Rsine instance capable of handling remote notifications
     */
    public Rsine(int managedStoreChangesListeningPort,
                 String managedStoreSparqlEndpoint,
                 String authoritativeUri)
    {
        this(managedStoreChangesListeningPort, managedStoreSparqlEndpoint);
        RemoteNotificationService remoteNotificationService = new RemoteNotificationService();
        this.remoteNotificationService = remoteNotificationService;
        remoteNotificationService.setAuthoritativeUri(authoritativeUri);
        requestHandlerFactory.setRemoteNotificationService(remoteNotificationService);
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

                case 3:
                    new Rsine(Integer.parseInt(args[0]), args[1], args[2]);
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
        System.out.println("Parameters: managedStoreChangesListeningPort managedStoreSparqlEndpoint [authoritativeUri]");
    }

    public RemoteNotificationServiceBase getRemoteNotificationService() {
        return remoteNotificationService;
    }

    /**
     * @deprecated registration should be exposed as an HTTP service; for testing only
     */
    public void registerSubscription(Subscription subscription) {
        registrationService.register(subscription);
    }

}
