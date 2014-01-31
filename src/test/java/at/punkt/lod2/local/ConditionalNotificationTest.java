package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.ExpectedCountReached;
import at.punkt.lod2.util.Helper;
import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.Condition;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.apache.jena.fuseki.Fuseki;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ConditionalNotificationTest {

    private Helper helper;
    private Rsine rsine;
    private CountingNotifier countingNotifier;
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws IOException, RepositoryException {
        Helper.initFuseki("dataset");

        countingNotifier = new CountingNotifier();
        applicationContext = new ClassPathXmlApplicationContext("/at/punkt/lod2/local/LocalTest-context.xml");
        helper = applicationContext.getBean(Helper.class);
        rsine = applicationContext.getBean(Rsine.class);
        rsine.start();
    }

    @After
    public void tearDown() throws Exception {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    @Test
    public void propertyCreated()
        throws IOException, RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        registerSubscription(
                createPropertyCreatedQuery(),
                new ToStringBindingSetFormatter(),
                new Condition(createPrefLabelCondition(), false)); // triple did not exist before

        postTripleChange();
        insertIntoManagedStore();

        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new ExpectedCountReached(countingNotifier, 1));
    }

    private void registerSubscription(String query, BindingSetFormatter formatter, Condition condition) {
        Subscription subscription = new Subscription();
        subscription.addQuery(query, formatter, condition);
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

    private void insertIntoManagedStore()
        throws MalformedQueryException, RepositoryException, UpdateExecutionException
    {
        String managedServerSparqlEndpoint = applicationContext.getBean("managedServerSparqlUpdate", String.class);
        RepositoryConnection repCon = new SPARQLConnection(new SPARQLRepository(managedServerSparqlEndpoint));
        repCon.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA {" +getPrefLabelTriple()+ "}").execute();
        repCon.close();
    }

    private String getPrefLabelTriple() {
        return "<http://reegle.info/glossary/someConcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"some preflabel\"@en .";
    }

    private void postTripleChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            getPrefLabelTriple());

        helper.postChangeset(props);
    }

    @Test
    public void propertyChanged()
        throws IOException, MalformedQueryException, RepositoryException, UpdateExecutionException
    {
        registerSubscription(
                createPropertyCreatedQuery(),
                new ToStringBindingSetFormatter(),
                new Condition(createPrefLabelCondition(), true)); // triple did exist before

        postTripleChange(); // no notification should occur here because condition is not fulfilled

        insertIntoManagedStore();
        postTripleChange(); // here we get the one and only notification

        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new ExpectedCountReached(countingNotifier, 1));
    }

}
