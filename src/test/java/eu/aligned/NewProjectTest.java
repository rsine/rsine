package eu.aligned;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.registrationservice.SubscriptionParser;
import eu.lod2.rsine.service.ChangeSetFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.util.Collections;

public class NewProjectTest {

    private Repository repo;

    @Before
    public void setUp() throws IOException, OpenRDFException {
        repo = setUpFromTestResource("/eu/aligned/new_project_changeset.trig", RDFFormat.TRIG);
    }

    public Repository setUpFromTestResource(String fileName, RDFFormat format) throws OpenRDFException, IOException {
        Model modelFromResourceFile = Helper.createModelFromResourceFile(fileName, format);
        Model changeSet = new ChangeSetFactory().assembleChangeset(modelFromResourceFile, Collections.EMPTY_LIST);

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        RepositoryConnection repCon = repository.getConnection();

        try {
            repCon.add(changeSet);
        }
        finally {
            repCon.close();
        }

        return repository;
    }

    @Test
    public void checkChangeSet() throws OpenRDFException, IOException {
        RepositoryConnection repCon = repo.getConnection();

        TurtleWriter writer = new TriGWriter(System.out);
        writer.startRDF();
        RepositoryResult<Statement> stats = repCon.getStatements(null, null, null, false);
        while (stats.hasNext()) {
            writer.handleStatement(stats.next());
        }
        writer.endRDF();

        Model rdfSubscription = Helper.createModelFromResourceFile("/eu/aligned/new_project_subscription.ttl", RDFFormat.TURTLE);

        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();
        NotificationQuery query = subscription.getQueries().next();
        String sparqlQuery = query.getSparqlQuery();
        int startFilter = sparqlQuery.indexOf("FILTER");
        sparqlQuery = sparqlQuery.substring(0, startFilter) + "}";

        TupleQueryResult res = repCon.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();
        Assert.assertTrue(res.hasNext());
    }

}
