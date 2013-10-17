package eu.lod2.rsine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CmdParams {

    private final Logger logger = LoggerFactory.getLogger(CmdParams.class);

    private JCommander jc;

    @Parameter(names = {"-a", "--authoritative-uri"}, description = "URI scheme of local resources")
    public String authoritativeUri;

    @Parameter(names = {"-h", "--help"}, description = "Outputs commannd line parameter description")
    boolean help = false;

    @Parameter(names = {"-s", "--sparql-endpoint"}, description = "URI of managed store SPARQL endpoint")
    public String managedStoreSparqlEndpoint = "";

    @Parameter(names = {"-c", "--changes-port"}, description = "Port where rsine listens for incoming triple store changes")
    public Integer changesListeningPort;

    CmdParams(String[] args) {
        jc = new JCommander(this);
        jc.setProgramName("rsine");
        jc.parse(args);

        if (help) {
            jc.usage();
        }
        else {
            if (checkParams()) {
                logParamValues();
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
            logger.info("Provide missing parameters either on command line or in the configuration file " +Rsine.propertiesFileName);
        }
        return !incompleteParams;
    }

    private <T> T getFromProps(T obj) {
        return null;
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