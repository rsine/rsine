package eu.lod2.rsine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final Logger logger = LoggerFactory.getLogger(Rsine.class);

    private static JCommander jc;

    @Autowired
    private ChangeSetService changeSetService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RemoteNotificationServiceBase remoteNotificationService;

    public static CmdParams cmdParams;

    public Rsine() {
    }

    public void start() throws IOException, RepositoryException {
        changeSetService.start();
    }

    public void stop() throws IOException, InterruptedException {
        changeSetService.stop();
    }

    public static void main(String[] args) throws IOException, RepositoryException {
        cmdParams = new CmdParams(args);
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-cmdbased-context.xml");
        Rsine rsine = (Rsine) applicationContext.getBean("rsine");
        rsine.start();
    }

    /**
     * @deprecated registration should be exposed as an HTTP service; for testing only
     */
    public void registerSubscription(Subscription subscription) {
        registrationService.register(subscription);
    }

    private static class CmdParams {

        private final Logger logger = LoggerFactory.getLogger(CmdParams.class);

        @Parameter(names = {"-a", "--authoritative-uri"}, description = "URI scheme of local resources")
        String authoritativeUri;

        @Parameter(names = {"-h", "--help"}, description = "Outputs commannd line parameter description")
        boolean help = false;

        @Parameter(names = {"-s", "--sparql-endpoint"}, description = "URI of managed store SPARQL endpoint")
         String managedStoreSparqlEndpoint = "";

        @Parameter(names = {"-c", "--changes-port"}, description = "Port where rsine listens for incoming triple store changes")
        Integer changesListeningPort;

        CmdParams(String[] args) {
            jc = new JCommander(this);
            jc.setProgramName("rsine");
            jc.parse(args);

            if (help) {
                jc.usage();
            }
            else if (checkParams()) {
                logParamValues();
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
    }

}
