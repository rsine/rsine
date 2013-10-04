package eu.lod2.rsine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.PostRequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.NullRemoteNotificationService;
import eu.lod2.rsine.remotenotification.RemoteNotificationService;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Assembles an Rsine instance from its components
 * TODO: use spring ioc for this
 */
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final Logger logger = LoggerFactory.getLogger(Rsine.class);

    private static JCommander jc;
    private ChangeSetService changeSetService;
    private RegistrationService registrationService;
    private QueryDispatcher queryDispatcher;
    private RemoteNotificationServiceBase remoteNotificationService = new NullRemoteNotificationService();
    private ChangeSetStore changeSetStore;

    @Parameter(names = {"-a", "--authoritative-uri"}, description = "URI scheme of local resources")
    private String authoritativeUri;

    @Parameter(names = {"-h", "--help"}, description = "Outputs commannd line parameter description")
    private boolean help = false;

    @Parameter(names = {"-s", "--sparql-endpoint"}, description = "URI of managed store SPARQL endpoint")
    private String managedStoreSparqlEndpoint;

    @Parameter(names = {"-c", "--changes-port"}, description = "Port where rsine listens for incoming triple store changes")
    private Integer changesListeningPort;

    private Rsine() {
    }

    private void init() {
        changeSetService = new ChangeSetService(changesListeningPort);
        registrationService = new RegistrationService();
        queryDispatcher = new QueryDispatcher();
        changeSetStore = new ChangeSetStore();

        queryDispatcher.setRegistrationService(registrationService);
        queryDispatcher.setManagedTripleStore(managedStoreSparqlEndpoint);
        queryDispatcher.setChangeSetStore(changeSetStore);

        PostRequestHandlerFactory handlerFactory = PostRequestHandlerFactory.getInstance();
        handlerFactory.setChangeSetCreator(new ChangeSetCreator());
        handlerFactory.setChangeSetStore(changeSetStore);
        handlerFactory.setQueryDispatcher(queryDispatcher);
        handlerFactory.setRemoteNotificationService(remoteNotificationService);
        handlerFactory.setRegistrationService(registrationService);                
    }

    /**
     * Convenience constructor for testing
     */
    public Rsine(int changesListeningPort,
                 String managedStoreSparqlEndpoint)
    {
        this();
        this.changesListeningPort = changesListeningPort;
        this.managedStoreSparqlEndpoint = managedStoreSparqlEndpoint;
        init();
    }

    /**
     * Convenience constructor for testing
     */
    public Rsine(int changesListeningPort,
                 String managedStoreSparqlEndpoint,
                 String authoritativeUri)
    {
        this(changesListeningPort, managedStoreSparqlEndpoint);
        this.authoritativeUri = authoritativeUri;
        createRemoteNotificationService();
        init();
    }

    public void start() throws IOException, RepositoryException {
        changeSetService.start();
    }

    public void stop() throws IOException, InterruptedException {
        changeSetService.stop();
    }

    public static void main(String[] args) throws IOException, RepositoryException {
        Rsine rsine = new Rsine();
        rsine.parseParams(args);

        if (rsine.help) {
            jc.usage();
        }
        else {
            if (rsine.checkParams()) {
                rsine.logParamValues();
                rsine.createRemoteNotificationService();
                rsine.init();
                rsine.start();
            }
        }
    }

    private void parseParams(String[] args) {
        jc = new JCommander(this);
        jc.setProgramName("rsine");
        jc.parse(args);

        try {
            loadParamsFromProperties();
        }
        catch (IOException e) {
            logger.error("Cannot load parameters from properties file " +propertiesFileName);
        }
    }

    private void loadParamsFromProperties() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(propertiesFileName);
        properties.load(stream);

        if (authoritativeUri == null) {
            authoritativeUri = (String) properties.get("managedstore.authUri");
        }
        if (managedStoreSparqlEndpoint == null) {
            managedStoreSparqlEndpoint = (String) properties.get("managedstore.endpoint");
        }
        if (changesListeningPort == null) {
            try {
                changesListeningPort = Integer.parseInt((String) properties.get("changes.port"));
            }
            catch (NumberFormatException e) {
                changesListeningPort = null;
            }
        }
    }

    private boolean checkParams() {
        boolean incompleteParams = false;
        if (managedStoreSparqlEndpoint == null) {
            logger.error("No SPARQL endpoint of the managed triple store provided");
            incompleteParams = true;
        }
        if (changesListeningPort == null) {
            logger.error("No change listening port provided");
            incompleteParams = true;
        }

        if (incompleteParams) {
            logger.info("Provide missing parameters either on command line or in the configuration file " +propertiesFileName);
        }
        return !incompleteParams;
    }

    private void logParamValues() {
        logger.info("Listening for changeset from managed store on port " +changesListeningPort);
        logger.info("SPARQL endpoint of managed store is set to " +managedStoreSparqlEndpoint);
        if (authoritativeUri == null) {
            logger.info("No remote change notification configured (authoritative uri not set)");
        }
        else {
            logger.info("Authoritative URI set to " +authoritativeUri);
        }
    }

    private void createRemoteNotificationService() {
        if (authoritativeUri != null) {
            remoteNotificationService = new RemoteNotificationService();
            ((RemoteNotificationService) remoteNotificationService).setAuthoritativeUri(authoritativeUri);
        }
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
