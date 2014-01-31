package eu.lod2.rsine.changesetstore;

import eu.lod2.util.Namespaces;
import info.aduna.iteration.Iterations;
import org.apache.commons.io.FileUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@Component
public class ChangeSetStore {

    private final Logger logger = LoggerFactory.getLogger(ChangeSetStore.class);
    private Repository repository;

    public ChangeSetStore() throws IOException, RepositoryException {
        repository = new SailRepository(new NativeStore(createDataDir()));
        repository.initialize();
    }

    public synchronized void shutdown() throws RepositoryException, IOException {
        repository.shutDown();
        FileUtils.deleteDirectory(createDataDir());
    }

    private File createDataDir() throws IOException {
        File dataDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "rsine");

        if (dataDir.exists()) {
            FileUtils.cleanDirectory(dataDir);
        }

        return dataDir;
    }

    public synchronized void persistChangeSet(Graph changeSet) throws RepositoryException {
        RepositoryConnection repCon = repository.getConnection();
        repCon.add(changeSet);

        repCon.close();
        logger.debug("created changeset: " +formatChangeSet(changeSet));
    }

    private String formatChangeSet(Graph changeSet) {
        StringWriter sw = new StringWriter();
        TurtleWriter turtleWriter = new TurtleWriter(sw);

        try {
            turtleWriter.startRDF();
            Iterator<Statement> statementIterator = changeSet.iterator();
            while (statementIterator.hasNext()) {
                turtleWriter.handleStatement(statementIterator.next());
            }
            turtleWriter.endRDF();
        }
        catch (RDFHandlerException e) {
            return "Could not format changeset";
        }
        return sw.toString();
    }

    public synchronized Collection<BindingSet> evaluateQuery(String query) throws OpenRDFException {
        Collection<BindingSet> bindingSets = new ArrayList<BindingSet>();

        RepositoryConnection repCon = repository.getConnection();
        try {
            TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();

            while (result.hasNext()) {
                bindingSets.add(result.next());
            }

            return bindingSets;
        }
        finally {
            repCon.close();
        }
    }

    public synchronized int getChangeSetCount() throws RepositoryException {
        RepositoryConnection repCon = repository.getConnection();

        try {
            RepositoryResult<Statement> result = repCon.getStatements(
                    null,
                    RDF.TYPE,
                    ValueFactoryImpl.getInstance().createURI(Namespaces.CS_NAMESPACE.getName(), "ChangeSet"),
                    false);
            return Iterations.asList(result).size();
        }
        finally {
            repCon.close();
        }
    }

}
