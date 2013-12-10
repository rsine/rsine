package eu.lod2.rsine.queryhandling;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.queryhandling.policies.IEvaluationPolicy;
import eu.lod2.rsine.registrationservice.Condition;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
    private QueryProfiler queryProfiler;

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

    public List<String> evaluate(NotificationQuery query, IEvaluationPolicy usePolicy)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        usePolicy.checkEvaluationNeeded(query);

        RepositoryConnection changeSetCon = changeSetStore.getRepository().getConnection();
        RepositoryConnection managedStoreCon = new SPARQLConnection(new SPARQLRepository(managedTripleStoreSparqlEndpoint));
        try {
            String issuedQuery = fillInPlaceholders(query);
            long start = System.currentTimeMillis();
            List<String> messages = createMessages(query, issuedQuery, changeSetCon, managedStoreCon);
            queryProfiler.log(issuedQuery, System.currentTimeMillis() - start);

            return messages;
        }
        finally {
            changeSetCon.close();
            managedStoreCon.close();
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

    private List<String> createMessages(NotificationQuery query,
                                        String issuedQuery,
                                        RepositoryConnection repCon,
                                        RepositoryConnection managedStoreCon)
        throws MalformedQueryException, RepositoryException, QueryEvaluationException
    {
        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, issuedQuery).evaluate();
        query.updateLastIssued();

        List<String> messages = new ArrayList<String>();
        while (result.hasNext()) {
            BindingSet bs = result.next();

            if (evaluateConditions(query.getConditions(), bs, managedStoreCon)) {
                messages.add(query.getBindingSetFormatter().toMessage(bs));
            }
        }

        return messages;
    }

    private boolean evaluateConditions(Iterator<Condition> conditions,
                                       BindingSet bs,
                                       RepositoryConnection managedStoreCon)
    {
        boolean allConditionsFulfilled = true;

        while (conditions.hasNext()) {
            Condition condition = conditions.next();
            try {
                allConditionsFulfilled &= evaluateCondition(condition, bs, managedStoreCon);
            }
            catch (Exception e) {
                logger.error("Ignoring condition due to error", e);
            }
        }

        return allConditionsFulfilled;
    }

    private boolean evaluateCondition(Condition condition, BindingSet bs, RepositoryConnection managedStoreCon)
        throws MalformedQueryException, RepositoryException, QueryEvaluationException
    {
        BooleanQuery booleanQuery = managedStoreCon.prepareBooleanQuery(QueryLanguage.SPARQL, condition.getAskQuery());
        for (String bindingName : bs.getBindingNames()) {
            booleanQuery.setBinding(bindingName, bs.getBinding(bindingName).getValue());
        }

        return booleanQuery.evaluate() == condition.getExpectedResult();
    }

}
