package eu.lod2.rsine.queryhandling;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.queryhandling.policies.IEvaluationPolicy;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class QueryEvaluator {

    private final Logger logger = LoggerFactory.getLogger(QueryEvaluator.class);
    public final static String QUERY_LAST_ISSUED = "QUERY_LAST_ISSUED";
    public final static String MANAGED_STORE_SPARQL_ENDPOINT = "MANAGED_STORE_SPARQL_ENDPOINT";
    public final static String AUTH_URI = "AUTH_URI";

    @Autowired
    private ChangeSetStore changeSetStore;

    @Autowired
    private IEvaluationPolicy evaluationPolicy;

    private String managedTripleStoreSparqlEndpoint, authoritativeUri;

    public QueryEvaluator() {
        managedTripleStoreSparqlEndpoint = "";
        authoritativeUri = "";
    }

    public QueryEvaluator(String sparqlEndpoint, String authoritativeUri) {
        this();
        this.managedTripleStoreSparqlEndpoint = sparqlEndpoint;
        this.authoritativeUri = authoritativeUri;
    }

    public List<String> evaluate(NotificationQuery query)
        throws RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        evaluationPolicy.checkEvaluate(query);

        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();
        try {
            String issuedQuery = fillInPlaceholders(query);
            long start = System.currentTimeMillis();
            List<String> messages = createMessages(query, issuedQuery, repCon);
            logger.info("Query execution and message creation took " +(System.currentTimeMillis() - start) +"ms");
            return messages;
        }
        finally {
            repCon.close();
        }
    }

    private String fillInPlaceholders(NotificationQuery query) {
        String sparqlQuery;
        sparqlQuery = amendChangeSetsTimeConstraint(query);
        sparqlQuery = sparqlQuery.replace(MANAGED_STORE_SPARQL_ENDPOINT, managedTripleStoreSparqlEndpoint);
        sparqlQuery = sparqlQuery.replace(AUTH_URI, authoritativeUri);
        return sparqlQuery;
    }

    /**
     * Replaces the placeholder in the subscriber query with the date the query has been last issued. This way only
     * changesets that have an creation date after the query hast been last issued are returned
     */
    private String amendChangeSetsTimeConstraint(NotificationQuery query) {
        String sparqlQuery = query.getSparqlQuery();
        String queryLastIssuedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ").format(query.getLastIssued());

        // fix timezone format
        queryLastIssuedDate = new StringBuffer(queryLastIssuedDate).insert(queryLastIssuedDate.length() - 2, ":").toString();

        return sparqlQuery.replace(QUERY_LAST_ISSUED, queryLastIssuedDate);
    }

    private List<String> createMessages(NotificationQuery query, String issuedQuery, RepositoryConnection repCon)
        throws MalformedQueryException, RepositoryException, QueryEvaluationException
    {
        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, issuedQuery).evaluate();

        List<String> messages = new ArrayList<String>();
        while (result.hasNext()) {
            BindingSet bs = result.next();
            messages.add(query.getBindingSetFormatter().toMessage(bs));
        }
        query.updateLastIssued();

        return messages;
    }
}
