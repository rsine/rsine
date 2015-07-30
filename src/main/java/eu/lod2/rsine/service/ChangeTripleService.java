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

    public static String POST_BODY_AFFECTEDTRIPLE = "affectedTriple";
    public static String POST_BODY_SECONDARYTRIPLE = "secondaryTriple";
    public static String POST_BODY_CHANGETYPE = "changeType";
    public static String CHANGETYPE_ADD = "add";
    public static String CHANGETYPE_REMOVE = "remove";
    public static String CHANGETYPE_UPDATE = "update";

    @Autowired
    private ChangeSetCreator changeSetCreator;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    void handleAnnouncedTriple(String announceTriple) throws IOException, RDFParseException, RDFHandlerException {
        logger.debug("Incoming triple change announcement: " +announceTriple);

        Properties properties = new Properties();
        properties.load(new StringReader(announceTriple));

        String changeType = getValueForName(POST_BODY_CHANGETYPE, properties);
        List<Statement> triples = extractStatements(properties, changeType);

        Model changeSet = changeSetCreator.assembleChangeset(triples.get(0), triples.get(1), changeType);
        persistAndNotifyProvider.persistAndNotify(changeSet, false);
    }

    private String getValueForName(String key, Properties properties) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new ItemNotFoundException("Key '" +key+ "' not found in request properties");
        }
        return value;
    }

    private List<Statement> extractStatements(Properties properties, String changeType)
            throws RDFParseException, IOException, RDFHandlerException
    {
        Statement secondaryTriple = null;
        Statement affectedTriple = createStatement(getValueForName(POST_BODY_AFFECTEDTRIPLE, properties));

        try {
            secondaryTriple = createStatement(getValueForName(POST_BODY_SECONDARYTRIPLE, properties));
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
