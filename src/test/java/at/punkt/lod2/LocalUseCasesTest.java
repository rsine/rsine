package at.punkt.lod2;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.Condition;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.apache.jena.fuseki.Fuseki;
import org.junit.*;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Properties;

public class LocalUseCasesTest {

    private Rsine rsine;
    private Helper helper;

    private CountingNotifier countingNotifier = new CountingNotifier();

    @Before
    public void setUp() throws IOException, RepositoryException {
        Helper.initFuseki(Rsine.class.getResource("/reegle.rdf"), "dataset");
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("/at/punkt/lod2/LocalTest-context.xml");
        helper = context.getBean(Helper.class);
        rsine = context.getBean(Rsine.class);

        rsine.start();
    }

    @After
    public void tearDown() throws Exception {
        Fuseki.getServer().stop();
        rsine.stop();
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
                    "SERVICE <" + QueryEvaluator.MANAGED_STORE_SPARQL_ENDPOINT+ "> {" +
                        "?concept skos:prefLabel ?prefLabel . " +
                        "?concept dcterms:creator \"" +contributor+ "\""+
                    "}" +
                    "FILTER(langMatches(lang(?prefLabel), \"en\"))" +
                    "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
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
                    "SERVICE <" +QueryEvaluator.MANAGED_STORE_SPARQL_ENDPOINT+"> {" +
                        "?concept skos:prefLabel ?conceptLabel . " +
                        "?otherConcept skos:prefLabel ?otherConceptLabel . " +
                        "?concept dcterms:creator \"" +contributor+ "\""+
                    "}" +
                    "FILTER(?hierarchicalRelation IN (skos:broader, skos:narrower))"+
                    "FILTER(langMatches(lang(?conceptLabel), \"en\"))" +
                    "FILTER(langMatches(lang(?otherConceptLabel), \"en\"))" +
                    "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";
    }

    @Test(timeout = 5000)
    public void propertyCreated() throws IOException {
        registerSubscription(
            createPropertyCreatedQuery(),
            new ToStringBindingSetFormatter(),
            new Condition(createPrefLabelCondition(), false));
        prefLabelAddition();

        Assert.assertEquals(1, countingNotifier.waitForNotification());
    }

    private void registerSubscription(String query,
                                      BindingSetFormatter formatter,
                                      Condition condition)
    {
        Subscription subscription = new Subscription();
        subscription.setQuery(query, formatter, condition);
        subscription.addNotifier(new LoggingNotifier());
        subscription.addNotifier(countingNotifier);
        rsine.registerSubscription(subscription);
    }

    private String createPropertyCreatedQuery() {
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
                "SELECT ?sub ?obj " +
                "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +

                    "?addition rdf:subject ?sub . " +
                    "?addition rdf:predicate ?pre . " +
                    "?addition rdf:object ?obj . "+

                    "FILTER ((?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) && " +
                            "(?pre IN (skos:prefLabel)))" +
                "}";
    }

    private String createPrefLabelCondition() {
        return Namespaces.SKOS_PREFIX + "ASK {?sub skos:prefLabel ?obj}";
    }

    private void prefLabelAddition() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/someConcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"some preflabel\"@en .");

        helper.doPost(props);
    }


    public void propertyChanged() {

    }

    @Ignore
    @Test
    public void scopeNoteChanges() throws IOException {
        registerSubscription(createScopeNoteChangesQuery(), new ScopeNoteChangeFormatter());

        scopeNoteDefinition();
        scopeNoteChange();
        scopeNoteChangeOfConceptCreatedByOtherUser();

        Assert.assertEquals(2, countingNotifier.waitForNotification());
    }

    private void scopeNoteDefinition() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/2547> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some scope note\"@en .");

        helper.doPost(props);
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

        helper.doPost(props);

    }

    private void scopeNoteChangeOfConceptCreatedByOtherUser() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
                ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
                "<http://reegle.info/glossary/1> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some scope note\"@en .");

        helper.doPost(props);
    }

    @Test
    public void conceptLinking() throws IOException {
        registerSubscription(createConceptLinkingQuery(), new ConceptLinkingFormatter());
        addLink();

        Assert.assertEquals(1, countingNotifier.waitForNotification());
    }

    private void addLink() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/443> <http://www.w3.org/2004/02/skos/core#narrower> <http://reegle.info/glossary/442> .");
        helper.doPost(props);
    }

    private class ScopeNoteChangeFormatter implements BindingSetFormatter {

        @Override
        public String toMessage(BindingSet bindingSet) {
            String conceptUri = bindingSet.getValue("concept").stringValue();
            String conceptLabel = ((Literal) bindingSet.getValue("prefLabel")).getLabel();
            String newScopeNote = ((Literal) bindingSet.getValue("scopeNote")).getLabel();

            return "The scope note of concept '" + conceptLabel + "'(" + conceptUri +") has been defined as/changed to '" + newScopeNote + "'";
        }

    }

    private class ConceptLinkingFormatter implements BindingSetFormatter {

        @Override
        public String toMessage(BindingSet bindingSet) {
            String conceptUri = bindingSet.getValue("concept").stringValue();
            String otherConceptUri = bindingSet.getValue("otherConcept").stringValue();
            String conceptLabel = ((Literal) bindingSet.getValue("conceptLabel")).getLabel();
            String otherConceptLabel = ((Literal) bindingSet.getValue("otherConceptLabel")).getLabel();

            return "A new hierarchical link for concept '" +conceptLabel+ "'(" +conceptUri+ ") to concept '"
                +otherConceptLabel+ "'(" +otherConceptUri+ ") has been established";
        }
    }

}
