package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.Notifier;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class LocalUseCasesTest {

    private final Logger logger = LoggerFactory.getLogger(LocalUseCasesTest.class);

    private SPARQLServer fusekiServer;
    private Rsine rsine;
    private int managedStoreChangesListeningPort = TestUtils.getRandomPort();

    @Before
    public void setUp() throws IOException, RepositoryException {
        fusekiServer = new TestUtils().initFuseki(Rsine.class.getResource("/reegle.rdf"), "dataset");

        rsine = new Rsine(managedStoreChangesListeningPort, "http://localhost:3030/dataset/query");
        registerUser();

        rsine.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        fusekiServer.stop();
        rsine.stop();
    }

    private void registerUser() {
        Subscription subscription = rsine.requestSubscription();
        subscription.addQuery(createScopeNoteChangesQuery());
        subscription.addQuery(createConceptLinkingQuery());
        rsine.registerSubscription(subscription);
    }

    private String createScopeNoteChangesQuery() {
        String contributor = "reegle";
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT ?concept ?prefLabel ?scopeNote " +
               "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +
                    "?addition rdf:subject ?concept . " +
                    "?addition rdf:predicate skos:scopeNote . " +
                    "?addition rdf:object ?scopeNote . "+
                    "SERVICE <" +QueryDispatcher.MANAGED_STORE_SPARQL_ENDPONT+ "> {" +
                        "?concept skos:prefLabel ?prefLabel . " +
                        "?concept dcterms:creator \"" +contributor+ "\""+
                    "}" +
                    "FILTER(langMatches(lang(?prefLabel), \"en\"))" +
                    "FILTER (?csdate > \"" + QueryDispatcher.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
               "}";
    }

    private String createConceptLinkingQuery() {
        String contributor = "reegle";
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT * " +
               "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +
                    "?addition rdf:subject ?concept . " +
                    "?addition rdf:predicate ?hierarchicalRelation . " +
                    "?addition rdf:object ?otherConcept . "+
                    "SERVICE <http://localhost:3030/dataset/query> {" +
                        "?concept skos:prefLabel ?conceptLabel . " +
                        "?otherConcept skos:prefLabel ?otherConceptLabel . " +
                        "?concept dcterms:creator \"" +contributor+ "\""+
                    "}" +
                    "FILTER(?hierarchicalRelation IN (skos:broader, skos:narrower))"+
                    "FILTER(langMatches(lang(?conceptLabel), \"en\"))" +
                    "FILTER(langMatches(lang(?otherConceptLabel), \"en\"))" +
                    "FILTER (?csdate > \"" + QueryDispatcher.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";
    }

    @Test
    public void scopeNoteChanges() throws IOException {
        ScopeNoteChangeNotifier scopeNoteChangeNotifier = new ScopeNoteChangeNotifier();
        rsine.setNotifier(scopeNoteChangeNotifier);

        scopeNoteDefinition();
        scopeNoteChange();
        scopeNoteChangeOfConceptCreatedByOtherUser();

        Assert.assertEquals(2, scopeNoteChangeNotifier.notificationsCount);
    }

    private void scopeNoteDefinition() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/2547> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some scope note\"@en .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    private void scopeNoteChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/2547> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some scope note\"@en .");
        props.setProperty(
            ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE,
            "<http://reegle.info/glossary/2547> <http://www.w3.org/2004/02/skos/core#scopeNote> \"updated scope note\"@en .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);

    }

    private void scopeNoteChangeOfConceptCreatedByOtherUser() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some scope note\"@en .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    @Test
    public void conceptLinking() throws IOException {
        ConceptLinkingNotifier conceptLinkingNotifier = new ConceptLinkingNotifier();
        rsine.setNotifier(conceptLinkingNotifier);
        addLink();

        Assert.assertEquals(1, conceptLinkingNotifier.notificationsCount);
    }

    private void addLink() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/443> <http://www.w3.org/2004/02/skos/core#narrower> <http://reegle.info/glossary/442> .");
        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    private class ScopeNoteChangeNotifier extends Notifier {

        private String conceptLabel, conceptUri, newScopeNote;
        private int notificationsCount = 0;

        @Override
        public void queryResultsAvailable(BindingSet bs, Subscription subscription) {
            conceptUri = bs.getValue("concept").stringValue();
            conceptLabel = ((Literal) bs.getValue("prefLabel")).getLabel();
            newScopeNote = ((Literal) bs.getValue("scopeNote")).getLabel();

            logger.info("The scope note of concept '" + conceptLabel + "'(" + conceptUri +
                    ") has been defined as/changed to '" + newScopeNote + "'");
            notificationsCount++;
        }

    }

    private class ConceptLinkingNotifier extends Notifier {

        private String conceptLabel, conceptUri, otherConceptLabel, otherConceptUri;
        private int notificationsCount = 0;

        @Override
        public void queryResultsAvailable(BindingSet bs, Subscription subscription) {
            conceptUri = bs.getValue("concept").stringValue();
            otherConceptUri = bs.getValue("otherConcept").stringValue();
            conceptLabel = ((Literal) bs.getValue("conceptLabel")).getLabel();
            otherConceptLabel = ((Literal) bs.getValue("otherConceptLabel")).getLabel();

            logger.info("A new hierarchical link for concept '" +conceptLabel+ "'(" +conceptUri+ ") to concept '"
                +otherConceptLabel+ "'(" +otherConceptUri+ ") has been established");
            notificationsCount++;
        }

    }

}
