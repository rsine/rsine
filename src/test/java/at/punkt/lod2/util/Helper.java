package at.punkt.lod2.util;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetservice.PersistAndNotifyProvider;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class Helper {

    public static DatasetGraph initFuseki(URL rdfFile, String datasetName) {
        URI rdfFileUri = new File(rdfFile.getFile()).toURI();
        DatasetGraph datasetGraph = DatasetGraphFactory.createMem();
        RDFDataMgr.read(datasetGraph, rdfFileUri.toString());
        ServerConfig serverConfig = FusekiConfig.defaultConfiguration(datasetName, datasetGraph, true) ;
        SPARQLServer fusekiServer = new SPARQLServer(serverConfig);
        Fuseki.setServer(fusekiServer);
        fusekiServer.start();
        return datasetGraph;
    }

    public static Model createModelFromResourceFile(String fileName, RDFFormat format)
        throws RDFParseException, IOException, RDFHandlerException
    {
        RDFParser rdfParser = Rio.createParser(format);
        Model model = new TreeModel();
        StatementCollector collector = new StatementCollector(model);
        rdfParser.setRDFHandler(collector);
        rdfParser.parse(Rsine.class.getResourceAsStream(fileName), "");
        return model;
    }

    public static Model createChangeSetModel(String subjUri, String predUri, Value obj, String changeType) {
        return new ChangeSetCreator().assembleChangeset(
                new StatementImpl(new URIImpl(subjUri), new URIImpl(predUri), obj),
                null,
                changeType);
    }

    public static Model createChangeSetModel(String subjUri1, String predUri1, Value obj1,
                                      String subjUri2, String predUri2, Value obj2,
                                      String changeType)
    {
        return new ChangeSetCreator().assembleChangeset(
                new StatementImpl(new URIImpl(subjUri1), new URIImpl(predUri1), obj1),
                new StatementImpl(new URIImpl(subjUri2), new URIImpl(predUri2), obj2),
                changeType);
    }

    public static void setLabel(RepositoryConnection repCon,
                                org.openrdf.model.URI concept,
                                org.openrdf.model.URI labelType,
                                Literal newlabel,
                                PersistAndNotifyProvider persistAndNotifyProvider) throws RepositoryException
    {
        repCon.add(concept, labelType, newlabel);

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(concept.stringValue(),
                        labelType.stringValue(),
                        newlabel,
                        ChangeTripleHandler.CHANGETYPE_ADD),
                true);
    }

    public static void setAltLabel(RepositoryConnection repCon,
                            org.openrdf.model.URI concept,
                            Literal newAltLabel,
                            PersistAndNotifyProvider persistAndNotifyProvider) throws RepositoryException
    {
        setLabel(repCon, concept, SKOS.ALT_LABEL, newAltLabel, persistAndNotifyProvider);
    }

}
