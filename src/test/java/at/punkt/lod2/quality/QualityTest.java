package at.punkt.lod2.quality;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.apache.jena.fuseki.Fuseki;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"QualityTest-context.xml"})
public class QualityTest {

    @Autowired
    private Rsine rsine;

    @Autowired
    private Helper helper;

    @Autowired
    private RegistrationService registrationService;

    private CountingNotifier countingNotifier;
    private DatasetGraph datasetGraph;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException {
        countingNotifier = new CountingNotifier();
        datasetGraph = helper.initFuseki(Rsine.class.getResource("/reegle.rdf"), "dataset");
        rsine.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    @Test
    public void hierarchicalCycles() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/cyclic_hierarchical_relations.ttl");
        addTriple(new URIImpl("http://reegle.info/glossary/1124"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/glossary/676"));

        Assert.assertEquals(1, countingNotifier.waitForNotification());
    }

    private void subscribe(String subscriptionFileLocation) throws RDFParseException, IOException, RDFHandlerException {
        Model subscriptionModel = helper.createModelFromResourceFile(subscriptionFileLocation, RDFFormat.TURTLE);
        Resource subscriptionId = registrationService.register(subscriptionModel);
        Subscription subscription = registrationService.getSubscription(subscriptionId);
        subscription.addNotifier(countingNotifier);
    }

    public void addTriple(URI subject, URI predicate, URI object)
            throws IOException, RDFHandlerException
    {
        datasetGraph.getDefaultGraph().add(new Triple(
                NodeFactory.createURI(subject.toString()),
                NodeFactory.createURI(predicate.toString()),
                NodeFactory.createURI(object.toString())));
        helper.postStatementAdded(new StatementImpl(subject, predicate, object));
    }

    @Test
    public void multiHierarchicalCycles() throws IOException, RDFHandlerException, RDFParseException {
        subscribe("/quality/cyclic_hierarchical_relations.ttl");
        addTriple(new URIImpl("http://reegle.info/glossary/1124"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/newConcept"));
        addTriple(new URIImpl("http://reegle.info/newConcept"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/glossary/676"));

        Assert.assertEquals(1, countingNotifier.waitForNotification());
    }

    @Test
    public void noCycle() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/cyclic_hierarchical_relations.ttl");
        addTriple(new URIImpl("http://reegle.info/glossary/1124"),
            SKOS.NARROWER,
            new URIImpl("http://reegle.info/glossary/676"));

        Assert.assertEquals(0, countingNotifier.waitForNotification(2000));
    }

    @Test
    public void disjointLabelViolations() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/disjoint_labels_violation.ttl");

        // clash with preflabel
        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/682"), new LiteralImpl("energy efficiency", "en"));

        // clash with altlabel
        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/1063"), new LiteralImpl("emission", "en"));

        Assert.assertEquals(2, countingNotifier.waitForNotification());
    }

    @Test
    public void noDisjointLabelViolations() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/disjoint_labels_violation.ttl");
        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/195"), new LiteralImpl("some other label", "en"));
        Assert.assertEquals(0, countingNotifier.waitForNotification(2000));
    }

}