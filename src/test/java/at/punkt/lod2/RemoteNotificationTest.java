package at.punkt.lod2;

import at.punkt.lod2.util.CountingNotifier;
import eu.lod2.rsine.Rsine;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml", "RemoteTest-context.xml"})
public class RemoteNotificationTest {

    @Autowired
    private Rsine localRsineInstance, remoteRsineInstance;

    private int localInstanceListeningPort, remoteInstanceListeningPort;
    private Model changeSet;

    private CountingNotifier countingNotifier;

    /*
    @Before
    public void setUp() throws RDFParseException, IOException, RDFHandlerException, RepositoryException {
        localInstanceListeningPort = Helper.MANAGED_STORE_LISTENING_PORT;
        remoteInstanceListeningPort = Helper.MANAGED_STORE_LISTENING_PORT + 1;
        countingNotifier = new CountingNotifier();

        initServices();
        readChangeSet();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        localRsineInstance.stop();
        remoteRsineInstance.stop();
    }

    private void initServices() throws IOException, RepositoryException {
        localRsineInstance = new Rsine(localInstanceListeningPort, "", "http://reegle.info/");
        localRsineInstance.getRemoteNotificationService().setRemoteServiceDetector(new TestRemoteServiceDetector());
        localRsineInstance.start();

        remoteRsineInstance = new Rsine(remoteInstanceListeningPort, "", "http://zbw.eu");
        registerRemoteChangeSubscriber(remoteRsineInstance);
        remoteRsineInstance.start();
    }

    private void registerRemoteChangeSubscriber(Rsine rsine) {
        Subscription subscription = new Subscription();
        subscription.addQuery(createRemoteReferencesDetectionQuery(), new RemoteReferencesFormatter());
        subscription.addNotifier(countingNotifier);
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
        Assert.assertTrue(countingNotifier.waitForNotification() >= 1);
    }

    private class TestRemoteServiceDetector implements IRemoteServiceDetector {

        @Override
        public URI getRemoteService(Resource resource) {
            return new URIImpl("http://localhost:" +remoteInstanceListeningPort+ "/remote");
        }

    }

    private class RemoteReferencesFormatter implements BindingSetFormatter {

        @Override
        public String toMessage(BindingSet bindingSet) {
            String source = bindingSet.getValue("source").stringValue();
            String subj = bindingSet.getValue("subject").stringValue();
            String pred = bindingSet.getValue("predicate").stringValue();
            String obj = bindingSet.getValue("object").stringValue();

            return "The remote entity '" +source+ "' has stated the following information about a local concept: " +
                    "'" +subj +" "+ pred +" "+ obj +"'";
        }

    }
    */
}
