package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.dissemination.Notifier;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.IRemoteServiceDetector;
import eu.lod2.util.Namespaces;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RemoteNotificationTest {

    private final Logger logger = LoggerFactory.getLogger(RemoteNotificationTest.class);

    private final int localInstanceListeningPort = new TestUtils().getRandomPort(),
                      remoteInstanceListeningPort = new TestUtils().getRandomPort();
    private Model changeSet;
    private Rsine localRsineInstance;
    private ResultDetectionNotifier resultDetectionNotifier;

    @Before
    public void setUp() throws RDFParseException, IOException, RDFHandlerException, RepositoryException {
        initServices();
        readChangeSet();
    }

    private void initServices() throws IOException, RepositoryException {
        localRsineInstance = new Rsine(localInstanceListeningPort, "", "http://reegle.info/");
        localRsineInstance.getRemoteNotificationService().setRemoteServiceDetector(new TestRemoteServiceDetector());
        localRsineInstance.start();

        Rsine remoteRsineInstance = new Rsine(remoteInstanceListeningPort, "", "http://zbw.eu");
        remoteRsineInstance.setNotifier(resultDetectionNotifier = new ResultDetectionNotifier());
        registerRemoteChangeSubscriber(remoteRsineInstance);
        remoteRsineInstance.start();
    }

    private void registerRemoteChangeSubscriber(Rsine rsine) {
        Subscription subscription = rsine.requestSubscription();
        subscription.addQuery(createRemoteReferencesDetectionQuery());
        rsine.registerSubscription(subscription);
    }

    private String createRemoteReferencesDetectionQuery() {
        return Namespaces.SKOS_PREFIX+
                Namespaces.CS_PREFIX+
                Namespaces.DCTERMS_PREFIX+
                "SELECT * " +
                "WHERE {" +
                    "?cs a cs:ChangeSet . " +

                    "?cs dcterms:source ?source . "+

                    "?cs cs:addition ?addition . " +
                    "?addition rdf:subject ?subject . " +
                    "?addition rdf:predicate ?predicate . " +
                    "?addition rdf:object ?object . "+
                "}";
    }

    private void readChangeSet() throws RDFParseException, IOException, RDFHandlerException {
        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
        changeSet = new TreeModel();
        StatementCollector collector = new StatementCollector(changeSet);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(Rsine.class.getResourceAsStream("/changeset.rdf"), "");
    }

    @Test
    public void changeSetDissemination() throws RDFParseException, IOException, RDFHandlerException {
        localRsineInstance.getRemoteNotificationService().announce(changeSet);
        Assert.assertTrue(resultDetectionNotifier.resultDetected);
    }

    private class TestRemoteServiceDetector implements IRemoteServiceDetector {

        @Override
        public URI getRemoteService(Resource resource) {
            return new URIImpl("http://localhost:" +remoteInstanceListeningPort+ "/remote");
        }

    }

    private class ResultDetectionNotifier extends Notifier {

        private boolean resultDetected;

        @Override
        public void queryResultsAvailable(BindingSet bs, Subscription subscription) {
            String source = bs.getValue("source").stringValue();
            String subj = bs.getValue("subject").stringValue();
            String pred = bs.getValue("predicate").stringValue();
            String obj = bs.getValue("object").stringValue();

            logger.info("The remote entity '" +source+ "' has stated the following information about a local concept: " +
                        "'" +subj +" "+ pred +" "+ obj +"'");

            resultDetected = true;
        }

    }
}
