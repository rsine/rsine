package at.punkt.lod2;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.changesetservice.ChangeSetService;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
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
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class ChangesetServiceTest {

    @Autowired
    private ChangeSetService changeSetService;

    @Autowired
    private Helper helper;

    @Autowired
    private ChangeSetStore changeSetStore;

    @Before
    public void setUp() throws IOException, RepositoryException {
        System.out.println("starting service");
        changeSetService.start();
    }

    @After
    public void after() throws IOException, InterruptedException {
        System.out.println("stopping service");
        changeSetService.stop();
    }

    @Test
    public void postTripleChange() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(200, helper.doPost(props));
    }    

    @Test
    public void postManuallyAssembledProperties() throws IOException {
        String entityContent = ChangeTripleHandler.POST_BODY_CHANGETYPE +"=add\n" +ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE+ "=<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .";
        HttpPost httpPost = new HttpPost("http://localhost:" +helper.getChangeSetListeningPort());
        httpPost.setEntity(new StringEntity(entityContent));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void postEmptyTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "");

        Assert.assertEquals(400, helper.doPost(props));
    }

    @Test
    public void postIllegalTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "http://www.example.org/someconcept a skos:Concept .");

        Assert.assertEquals(400, helper.doPost(props));
    }

    @Test
    public void postInvalidEofTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en");

        Assert.assertEquals(400, helper.doPost(props));
    }

    @Test
    public void postMissingTriple() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);

        Assert.assertEquals(400, helper.doPost(props));
    }

    /**
     * Posting an update results in creation of a changeset with both removal and addition statements
     */
    @Test(timeout = 2000)
    public void postUpdate()
        throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_UPDATE);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");
        props.setProperty(ChangeTripleHandler.POST_BODY_SECONDARYTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"updatedlabel\"@en .");

        helper.doPost(props);
        waitForChangeSetWritten();
    }

    private void waitForChangeSetWritten()
        throws RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();
        TupleQuery query = repCon.prepareTupleQuery(QueryLanguage.SPARQL,
                Namespaces.SKOS_PREFIX +
                        Namespaces.CS_PREFIX +
                        "SELECT * " +
                        "WHERE {" +
                        "?cs a cs:ChangeSet . " +
                        "?cs cs:removal ?removal . " +
                        "?cs cs:addition ?addition . " +
                        "}");

        boolean resultAvailable = false;
        while (!resultAvailable) {
            TupleQueryResult result = query.evaluate();
            resultAvailable = result.hasNext();
        }
    }

    @Test
    public void postMissingChangeType() throws IOException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        Assert.assertEquals(400, helper.doPost(props));
    }

    @Test(timeout = 2000)
    public void tripleChangeToRepo() throws IOException, RepositoryException {
        Properties props = new Properties();
        props.setProperty(ChangeTripleHandler.POST_BODY_CHANGETYPE, ChangeTripleHandler.CHANGETYPE_ADD);
        props.setProperty(ChangeTripleHandler.POST_BODY_AFFECTEDTRIPLE, "<http://example.org/myconcept> <http://www.w3.org/2004/02/skos/core#prefLabel> \"somelabel\"@en .");

        helper.doPost(props);
        Assert.assertEquals(1, waitForChangeSetCreated());
    }

    private int waitForChangeSetCreated() throws RepositoryException {
        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();

        Collection<Statement> statements = Collections.EMPTY_LIST;
        while (statements.isEmpty()) {
            RepositoryResult<Statement> result = repCon.getStatements(
                null,
                RDF.TYPE,
                ValueFactoryImpl.getInstance().createURI(Namespaces.CS_NAMESPACE.getName(), "ChangeSet"),
                false);
            statements = Iterations.asList(result);
        }

        return statements.size();
    }

    private class DummyQueryDispatcher implements IQueryDispatcher {

        @Override
        public void trigger() {
            //don't do nothing here
        }

    }

}
