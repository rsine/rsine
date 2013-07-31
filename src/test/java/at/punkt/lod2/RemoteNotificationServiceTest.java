package at.punkt.lod2;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.remotenotification.IRemoteServiceDetector;
import eu.lod2.rsine.remotenotification.RemoteNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.IOException;

public class RemoteNotificationServiceTest {

    private int remoteNotificationServiceListeningPort = TestUtils.getRandomPort();
    private RemoteNotificationService reegleRemoteNotificationService, otherRemoteNotificationService;
    private Model changeSet;

    @Before
    public void setUp() throws RDFParseException, IOException, RDFHandlerException {
        initServices();
        readChangeSet();
    }

    private void initServices() {
        reegleRemoteNotificationService = new RemoteNotificationService();
        otherRemoteNotificationService = new RemoteNotificationService();

        reegleRemoteNotificationService.setAuthoritativeUri("http://reegle.info/");
        reegleRemoteNotificationService.setRemoteServiceDetector(new TestRemoteServiceDetector());
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
        reegleRemoteNotificationService.announce(changeSet);
    }

    private class TestRemoteServiceDetector implements IRemoteServiceDetector {

        @Override
        public URI getRemoteService(Resource resource) {
            return new URIImpl("http://localhost:" +remoteNotificationServiceListeningPort);
        }

    }
}
