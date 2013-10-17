package at.punkt.lod2.remote;

import at.punkt.lod2.util.CountingNotifier;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.remotenotification.RemoteNotificationServiceBase;
import eu.lod2.util.Namespaces;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.query.BindingSet;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"RemoteTest-localContext.xml", "RemoteTest-remoteContext.xml"})
public class RemoteNotificationTest implements ApplicationContextAware {

    @Autowired
    private Rsine localRsine, remoteRsine;

    private Model changeSet;
    private CountingNotifier countingNotifier = new CountingNotifier();
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException {
        initServices();
        readChangeSet();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        localRsine.stop();
        remoteRsine.stop();
    }

    private void initServices() throws IOException {
        registerRemoteChangeSubscriber(remoteRsine);
        localRsine.start();
        remoteRsine.start();
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
        RemoteNotificationServiceBase remoteNotificationServiceBase = (RemoteNotificationServiceBase) applicationContext.
            getBean("remoteNotificationServiceBase");
        remoteNotificationServiceBase.announce(changeSet);
        Assert.assertTrue(countingNotifier.waitForNotification() >= 1);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
