package at.punkt.lod2.local;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.util.Namespaces;
import org.apache.jena.fuseki.Fuseki;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class RealDataTest {

    private final Logger logger = LoggerFactory.getLogger(RealDataTest.class);

    @Autowired
    private Rsine rsine;

    @Autowired
    private Helper helper;

    private String[] predicates = {Namespaces.DCTERMS_NAMESPACE.getName() + "title",
            Namespaces.DCTERMS_NAMESPACE.getName() + "creator",
            Namespaces.SKOS_NAMESPACE.getName() + "prefLabel",
            Namespaces.SKOS_NAMESPACE.getName() + "altLabel",
            Namespaces.SKOS_NAMESPACE.getName() + "hiddenLabel",
            Namespaces.SKOS_NAMESPACE.getName() + "scopeNote",
            Namespaces.SKOS_NAMESPACE.getName() + "broader",
            Namespaces.SKOS_NAMESPACE.getName() + "narrower",
            Namespaces.SKOS_NAMESPACE.getName() + "related",
            Namespaces.SKOS_NAMESPACE.getName() + "notation",
    };

    private enum ChangeType {addition, removal}

    private final int SUBSCRIBER_COUNT = 20;
    private final int POST_COUNT = 50;
    private final String VOCAB_FILENAME = "/stw.rdf";

    private List<Statement> vocabStatements;
    private long accumulatedPostDurations = 0;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException, RDFHandlerException
    {
        helper.initFuseki(Rsine.class.getResource(VOCAB_FILENAME), "dataset");
        rsine.start();

        createVocabModel();
    }

    private void createVocabModel() throws RDFParseException, IOException, RDFHandlerException {
        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
        Set<Statement> vocabStatementsSet = new HashSet<Statement>();
        StatementCollector collector = new StatementCollector(vocabStatementsSet);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(Rsine.class.getResourceAsStream(VOCAB_FILENAME), "");

        vocabStatements = new ArrayList<Statement>(vocabStatementsSet.size());
        vocabStatements.addAll(vocabStatementsSet);
    }

    @After
    public void tearDown() throws IOException, InterruptedException, RepositoryException {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    @Ignore
    @Test
    public void randomAction() throws IOException, RDFHandlerException {
        registerUsers(SUBSCRIBER_COUNT);
        for (int i = 0; i < POST_COUNT; i++) {
            postChanges();
        }

        double avgInsertNotifyTimeMillis = accumulatedPostDurations / POST_COUNT;
        logger.info("Average insert/notification time: " +avgInsertNotifyTimeMillis+ "ms = " +
            Math.floor(1000 / avgInsertNotifyTimeMillis)+ " triple changes per second");
    }

    private void registerUsers(int subscriberCount) {
        for (int subscriber = 0; subscriber < subscriberCount; subscriber++) {
            Subscription subscription = new Subscription();
            addRandomQueries(subscription, ((int) (Math.random() * predicates.length)) + 1);
            subscription.addNotifier(new LoggingNotifier());
            rsine.registerSubscription(subscription);
        }
    }

    private void addRandomQueries(Subscription subscription, int maxQueryCount) {
        Set<String> usedPredicates = new HashSet<String>();
        for (int i = 0; i < maxQueryCount; i++) {
            String predicate = predicates[(int) (Math.random() * predicates.length)];
            if (!usedPredicates.contains(predicate)) {
                ChangeType changeType = getRandomChangeType();
                subscription.addQuery(changeSetPredicateQuery(predicate, changeType),
                                      new PredicateQueryFormatter((URI) subscription.getSubscriptionId(), predicate, changeType));
                usedPredicates.add(predicate);
            }
        }
    }

    private String changeSetPredicateQuery(String predicate, ChangeType changeType) {
        return Namespaces.CS_PREFIX+
               " SELECT ?cs ?subject ?object "
                + "WHERE { "
                + "?cs a cs:ChangeSet; "
                + "cs:createdDate ?csdate; "
                + "cs:" +changeType.toString()+ " ?x ."
                + "?x rdf:subject ?subject; "
                + "rdf:predicate <"+predicate+">; "
                + "rdf:object ?object . "
                + "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<"+XMLSchema.DATETIME+">)."
                + "}";
    }

    private void postChanges() throws IOException, RDFHandlerException {
        ChangeType changeType = getRandomChangeType();
        String changeCommand = "";
        switch (changeType) {
            case addition:
                changeCommand = ChangeTripleHandler.CHANGETYPE_ADD;
                break;

            case removal:
                changeCommand = ChangeTripleHandler.CHANGETYPE_REMOVE;
                break;
        }

        Statement randomStatement = getRandomStatement();
        post(changeCommand, randomStatement);
    }

    private ChangeType getRandomChangeType() {
        return ChangeType.values()[(int) Math.round(Math.random())];
    }

    private Statement getRandomStatement() {
        int statementIndex = (int) (Math.random() * vocabStatements.size());
        return vocabStatements.get(statementIndex);
    }

    private void post(String changeType, Statement randomStatement) throws IOException, RDFHandlerException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, changeType);
        props.setProperty(
            ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE,
            toNtriplesFormat(randomStatement)
            );

        long startTimeMillis = System.currentTimeMillis();
        helper.postChangeset(props);
        accumulatedPostDurations += System.currentTimeMillis() - startTimeMillis;
    }

    private String toNtriplesFormat(Statement statement) throws RDFHandlerException {
        StringWriter nTriplesStringWriter = new StringWriter();
        NTriplesWriter nTriplesWriter = new NTriplesWriter(nTriplesStringWriter);
        nTriplesWriter.startRDF();
        nTriplesWriter.handleStatement(statement);
        nTriplesWriter.endRDF();

        return nTriplesStringWriter.toString().trim();
    }

    private class PredicateQueryFormatter implements BindingSetFormatter {

        private String predicate;
        private URI subscriber;
        private ChangeType changeType;

        PredicateQueryFormatter(URI subscriber, String predicate, ChangeType changeType) {
            this.subscriber = subscriber;
            this.predicate = predicate;
            this.changeType = changeType;
        }

        @Override
        public String toMessage(BindingSet bindingSet) {
            String changeSet = bindingSet.getBinding("cs").getValue().stringValue();
            String subj = bindingSet.getBinding("subject").getValue().stringValue();
            String obj = bindingSet.getBinding("object").getValue().stringValue();


            return "subscriber: '" +subscriber.stringValue()+ "': " +changeType+ " of triple (" +
                    subj +" "+ predicate +" "+ obj +"), cs: " +changeSet;
        }

    }
}
