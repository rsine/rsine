package eu.lod2.rsine.changesetstore;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.StringWriter;
import java.util.Iterator;

@Component
public class ChangeSetStore {

    private final Logger logger = LoggerFactory.getLogger(ChangeSetStore.class);
    private Repository repository;
    private boolean isInitialized;

    public ChangeSetStore() {
        File tempDir = new File(createDataDirName());
        repository = new SailRepository(new NativeStore(tempDir));
    }

    private String createDataDirName() {
        return System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis();
    }

    public synchronized void persistChangeSet(Graph changeSet) throws RepositoryException {
        ensureInitialized();

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


    public Repository getRepository() throws RepositoryException {
        ensureInitialized();
        return repository;
    }

    private synchronized void ensureInitialized() throws RepositoryException {
        if (!isInitialized) {
            repository.initialize();
            isInitialized = true;
        }
    }

}
