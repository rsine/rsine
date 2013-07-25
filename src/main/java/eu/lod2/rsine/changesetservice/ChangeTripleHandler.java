package eu.lod2.rsine.changesetservice;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.querydispatcher.IQueryDispatcher;
import eu.lod2.util.ItemNotFoundException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class ChangeTripleHandler extends PostRequestHandler {

    public static String POST_BODY_CHANGETYPE = "changeType";
    public static String CHANGETYPE_ADD = "add";
    public static String CHANGETYPE_REMOVE = "remove";
    public static String CHANGETYPE_UPDATE = "update";
    public static String POST_BODY_AFFECTEDTRIPLE = "affectedTriple";
    public static String POST_BODY_SECONDARYTRIPLE = "secondaryTriple";

    private ChangeSetCreator changeSetCreator;
    private ChangeSetStore changeSetStore;
    private IQueryDispatcher queryDispatcher;

    @Override
    protected void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response) {
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(request.getEntity());
            String changeType = getValueForName(POST_BODY_CHANGETYPE, params);
            List<Statement> triples = extractStatements(params, changeType);

            Graph changeSet = changeSetCreator.assembleChangeset(triples.get(0), triples.get(1), changeType);
            changeSetStore.persistChangeSet(changeSet);

            queryDispatcher.trigger();
        }
        catch (ItemNotFoundException e) {
            errorResponse(response, "No triple or change type provided");
        }
        catch (RDFParseException e) {
            errorResponse(response, "Error parsing provided triple");
        }
        catch (RepositoryException e) {
            errorResponse(response, "Error persisting changeset");
        }
        catch (IOException e) {
            errorResponse(response, e.getMessage());
        }
        catch (RDFHandlerException e) {
            errorResponse(response, e.getMessage());
        }
    }

    private List<Statement> extractStatements(List<NameValuePair> params, String changeType)
        throws RDFParseException, IOException, RDFHandlerException
    {
        Statement affectedTriple = null;
        Statement secondaryTriple = null;

        affectedTriple = createStatement(getValueForName(POST_BODY_AFFECTEDTRIPLE, params));

        try {
            secondaryTriple = createStatement(getValueForName(POST_BODY_SECONDARYTRIPLE, params));
        }
        catch (ItemNotFoundException e) {
            if (changeType.equals(CHANGETYPE_UPDATE)) {
                throw e;
            }
        }

        return Arrays.asList(affectedTriple, secondaryTriple);
    }

    private void errorResponse(HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        response.setReasonPhrase(message);
    }

    private String getValueForName(String name, List<NameValuePair> nameValuePairs) {
        for (NameValuePair nameValuePair : nameValuePairs) {
            if (nameValuePair.getName().equals(name)) {
                return nameValuePair.getValue();
            }
        }
        throw new ItemNotFoundException("Name '" +name+ "' not found in name-value pairs");
    }

    private Statement createStatement(String triple) throws RDFParseException, IOException, RDFHandlerException {
        RDFParser parser = new NTriplesParserFactory().getParser();
        SingleStatementHandler singleStatementHandler = new SingleStatementHandler();
        parser.setRDFHandler(singleStatementHandler);
        parser.parse(new StringReader(triple), "http://some.base.uri/");
        return singleStatementHandler.getStatement();
    }

    public void setChangeSetCreator(ChangeSetCreator changeSetCreator) {
        this.changeSetCreator = changeSetCreator;
    }

    public void setChangeSetStore(ChangeSetStore changeSetStore) {
        this.changeSetStore = changeSetStore;
    }

    public void setQueryDispatcher(IQueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
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
