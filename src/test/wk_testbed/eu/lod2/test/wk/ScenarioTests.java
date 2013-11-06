package eu.lod2.test.wk;

import at.punkt.lod2.util.Helper;
import org.apache.jena.fuseki.Fuseki;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class ScenarioTests {

    private final int MAX_FILE_CNT = 10;
    private final static String DATASET_DIR = "/home/christian/Downloads/dumps";

    @Before
    public void setUp() {
        initFuseki();
    }

    private void initFuseki() {
        Collection<URI> allRdfFiles = new ArrayList<URI>();
        int fileCnt = 0;
        for (File file : new File(DATASET_DIR).listFiles()) {
            if (fileCnt > MAX_FILE_CNT) break;

            allRdfFiles.add(file.toURI());
            fileCnt++;
        }

        Helper.initFuseki(allRdfFiles, "dataset");
    }

    @After
    public void tearDown() {
        Fuseki.getServer().stop();
    }

    @Test
    public void queryExecutionTime() {
        System.out.println("xxx");
    }

}
