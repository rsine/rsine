package at.punkt.lod2.util;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import eu.lod2.rsine.Rsine;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

public class Helper {

    private int changeSetListeningPort;

    public Helper(int changeSetListeningPort) {
        this.changeSetListeningPort = changeSetListeningPort;
    }

    public int doPost(Properties properties) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:" +changeSetListeningPort);
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        httpPost.setEntity(new StringEntity(sw.toString()));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        return response.getStatusLine().getStatusCode();
    }

    public SPARQLServer initFuseki(URL rdfFile, String datasetName) {
        DatasetGraph datasetGraph = DatasetGraphFactory.createMem();
        RDFDataMgr.read(datasetGraph, new File(rdfFile.getFile()).toURI().toString());

        ServerConfig serverConfig = FusekiConfig.defaultConfiguration(datasetName, datasetGraph, true) ;
        SPARQLServer fusekiServer = new SPARQLServer(serverConfig) ;
        Fuseki.setServer(fusekiServer);
        fusekiServer.start();

        return fusekiServer;
    }

    public Model createModelFromResourceFile(String fileName, RDFFormat format)
        throws RDFParseException, IOException, RDFHandlerException
    {
        RDFParser rdfParser = Rio.createParser(format);
        Model model = new TreeModel();
        StatementCollector collector = new StatementCollector(model);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(Rsine.class.getResourceAsStream(fileName), "");
        return model;
    }

    public int getChangeSetListeningPort() {
        return changeSetListeningPort;
    }

}
