package at.punkt.lod2;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.apache.jena.fuseki.Fuseki;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class ManagedStoreNotificationTest {

    @Autowired
    private Rsine rsine;

    @Autowired
    private Helper helper;

    private ScopeNoteCreationFormatter scopeNoteCreationFormatter;
    private CountingNotifier countingNotifier;

    @Before
    public void setUp() throws IOException, RepositoryException {
        helper.initFuseki(Rsine.class.getResource("/reegle.rdf"), "dataset");
        scopeNoteCreationFormatter = new ScopeNoteCreationFormatter();
        countingNotifier = new CountingNotifier();

        registerUser();
        rsine.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    private void registerUser() {
        Subscription subscription = new Subscription();
        subscription.addQuery(createScopeNoteCreatedQuery(), scopeNoteCreationFormatter);
        subscription.addNotifier(new LoggingNotifier());
        subscription.addNotifier(countingNotifier);
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
                    "SERVICE <" + QueryEvaluator.MANAGED_STORE_SPARQL_ENDPOINT+ "> {" +
                        "?concept skos:prefLabel ?prefLabel . " +
                    "}" +
                    "FILTER(langMatches(lang(?prefLabel), \"en\"))" +
               "}";
    }

    @Test
    public void notificationDissemination() throws IOException {
        triggerQueryExecution();

        Assert.assertEquals(1, countingNotifier.waitForNotification());
        Assert.assertEquals("blanching", scopeNoteCreationFormatter.prefLabel);
    }

    private void triggerQueryExecution() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/2547> <http://www.w3.org/2004/02/skos/core#scopeNote> \"some additional info\"@en .");

        helper.doPost(props);

    }

    private class ScopeNoteCreationFormatter implements BindingSetFormatter {

        private String prefLabel;

        @Override
        public String toMessage(BindingSet bindingSet) {
            String newScopeNote = ((Literal) bindingSet.getValue("newScopeNote")).getLabel();
            prefLabel = ((Literal) bindingSet.getValue("prefLabel")).getLabel();

            return "Scope note '" +newScopeNote+ "' has been created for concept '" +prefLabel+ "'";
        }

    }
}
