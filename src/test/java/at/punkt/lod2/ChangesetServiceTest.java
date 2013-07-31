package at.punkt.lod2;

import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetservice.RequestHandlerFactory;
import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.util.Namespaces;
import info.aduna.iteration.Iterations;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.util.Properties;

public class ChangesetServiceTest {

    private final int port = new TestUtils().getRandomPort();
    private ChangeSetService changeSetService;
    private ChangeSetStore changeSetStore;

    @Before
    public void setUp() throws IOException, RepositoryException {
        RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory();
        requestHandlerFactory.setChangeSetCreator(new ChangeSetCreator());
        requestHandlerFactory.setQueryDispatcher(new DummyQueryDispatcher());

        changeSetStore = new ChangeSetStore();
        requestHandlerFactory.setChangeSetStore(changeSetStore);

        changeSetService = new ChangeSetService(port);
        changeSetService.setRequestHandlerFactory(requestHandlerFactory);

        changeSetService.start();
    }

    @After
    public void tearDown() throws InterruptedException, IOException {
        changeSetService.stop();
    }

    @Test
    public void postTripleChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(200, new TestUtils().doPost(port, props));
    }    

    @Test
    public void postManuallyAssembledProperties() throws IOException {
        String entityContent = ChangeTripleHandler.POST_BODY_CHANGETYPE +"=add\n" +ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE+ "=<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .";
        HttpPost httpPost = new HttpPost("http://localhost:" +port);
        httpPost.setEntity(new StringEntity(entityContent));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void postEmptyTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "");

        Assert.assertEquals(400, new TestUtils().doPost(port, props));
    }

    @Test
    public void postIllegalTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "http://www.example.org/someconcept a skos:Concept .");

        Assert.assertEquals(400, new TestUtils().doPost(port, props));
    }

    @Test
    public void postInvalidEofTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en");

        Assert.assertEquals(400, new TestUtils().doPost(port, props));
    }

    @Test
    public void postMissingTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);

        Assert.assertEquals(400, new TestUtils().doPost(port, props));
    }

    /**
     * Posting an update results in creation of a changeset with both removal and addition statements
     */
    @Test
    public void postUpdate()
        throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");
        props.setProperty(ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"updatedlabel\"@en .");

        new TestUtils().doPost(port, props);

        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();
        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL,
                Namespaces.SKOS_PREFIX +
                        Namespaces.CS_PREFIX +
                        "SELECT * " +
                        "WHERE {" +
                            "?cs a cs:ChangeSet . " +
                            "?cs cs:removal ?removal . " +
                            "?cs cs:addition ?addition . " +
                        "}").evaluate();

        Assert.assertTrue(result.hasNext());
    }

    @Test
    public void postMissingChangeType() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(400, new TestUtils().doPost(port, props));
    }

    @Test
    public void tripleChangeToRepo() throws IOException, RepositoryException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        new TestUtils().doPost(port, props);

        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();
        RepositoryResult<Statement> result = repCon.getStatements(
            null,
            RDF.TYPE,
            ValueFactoryImpl.getInstance().createURI(Namespaces.CS_NAMESPACE.getName(), "ChangeSet"),
            false);

        Assert.assertEquals(1, Iterations.asList(result).size());
    }

    private class DummyQueryDispatcher implements IQueryDispatcher {

        @Override
        public void trigger() {
            //don't do nothing here
        }

    }

}
