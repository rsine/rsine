package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.queryhandling.EvaluationPostponedException;
import eu.lod2.util.ItemNotFoundException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Component
public class ChangeTripleService extends PostRequestHandler {

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

    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {
        logger.debug("Handling change triple request");

        try {
            Properties properties = new Properties();
            properties.load(request.getEntity().getContent());

            String changeType = getValueForName(POST_BODY_CHANGETYPE, properties);
            List<Statement> triples = extractStatements(properties, changeType);

            Model changeSet = changeSetCreator.assembleChangeset(triples.get(0), triples.get(1), changeType);
            persistAndNotifyProvider.persistAndNotify(changeSet, false);
        }
        catch (ItemNotFoundException e) {
            errorResponse(response, "No triple or change type provided");
        }
        catch (RDFParseException e) {
            errorResponse(response, "Error parsing provided triple");
        }
        catch (IOException e) {
            errorResponse(response, e.getMessage());
        }
        catch (RDFHandlerException e) {
            errorResponse(response, e.getMessage());
        }
        catch (EvaluationPostponedException e) {
            // ignore; when encountering this exception do nothing (special)
        }
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
        RDFParser parser = new TurtleParserFactory().getParser();
        SingleStatementHandler singleStatementHandler = new SingleStatementHandler();
        parser.setRDFHandler(singleStatementHandler);
        parser.parse(new StringReader(triple.trim()), "http://some.base.uri/");
        return singleStatementHandler.getStatement();
    }

    private void errorResponse(HttpResponse response, String message) {
        logger.error(message);
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        response.setReasonPhrase(message);
    }

    public void setPersistAndNotifyProvider(PersistAndNotifyProvider persistAndNotifyProvider) {
        this.persistAndNotifyProvider = persistAndNotifyProvider;
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
