package eu.lod2.rsine.changesetstore;

import org.openrdf.model.Graph;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;

public class ChangeSetStore {

    private Repository repository;
    private boolean isInitialized;

    public ChangeSetStore() {
        File tempDir = new File(createDataDirName());
        repository = new SailRepository(new MemoryStore(tempDir));
    }

    private String createDataDirName() {
        return System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis();
    }

    public void persistChangeSet(Graph changeSet) throws RepositoryException {
        ensureInitialized();

        RepositoryConnection repCon = repository.getConnection();
        repCon.add(changeSet);
        repCon.close();
    }

    public Repository getRepository() throws RepositoryException {
        ensureInitialized();
        return repository;
    }

    private void ensureInitialized() throws RepositoryException {
        if (!isInitialized) {
            repository.initialize();
            isInitialized = true;
        }
    }

}
