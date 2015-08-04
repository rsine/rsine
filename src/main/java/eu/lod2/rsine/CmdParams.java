package eu.lod2.rsine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.Properties;

class CmdParams {

    private final Logger logger = LoggerFactory.getLogger(CmdParams.class);

    private Properties properties;
    private JCommander jc;

    @Parameter(names = {"-s", "--sparql-endpoint"}, description = "URI of managed store SPARQL endpoint")
    public String managedStoreSparqlEndpoint;

    @Parameter(names = {"-a", "--authoritative-uri"}, description = "URI scheme of local resources")
    public String authoritativeUri;

    @Parameter(names = {"-p", "--port"}, description = "Port where rsine listens for incoming connections")
    public Integer port = 2221;

    @Parameter(names = {"-h", "--help"}, description = "Outputs commannd line parameter description")
    boolean help = false;

    public String feedbackFileName;

    CmdParams() {
    }

    CmdParams(String[] args) {
        initPropertiesFromFile();

        jc = new JCommander(this);
        jc.setProgramName("rsine");
        jc.parse(args);

        if (help) {
            jc.usage();
        }
        else {
            loadUnsetValuesFromProperties();
            checkParams();
            logParamValues();
        }
    }

    private void initPropertiesFromFile() {
        properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(Rsine.propertiesFileName);

        try {
            properties.load(stream);
        }
        catch (IOException e) {
            logger.warn("Error reading properties file " +Rsine.propertiesFileName);
        }
    }

    private void loadUnsetValuesFromProperties() {
        authoritativeUri = getFromPropsIfNull(authoritativeUri, "managedstore.authUri");
        managedStoreSparqlEndpoint = getFromPropsIfNull(managedStoreSparqlEndpoint, "managedstore.endpoint");
        feedbackFileName = getFromPropsIfNull(feedbackFileName, "feedback.filename");

        if (port == null) {
            try {
                this.port = Integer.parseInt(properties.getProperty("port"));
            }
            catch (Exception e) {
                port = null;
            }
        }

        if (authoritativeUri == null) {
            autoConfAuthUri();
        }
    }

    private String getFromPropsIfNull(String obj, String propertyKey) {
        if (obj == null) {
            return (String) properties.get(propertyKey);
        }
        return obj;
    }

    private void autoConfAuthUri() {
        try {
            if (managedStoreSparqlEndpoint == null) authoritativeUri = "";
            else {
                URI sparqlEndpointUri = new URI(managedStoreSparqlEndpoint);
                authoritativeUri = sparqlEndpointUri.getScheme() + "://" + sparqlEndpointUri.getHost();
            }
        }
        catch (URISyntaxException e) {
            logger.warn("Could not autodetect authoritative URI: managed store sparql endpoint is not a valid URI");
        }
    }

    private void checkParams() {
        boolean incompleteParams = false;

        if (managedStoreSparqlEndpoint == null) {
            logger.info("No SPARQL endpoint of the managed triple store provided");
            managedStoreSparqlEndpoint = "";
        }

        if (port == null) {
            logger.error("No change listening port provided");
            incompleteParams = true;
        }

        if (incompleteParams) {
            throw new InvalidParameterException("Provide missing parameters either on command line or in the configuration file " +Rsine.propertiesFileName);
        }
    }

    private void logParamValues() {
        logger.info("Listening for changeset from managed store on port " + port);
        logger.info("SPARQL endpoint of managed store is set to " +managedStoreSparqlEndpoint);
        if (authoritativeUri == null) {
            logger.warn("Authoritative uri not set. This may cause problems with some notification queries and disables remote notification");
        }
        else {
            logger.info("Authoritative URI set to " +authoritativeUri);
        }
    }
}