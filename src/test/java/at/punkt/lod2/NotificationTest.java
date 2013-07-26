package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.querydispatcher.QueryDispatcher;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class NotificationTest {

    private final int port = 8081;
    private Rsine rsine;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException {
        rsine = new Rsine(port);
        addVocabData();
    }

    private void addVocabData() throws RepositoryException, RDFParseException, IOException {
        URL vocabUrl = Rsine.class.getResource("/reegle.rdf");
        rsine.setManagedTripleStoreContent(new File(vocabUrl.getFile()));
    }

    @Test
    public void notificationDissemination() throws RDFParseException, IOException, RDFHandlerException {
        registerUser();
        postChanges();
    }

    private void registerUser() {
        Subscription subscription = rsine.requestSubscription();
        subscription.addQuery(createQuery());
        rsine.registerSubscription(subscription);
    }

    private String createQuery() {
        //preflabel changes of concepts
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT * " +
                    "FROM NAMED <" +Namespaces.CHANGESET_CONTEXT+ "> " +
                    "FROM NAMED <" +Namespaces.VOCAB_CONTEXT+ "> " +
                    "WHERE {" +
                        "GRAPH ?g {" +
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
                        "}" +
                        "FILTER (?csdate > \"" + QueryDispatcher.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                    "}";
    }

    private void postChanges() throws IOException {
        addConcept();
        setPrefLabel();
        changePrefLabel();
        addOtherConcept();
        //linkConcepts();
    }

    private void addConcept() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .");

        TestUtils.doPost(port, props);
    }

    private void setPrefLabel() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en .");

        TestUtils.doPost(port, props);
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

        TestUtils.doPost(port, props);
    }

    private void addOtherConcept() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1112> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .");

        TestUtils.doPost(port, props);
    }

    private void linkConcepts() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#related> <http://reegle.info/glossary/1112> .");

        TestUtils.doPost(port, props);
    }

}
