package at.punkt.lod2;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.messageformatting.DummyBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.junit.After;
import org.junit.Before;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.util.Properties;

public abstract class LocalNotificationTest implements ApplicationContextAware  {

    @Autowired
    private Rsine rsine;

    @Autowired
    private Helper helper;

    protected ApplicationContext applicationContext;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException {
        registerUser();
        rsine.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        rsine.stop();
    }

    private void registerUser() {
        Subscription subscription = new Subscription();
        subscription.addQuery(createQuery(), new DummyBindingSetFormatter());
        subscription.addNotifier(getNotifier());
        rsine.registerSubscription(subscription);
    }

    protected INotifier getNotifier() {
        return new LoggingNotifier();
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
                        "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                    "}";
    }

    protected void postEditChanges() throws IOException {
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

        helper.doPost(props);
    }

    private void setPrefLabel() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en .");

        helper.doPost(props);
    }

    protected void changePrefLabel() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Ottakringer Helles\"@en .");
        props.setProperty(
            ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Schremser Edelmärzen\"@en .");

        helper.doPost(props);
    }

    private void addOtherConcept() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1112> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .");

        helper.doPost(props);
    }

    private void linkConcepts() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            "<http://reegle.info/glossary/1111> <http://www.w3.org/2004/02/skos/core#related> <http://reegle.info/glossary/1112> .");

        helper.doPost(props);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
