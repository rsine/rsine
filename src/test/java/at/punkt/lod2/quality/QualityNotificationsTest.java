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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"QualityNotificationsTest-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class QualityNotificationsTest {

    @Autowired
    private Rsine rsine;

    @Autowired
    private Helper helper;

    @Autowired
    private RegistrationService registrationService;

    private DatasetGraph datasetGraph;
    private CountingNotifier countingNotifier;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException {
        datasetGraph = Helper.initFuseki(Rsine.class.getResource("/reegle.rdf"), "dataset");
        countingNotifier = new CountingNotifier();
        rsine.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    @Test(timeout = 10000)
    public void hierarchicalCycles() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/cyclic_hierarchical_relations.ttl");
        addTriple(new URIImpl("http://reegle.info/glossary/1124"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/glossary/676"));

        countingNotifier.waitForNotification();
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

    @Test(timeout = 10000)
    public void multiHierarchicalCycles() throws IOException, RDFHandlerException, RDFParseException {
        subscribe("/quality/cyclic_hierarchical_relations.ttl");
        addTriple(new URIImpl("http://reegle.info/glossary/1124"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/newConcept"));
        addTriple(new URIImpl("http://reegle.info/newConcept"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/glossary/676"));
        addTriple(new URIImpl("http://reegle.info/glossary/676"),
            SKOS.BROADER,
            new URIImpl("http://reegle.info/glossary/1124"));

        countingNotifier.waitForNotification();
    }

    @Test
    public void noCycle() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/cyclic_hierarchical_relations.ttl");
        addTriple(new URIImpl("http://reegle.info/glossary/1510"),
            SKOS.NARROWER,
            new URIImpl("http://reegle.info/glossary/229"));

        Assert.assertEquals(0, countingNotifier.waitForNotificationMaxTime(2000));
    }

    @Test(timeout = 10000)
    public void disjointLabelViolations_withPrefLabel() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/disjoint_labels_violation.ttl");
        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/682"), new LiteralImpl("energy efficiency", "en"));
        countingNotifier.waitForNotification();
    }

    @Test(timeout = 10000)
    public void disjointLabelViolations_withAltLabel() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/disjoint_labels_violation.ttl");
        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/1063"), new LiteralImpl("emission", "en"));
        countingNotifier.waitForNotification();
    }

    @Test
    public void noDisjointLabelViolations() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/disjoint_labels_violation.ttl");
        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/195"), new LiteralImpl("some other label", "en"));
        Assert.assertEquals(0, countingNotifier.waitForNotificationMaxTime(2000));
    }

    @Test(timeout = 10000)
    public void valuelessAssociativeRelations() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/valueless_associative_relations.ttl");
        String sibling1 = "http://reegle.info/glossary/1676";
        String sibling2 = "http://reegle.info/glossary/1252";
        addTriple(new URIImpl(sibling1), SKOS.RELATED, new URIImpl(sibling2));

        countingNotifier.waitForNotification();
    }

    @Test
    public void noValuelessAssociativeRelations() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/valueless_associative_relations.ttl");
        String nonSibling1 = "http://reegle.info/glossary/1510";
        String nonSibling2 = "http://reegle.info/glossary/229";

        addTriple(new URIImpl(nonSibling1), SKOS.RELATED, new URIImpl(nonSibling2));
        Assert.assertEquals(0, countingNotifier.waitForNotificationMaxTime(2000));
    }

    @Test
    public void hierarchicalRedundancies() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/hierarchical_redundancy.ttl");

        String level1Concept = "http://reegle.info/glossary/1056";
        String level3Concept = "http://reegle.info/glossary/196";
        addTriple(new URIImpl(level3Concept), SKOS.BROADER, new URIImpl(level1Concept));
        addTriple(new URIImpl(level3Concept), SKOS.NARROWER, new URIImpl(level1Concept));

        Assert.assertEquals(1, countingNotifier.waitForNotificationMaxTime(5000));
    }

    @Test
    public void noHierarchicalRedundancies() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/hierarchical_redundancy.ttl");

        String level1Concept = "http://reegle.info/glossary/196";
        String cousinConcept = "http://reegle.info/glossary/783";
        addTriple(new URIImpl(cousinConcept), SKOS.BROADER, new URIImpl(level1Concept));

        Assert.assertEquals(0, countingNotifier.waitForNotificationMaxTime(2000));
    }

    @Test(timeout = 10000)
    public void overlappingLabels() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/overlapping_labels.ttl");

        helper.setAltLabel(datasetGraph, new URIImpl("http://reegle.info/glossary/357"), new LiteralImpl("Biogas", "en"));
        countingNotifier.waitForNotification();
    }

    @Test(timeout = 10000)
    public void relationClashes() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/relation_clashes.ttl");

        String level1Concept = "http://reegle.info/glossary/1056";
        String level3Concept = "http://reegle.info/glossary/196";
        addTriple(new URIImpl(level3Concept), SKOS.RELATED, new URIImpl(level1Concept));

        countingNotifier.waitForNotification();
    }

    @Test
    public void mappingClashes() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/mapping_clashes.ttl");

        String concept = "http://reegle.info/glossary/1912";
        String relatedMappedConcept = "http://dbpedia.org/resource/Vulnerability";

        // error
        addTriple(new URIImpl(concept), SKOS.BROAD_MATCH, new URIImpl(relatedMappedConcept));

        // error
        addTriple(new URIImpl(relatedMappedConcept), SKOS.EXACT_MATCH, new URIImpl(concept));

        // ok
        addTriple(new URIImpl(concept), SKOS.BROAD_MATCH, new URIImpl("http://reegle.info/glossary/1674"));

        Assert.assertEquals(2, countingNotifier.waitForNotificationMaxTime(5000));
    }

    @Test(timeout = 10000)
    public void mappingMisues_sameScheme() throws IOException, RDFHandlerException, RDFParseException {
        subscribe("/quality/mapping_relations_misuse.ttl");
        String[] conceptsInSameScheme = {"http://reegle.info/glossary/676", "http://reegle.info/glossary/1620"};
        addTriple(new URIImpl(conceptsInSameScheme[0]), SKOS.BROAD_MATCH, new URIImpl(conceptsInSameScheme[1]));
        countingNotifier.waitForNotification();
    }

    @Test
    public void mappingMisues_differentScheme() throws IOException, RDFHandlerException, RDFParseException {
        subscribe("/quality/mapping_relations_misuse.ttl");

        String[] conceptsInDifferentSchemes = {"http://reegle.info/glossary/676", "http://reegle.info/glossary/698"};
        addTriple(new URIImpl(conceptsInDifferentSchemes[0]), SKOS.BROAD_MATCH, new URIImpl(conceptsInDifferentSchemes[1]));

        Assert.assertEquals(0, countingNotifier.waitForNotificationMaxTime(5000));
    }

    @Test
    public void mappingMisuse_nomisuse() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/mapping_relations_misuse.ttl");

        // ok: concepts in different concept schemes
        addTriple(new URIImpl("http://reegle.info/glossary/1124"), SKOS.BROAD_MATCH, new URIImpl("http://reegle.info/glossary/1714"));

        Assert.assertEquals(0, countingNotifier.waitForNotificationMaxTime(5000));
    }

    @Test
    public void topConceptsHavingBroaderConcepts() throws RDFParseException, IOException, RDFHandlerException {
        subscribe("/quality/top_concepts_having_broader_concepts.ttl");

        String topConcept = "http://reegle.info/glossary/1127";

        // error (should count as one because of inverse relation)
        addTriple(new URIImpl(topConcept), SKOS.BROADER, new URIImpl("http://some.concept"));
        addTriple(new URIImpl("http://some.other.concept"), SKOS.NARROWER, new URIImpl(topConcept));

        // no error
        addTriple(new URIImpl(topConcept), SKOS.NARROWER, new URIImpl("http://some.completely.other.concept"));

        addTriple(new URIImpl("http://some.completely.other.concept"), SKOS.BROADER, new URIImpl("http://some.completely.other.concept2"));

        Assert.assertEquals(1, countingNotifier.waitForNotificationMaxTime(5000));
    }

}