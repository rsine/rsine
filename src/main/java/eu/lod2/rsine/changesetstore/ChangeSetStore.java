package eu.lod2.rsine.changesetstore;

import eu.lod2.util.Namespaces;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;

public class ChangeSetStore {

    private final static String CS_CONTEXT = Namespaces.CS_NAMESPACE + "changesets";
    private Repository repository;

    public ChangeSetStore() throws RepositoryException {
        File tempDir = new File(createDataDirName());
        repository = new SailRepository(new MemoryStore(tempDir));
        repository.initialize();
    }

    private String createDataDirName() {
        return System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis();
    }

    public void persistChangeSet(Graph changeSet) throws RepositoryException {
        ValueFactory valueFactory = new ValueFactoryImpl();
        repository.getConnection().add(changeSet, valueFactory.createURI(CS_CONTEXT));
    }

    public Repository getRepository() {
        return repository;
    }

}
