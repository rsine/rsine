package at.punkt.lod2.util;

import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetservice.PersistAndNotifyProvider;
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

import java.io.IOException;

public class Helper {

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

    public static void setAltLabel(RepositoryConnection repCon,
                            org.openrdf.model.URI concept,
                            Literal newAltLabel,
                            PersistAndNotifyProvider persistAndNotifyProvider) throws RepositoryException
    {
        repCon.add(concept, SKOS.ALT_LABEL, newAltLabel);

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(concept.stringValue(),
                        SKOS.ALT_LABEL.stringValue(),
                        newAltLabel,
                        ChangeTripleHandler.CHANGETYPE_ADD),
                true);
    }

}
