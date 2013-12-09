package at.punkt.lod2.wk;

import at.punkt.lod2.util.Helper;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.queryhandling.QueryProfiler;
import eu.lod2.rsine.registrationservice.Subscription;
import org.apache.jena.fuseki.Fuseki;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"ScenarioTests-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ScenarioTests {

    private final int MAX_FILE_CNT = 10000;

    private final static String DATASET_DIR = "/home/christian/Downloads/dumps";
    private final static String DATASET_SPARQL_ENDPOINT = "http://localhost:3030/dataset/query";
    private final Logger logger = LoggerFactory.getLogger(ScenarioTests.class);

    private long loadedTriples;
    private StatisticsNotifier statisticsNotifier = new StatisticsNotifier();

    @Autowired
    private Rsine rsine;

    @Autowired
    private Helper helper;

    @Autowired
    private QueryProfiler queryProfiler;

    @Before
    public void setUp() throws IOException {
        initFuseki();
        initRsine();
    }

    private void initFuseki() {
        Collection<URI> allRdfFiles = new ArrayList<URI>();
        int fileCnt = 0;
        for (File file : new File(DATASET_DIR).listFiles()) {
            if (fileCnt > MAX_FILE_CNT) break;

            allRdfFiles.add(file.toURI());
            fileCnt++;
        }

        DatasetGraph graph = Helper.initFuseki(allRdfFiles, "dataset");
        loadedTriples = graph.getDefaultGraph().size();
    }

    private void initRsine() throws IOException {
        rsine.start();
        helper.postSubscriptionTtl("/wk/subscription_dm_all_doc_metadata.ttl");
        installStatisticsNotifier();
    }

    private void installStatisticsNotifier() {
        Iterator<Subscription> subscriptionIterator = rsine.getSubscriptions();
        while (subscriptionIterator.hasNext()) {
            Subscription subscription = subscriptionIterator.next();
            subscription.addNotifier(statisticsNotifier);
        }
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        Fuseki.getServer().stop();
        rsine.stop();
    }

    @Ignore
    @Test
    public void queryExecutionTime() throws OpenRDFException, IOException {
        int simulatedChanges = 10;

        for (int i = 0; i < simulatedChanges; i++) {
            Statement randomStatement = getRandomStatement();
            helper.postStatementAdded(randomStatement);
        }

        int meanQueryProcessingTime = queryProfiler.getMeanQueryProcessingTime();
        Assert.assertTrue(meanQueryProcessingTime < 100);

        logger.info("graph size: " +loadedTriples+ " triples");
        logger.info("Mean query processing time: " +meanQueryProcessingTime+ "ms");
        logger.info("Number of notifications: " +statisticsNotifier.getNotificationCount());
    }

    private Statement getRandomStatement()
        throws MalformedQueryException, RepositoryException, QueryEvaluationException
    {
        long offset = Math.round(Math.random() * loadedTriples);
        String query = "SELECT * WHERE { ?s ?p ?o } ORDER BY ?s OFFSET " +offset+ " LIMIT 1";
        RepositoryConnection repCon = new SPARQLConnection(new SPARQLRepository(DATASET_SPARQL_ENDPOINT));
        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
        BindingSet bs = result.next();
        return new StatementImpl((Resource) bs.getValue("s"), (org.openrdf.model.URI) bs.getValue("p"), bs.getValue("o"));
    }

    private class StatisticsNotifier implements INotifier {

        private Collection<Collection<String>> notifications = new ArrayList<Collection<String>>();

        @Override
        public void notify(Collection<String> messages) {
            notifications.add(messages);
        }

        int getNotificationCount() {
            return notifications.size();
        }

    }
}
