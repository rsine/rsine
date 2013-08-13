package at.punkt.lod2;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.TestUtils;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.messageformatting.DummyBindingSetFormatter;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.Properties;

public class LocalNotificationTest {

    private final int managedStoreChangesListeningPort = new TestUtils().getRandomPort();
    private Rsine rsine;
    private CountingNotifier countingNotifier;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException {
        countingNotifier = new CountingNotifier();

        rsine = new Rsine(managedStoreChangesListeningPort, "");
        rsine.start();
    }

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        registerUser();
        postChanges();
        countingNotifier.waitForNotification();
    }

    private void registerUser() {
        Subscription subscription = new Subscription();
        subscription.addQuery(createQuery(), new DummyBindingSetFormatter());
        subscription.addNotifier(countingNotifier);
        rsine.registerSubscription(subscription);
    }

    private String createQuery() {
        //preflabel changes of concepts
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT * " +
                    "WHERE {" +
                        "?cs a cs:ChangeSet . " +
                        "?cs cs:createdDate ?csdate . " +
                        "?cs cs:removal ?removal . " +
                        "?cs cs:addition ?addition . " +
                        "?removal rdf:subject ?concept . " +
                        "?addition rdf:subject ?concept . " +
                        "?removal rdf:predicate skos:prefLabel . " +
                        "?removal rdf:object ?oldLabel . "+
                        "?addition rdf:predicate skos:prefLabel . " +
                        "?addition rdf:object ?newLabel . "+
                        "FILTER (?csdate > \"" + QueryDispatcher.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                    "}";
    }

    private void postChanges() throws IOException {
        addConcept();
        setPrefLabel();
        changePrefLabel();
        addOtherConcept();
        linkConcepts();
    }

    private void addConcept() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    private void setPrefLabel() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    private void changePrefLabel() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en .");
        props.setProperty(
            ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Schremser Edelmärzen\"@en .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    private void addOtherConcept() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1112> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

    private void linkConcepts() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#related> <http://reegle.info/glossary/1112> .");

        new TestUtils().doPost(managedStoreChangesListeningPort, props);
    }

}
