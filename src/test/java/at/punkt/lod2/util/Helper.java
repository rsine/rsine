package at.punkt.lod2.util;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

public class Helper {

    private final static Logger logger = LoggerFactory.getLogger(Helper.class);
    private int changeSetListeningPort;

    public Helper(int changeSetListeningPort) {
        this.changeSetListeningPort = changeSetListeningPort;
    }

    public int postSubscriptionTtl(String fileName) throws IOException {
        InputStream subscriptionRdfContent = Rsine.class.getResourceAsStream(fileName);
        StringWriter writer = new StringWriter();
        IOUtils.copy(subscriptionRdfContent, writer);

        HttpPost httpPost = new HttpPost("http://localhost:" +changeSetListeningPort+ "/register");
        httpPost.setEntity(new StringEntity(writer.toString()));
        httpPost.setHeader("Content-Type", "text/turtle");
        HttpResponse response = new DefaultHttpClient().execute(httpPost);
        return response.getStatusLine().getStatusCode();
    }

    public void postStatementAdded(Statement statement) throws IOException, RDFHandlerException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
                ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
                createNtriplesRepresentation(statement));
        postChangeset(props);
    }

    private String createNtriplesRepresentation(Statement statement) throws RDFHandlerException {
        StringWriter sw = new StringWriter();
        RDFWriter writer = new NTriplesWriterFactory().getWriter(sw);

        writer.startRDF();
        writer.handleStatement(statement);
        writer.endRDF();

        return sw.toString();
    }

    public int postChangeset(Properties properties) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:" +changeSetListeningPort);
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        httpPost.setEntity(new StringEntity(sw.toString()));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        return response.getStatusLine().getStatusCode();
    }

    public void setAltLabel(DatasetGraph datasetGraph, org.openrdf.model.URI concept, Literal newAltLabel)
        throws IOException, RDFHandlerException
    {
        datasetGraph.getDefaultGraph().add(new Triple(
                NodeFactory.createURI(concept.stringValue()),
                NodeFactory.createURI(SKOS.ALT_LABEL.stringValue()),
                NodeFactory.createLiteral(newAltLabel.getLabel(), newAltLabel.getLanguage(), false)));
        postStatementAdded(new StatementImpl(concept, SKOS.ALT_LABEL, newAltLabel));
    }

    public static void initFuseki(String datasetName) {
        initFuseki(DatasetGraphFactory.createMem(), datasetName);
    }

    private static void initFuseki(DatasetGraph datasetGraph, String datasetName) {
        ServerConfig serverConfig = FusekiConfig.defaultConfiguration(datasetName, datasetGraph, true) ;
        SPARQLServer fusekiServer = new SPARQLServer(serverConfig) ;
        Fuseki.setServer(fusekiServer);
        fusekiServer.start();
    }

    public static DatasetGraph initFuseki(URL rdfFile, String datasetName) {
        return initFuseki(Arrays.asList(new File(rdfFile.getFile()).toURI()), datasetName);
    }

    public static DatasetGraph initFuseki(Collection<URI> rdfFiles, String datasetName) {
        DatasetGraph datasetGraph = DatasetGraphFactory.createMem();
        for (URI rdfFile : rdfFiles) {
            try {
                RDFDataMgr.read(datasetGraph, rdfFile.toString());
            }
            catch (Exception e) {
                // ignore
            }
        }
        initFuseki(datasetGraph, datasetName);
        return datasetGraph;
    }

    public static Model createModelFromResourceFile(String fileName, RDFFormat format)
        throws RDFParseException, IOException, RDFHandlerException
    {
        RDFParser rdfParser = Rio.createParser(format);
        Model model = new TreeModel();
        StatementCollector collector = new StatementCollector(model);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(Rsine.class.getResourceAsStream(fileName), "");
        return model;
    }

    public int getChangeSetListeningPort() {
        return changeSetListeningPort;
    }

}
