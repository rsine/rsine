package at.punkt.lod2.remote;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.ExpectedCountReached;
import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.RsineController;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import eu.lod2.util.Namespaces;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RemoteNotificationTest {

    private RsineController localRsine, remoteRsine;

    private Model changeSet;
    private CountingNotifier countingNotifier = new CountingNotifier();
    private AbstractApplicationContext localContext;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException {
        initServices();
        readChangeSet();
    }

    @After
    public void tearDown() throws IOException, InterruptedException, RepositoryException {
        localRsine.stop();
        remoteRsine.stop();
    }

    private void initServices() throws IOException {
        localContext = new ClassPathXmlApplicationContext("/at/punkt/lod2/remote/RemoteTest-localContext.xml");
        localRsine = localContext.getBean("changeSetService", RsineController.class);

        remoteRsine = new ClassPathXmlApplicationContext("/at/punkt/lod2/remote/RemoteTest-remoteContext.xml").
            getBean("changeSetService", RsineController.class);

        registerRemoteChangeSubscriber();
        localRsine.start();
        remoteRsine.start();
    }

    private void registerRemoteChangeSubscriber() {
        Subscription subscription = new Subscription();
        subscription.addQuery(createRemoteReferencesDetectionQuery(), new RemoteReferencesFormatter());
        subscription.addNotifier(countingNotifier);

        RegistrationService remoteRegistrationService = localContext.getBean(
            "remoteRegistrationService",
            RegistrationService.class);
        remoteRegistrationService.register(subscription, false);
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

    @Test(timeout = 5000)
    public void changeSetDissemination() throws RDFParseException, IOException, RDFHandlerException {
        RemoteNotificationServiceBase remoteNotificationServiceBase = localContext.getBean(
            "remoteNotificationServiceBase",
            RemoteNotificationServiceBase.class);
        remoteNotificationServiceBase.announce(changeSet);
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new ExpectedCountReached(countingNotifier, 1));
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

}
