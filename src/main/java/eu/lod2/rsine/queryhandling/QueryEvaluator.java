package eu.lod2.rsine.queryhandling;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.queryhandling.policies.IEvaluationPolicy;
import eu.lod2.rsine.registrationservice.Condition;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

@Component
public class QueryEvaluator {

    private final Logger logger = LoggerFactory.getLogger(QueryEvaluator.class);
    public final static String QUERY_LAST_ISSUED = "QUERY_LAST_ISSUED";
    public final static String AUTH_URI = "AUTH_URI";

    @Autowired
    private ChangeSetStore changeSetStore;

    @Autowired
    private QueryProfiler queryProfiler;

    @Autowired
    private RepositoryConnection managedStoreCon;

    private String authoritativeUri = "";

    public QueryEvaluator() {

    }

    public QueryEvaluator(String authoritativeUri) {
        this.authoritativeUri = authoritativeUri;
    }

    public Collection<String> evaluate(NotificationQuery query, IEvaluationPolicy usePolicy) throws OpenRDFException
    {
        usePolicy.checkEvaluationNeeded(query);

        //RepositoryConnection managedStoreCon = new SPARQLConnection(new SPARQLRepository(managedTripleStoreSparqlEndpoint));

        String issuedQuery = fillInPlaceholders(query);
        long start = System.currentTimeMillis();
        Collection<String> messages = createMessages(query, issuedQuery, managedStoreCon);
        queryProfiler.log(issuedQuery, System.currentTimeMillis() - start);

        return messages;
    }

    private String fillInPlaceholders(NotificationQuery query) {
        String sparqlQuery;
        sparqlQuery = amendChangeSetsTimeConstraint(query);
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

    private Collection<String> createMessages(NotificationQuery query,
                                        String issuedQuery,
                                        RepositoryConnection managedStoreCon) throws OpenRDFException
    {
        Collection<BindingSet> results = changeSetStore.evaluateQuery(issuedQuery);
        query.updateLastIssued();

        Collection<String> messages = new HashSet<String>();
        for (BindingSet bs : results) {
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
            allConditionsFulfilled &= evaluateCondition(condition, bs, managedStoreCon);
        }

        return allConditionsFulfilled;
    }

    private boolean evaluateCondition(Condition condition, BindingSet bs, RepositoryConnection managedStoreCon)
    {
        try {
            BooleanQuery booleanQuery = managedStoreCon.prepareBooleanQuery(QueryLanguage.SPARQL, condition.getAskQuery());
            for (String bindingName : bs.getBindingNames()) {
                booleanQuery.setBinding(bindingName, bs.getBinding(bindingName).getValue());
            }

            return booleanQuery.evaluate() == condition.getExpectedResult();
        }
        catch (Exception e) {
            logger.error("Error evaluating condition. Query: '" +condition.getAskQuery()+ "'", e);
        }
        return false;
    }

}
