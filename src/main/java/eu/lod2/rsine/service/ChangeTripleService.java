package eu.lod2.rsine.service;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.trig.TriGParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

@Service
public class ChangeTripleService {

    private final Logger logger = LoggerFactory.getLogger(ChangeTripleService.class);

    public static String POST_BODY_ADDEDTRIPLES = "addedTriples";
    public static String POST_BODY_REMOVEDTRIPLES = "removedTriples";

    @Autowired
    private ChangeSetFactory changeSetFactory;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    void handleAnnouncedTriples(String announceTriples) throws IOException, RDFParseException, RDFHandlerException {
        logger.debug("Incoming triple change announcement: " +announceTriples);

        Properties properties = new Properties();
        properties.load(new StringReader(announceTriples));

        String addedTriples = getValueForName(POST_BODY_ADDEDTRIPLES, properties);
        String removedTriples = getValueForName(POST_BODY_REMOVEDTRIPLES, properties);

        Collection<Statement> addedStatements = extractStatements(addedTriples);
        Collection<Statement> removedStatements = extractStatements(removedTriples);

        Model changeSet = changeSetFactory.assembleChangeset(addedStatements, removedStatements);
        persistAndNotifyProvider.persistAndNotify(changeSet, false);
    }

    private String getValueForName(String key, Properties properties) {
        String value = properties.getProperty(key);
        return value == null ? "" : value;
    }

    private Collection<Statement> extractStatements(String trigTriples)
            throws RDFParseException, IOException, RDFHandlerException
    {
        RDFParser parser = new TriGParserFactory().getParser();
        StatementsCollector statementsCollector = new StatementsCollector();
        parser.setRDFHandler(statementsCollector);
        parser.parse(new StringReader(trigTriples), "http://some.base.uri/");
        return statementsCollector.getStatements();
    }

    private class StatementsCollector extends RDFHandlerBase {

        private Collection<Statement> statements = new ArrayList<Statement>();

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            statements.add(st);
        }

        Collection<Statement> getStatements() {
            return statements;
        }

    }

}
