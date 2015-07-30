package eu.lod2.rsine.service;

import eu.lod2.util.ItemNotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class ChangeTripleService {

    private final Logger logger = LoggerFactory.getLogger(ChangeTripleService.class);

    public static String POST_BODY_ADDEDTRIPLES = "addedTriples";
    public static String POST_BODY_REMOVEDTRIPLES = "removedTriples";

    @Autowired
    private ChangeSetCreator changeSetCreator;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    void handleAnnouncedTriples(String announceTriples) throws IOException, RDFParseException, RDFHandlerException {
        logger.debug("Incoming triple change announcement: " +announceTriples);

        Properties properties = new Properties();
        properties.load(new StringReader(announceTriples));

        String addedTriples = getValueForName(POST_BODY_ADDEDTRIPLES, properties);
        String removedTriples = getValueForName(POST_BODY_REMOVEDTRIPLES, properties);

        List<Statement> addedStatements = extractStatements(addedTriples);
        List<Statement> removedStatements = extractStatements(removedTriples);


        Model changeSet = changeSetCreator.assembleChangeset(addedStatements, removedStatements);
        persistAndNotifyProvider.persistAndNotify(changeSet, false);
    }

    private String getValueForName(String key, Properties properties) {
        String value = properties.getProperty(key);
        return value == null ? "" : value;
    }

    private List<Statement> extractStatements(Properties properties, String changeType)
            throws RDFParseException, IOException, RDFHandlerException
    {
        Statement secondaryTriple = null;
        Statement affectedTriple = createStatement(getValueForName(POST_BODY_AFFECTEDTRIPLES, properties));

        try {
            secondaryTriple = createStatement(getValueForName(POST_BODY_SECONDARYTRIPLES, properties));
        }
        catch (ItemNotFoundException e) {
            if (changeType.equals(CHANGETYPE_UPDATE)) {
                throw e;
            }
        }

        return Arrays.asList(affectedTriple, secondaryTriple);
    }

    private Statement createStatement(String triple) throws RDFParseException, IOException, RDFHandlerException {
        RDFParser parser = new TriGParserFactory().getParser();
        SingleStatementHandler singleStatementHandler = new SingleStatementHandler();
        parser.setRDFHandler(singleStatementHandler);
        parser.parse(new StringReader(triple.trim()), "http://some.base.uri/");
        return singleStatementHandler.getStatement();
    }

    private class SingleStatementHandler extends RDFHandlerBase {

        private Statement statement;

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            statement = st;
        }

        Statement getStatement() {
            if (statement == null) throw new ItemNotFoundException("No statement parsed");
            return statement;
        }

    }

}
