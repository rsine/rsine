package at.punkt.lod2.local;

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
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ConceptMergeTest {

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
        datasetGraph = helper.initFuseki(Rsine.class.getResource("/reegle.rdf"), "dataset");
        subscribe();
        rsine.start();
    }

    private void subscribe() throws RDFParseException, IOException, RDFHandlerException {
        countingNotifier = new CountingNotifier();

        Model subscriptionModel = helper.createModelFromResourceFile("/wk/subscription_pp_merge.ttl", RDFFormat.TURTLE);
        Resource subscriptionId = registrationService.register(subscriptionModel);
        Subscription subscription = registrationService.getSubscription(subscriptionId);
        subscription.addNotifier(countingNotifier);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    @Test
    public void mergeDetection() throws IOException, RDFHandlerException, InterruptedException {
        String mainConcept = "http://reegle.info/glossary/440";
        String abandonedConcept = "http://reegle.info/glossary/422";
        Literal abandonedConceptPrefLabel = new LiteralImpl("combi storage tanks", "en");

        changeAltLabel(new URIImpl(mainConcept), abandonedConceptPrefLabel);
        Thread.sleep(1000);
        removeConcept(new URIImpl(abandonedConcept));

        Assert.assertEquals(1, countingNotifier.waitForNotification());
    }

    private void changeAltLabel(URI concept, Literal newAltLabel) throws IOException, RDFHandlerException {
        datasetGraph.getDefaultGraph().add(new Triple(
            NodeFactory.createURI(concept.stringValue()),
            NodeFactory.createURI(SKOS.ALT_LABEL.stringValue()),
            NodeFactory.createLiteral(newAltLabel.getLabel(), newAltLabel.getLanguage(), false)));
        helper.postStatementAdded(new StatementImpl(concept, SKOS.ALT_LABEL, newAltLabel));
    }

    private void removeConcept(URI concept) throws IOException, RDFHandlerException {
        datasetGraph.getDefaultGraph().add(new Triple(
            NodeFactory.createURI(concept.stringValue()),
            NodeFactory.createURI(OWL.NAMESPACE + "deprecated"),
            NodeFactory.createLiteral(Boolean.TRUE.toString())));

        helper.postStatementAdded(new StatementImpl(
            concept,
            new URIImpl(OWL.NAMESPACE + "deprecated"),
            new BooleanLiteralImpl(true)));
    }

    @Test
    public void noMerge() throws IOException, RDFHandlerException {
        changeAltLabel(new URIImpl("http://reegle.info/glossary/1059"), new LiteralImpl("test"));
        removeConcept(new URIImpl("http://reegle.info/glossary/355"));

        Assert.assertEquals(0, countingNotifier.waitForNotification(2000));
    }

}
