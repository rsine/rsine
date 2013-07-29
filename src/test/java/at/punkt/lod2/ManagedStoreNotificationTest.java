package at.punkt.lod2;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.Notifier;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ManagedStoreNotificationTest {

    private final Logger logger = LoggerFactory.getLogger(ManagedStoreNotificationTest.class);

    private int rsinePort = TestUtils.getRandomPort();
    private SPARQLServer fusekiServer;
    private DatasetGraph datasetGraph;
    private Rsine rsine;
    private ScopeNoteCreatedNotifier scopeNoteCreatedNotifier;

    @Before
    public void setUp() throws IOException, RepositoryException {
        initFuseki();

        rsine = new Rsine(rsinePort);
        rsine.setManagedTripleStore("localhost:3030/dataset");

        scopeNoteCreatedNotifier = new ScopeNoteCreatedNotifier();
        rsine.setNotifier(scopeNoteCreatedNotifier);

        registerUser();
    }

    private void initFuseki() {
        startFuseki();
        uploadData();
    }

    private void startFuseki() {
        datasetGraph = DatasetGraphFactory.createMem();
        ServerConfig serverConfig = FusekiConfig.defaultConfiguration("dataset", datasetGraph, true) ;
        fusekiServer = new SPARQLServer(serverConfig) ;
        Fuseki.setServer(fusekiServer);
        fusekiServer.start();
    }

    private void uploadData() {
        URL vocabUrl = Rsine.class.getResource("/reegle.rdf");
        RDFDataMgr.read(datasetGraph, new File(vocabUrl.getFile()).toURI().toString()) ;
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        fusekiServer.stop();
        rsine.stop();
    }

    private void registerUser() {
        Subscription subscription = rsine.requestSubscription();
        subscription.addQuery(createScopeNoteCreatedQuery());
        rsine.registerSubscription(subscription);
    }

    private String createScopeNoteCreatedQuery() {
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT ?prefLabel ?newScopeNote " +
               "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:addition ?addition . " +
                    "?addition rdf:subject ?concept . " +
                    "?addition rdf:predicate skos:scopeNote . " +
                    "?addition rdf:object ?newScopeNote . "+
                    "SERVICE <http://localhost:3030/dataset/query> { \n" +
                        "?concept skos:prefLabel ?prefLabel . " +
                    "}" +
                    "FILTER(langMatches(lang(?prefLabel), \"en\"))" +
               "}";
    }

    @Test
    public void notificationDissemination() throws IOException {
        triggerQueryExecution();

        Assert.assertEquals("blanching", scopeNoteCreatedNotifier.prefLabel);
        Assert.assertEquals(1, scopeNoteCreatedNotifier.resultCount);
    }

    private void triggerQueryExecution() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/2547> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some additional info\"@en .");

        TestUtils.doPost(rsinePort, props);

    }

    private class ScopeNoteCreatedNotifier extends Notifier {

        private String prefLabel;
        private int resultCount = 0;

        @Override
        public void queryResultsAvailable(BindingSet bs, Subscription subscription) {
            String newScopeNote = ((Literal) bs.getValue("newScopeNote")).getLabel();
            prefLabel = ((Literal) bs.getValue("prefLabel")).getLabel();
            resultCount++;

            logger.info("Scope note '" +newScopeNote+ "' has been created for concept '" +prefLabel+ "'");
        }

    }

}
