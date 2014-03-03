package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class NotificationQuery {

    private BindingSetFormatter bindingSetFormatter;
    private String sparqlQuery;
    private Date lastIssued = new Date(0);
    private Subscription subscription;
    private Auxiliary auxiliary;
    private Collection<Condition> conditions = new ArrayList<Condition>();

    public NotificationQuery(String sparqlQuery,
                      BindingSetFormatter bindingSetFormatter,
                      Subscription subscription)
    {
        this.sparqlQuery = sparqlQuery;
        this.bindingSetFormatter = bindingSetFormatter;
        this.subscription = subscription;
    }

    public void updateLastIssued() {
        lastIssued = new Date();
    }

    public Date getLastIssued() {
        return lastIssued;
    }

    public String getSparqlQuery() {
        return sparqlQuery;
    }

    public void setConditions(Collection<Condition> conditions) {
        this.conditions = conditions;
    }
    public Iterator<Condition> getConditions() {
        return conditions.iterator();
    }

    public void setAuxiliary(Auxiliary auxiliary) {
        this.auxiliary = auxiliary;
    }
    public Iterator<String> getAuxiliaryQueries() {
        return auxiliary.getQueriesIterator();
    }

    public BindingSetFormatter getBindingSetFormatter() {
        return bindingSetFormatter;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NotificationQuery &&
                sparqlQuery.equals(((NotificationQuery) obj).sparqlQuery) &&
                conditions.equals(((NotificationQuery) obj).conditions);
    }

    @Override
    public int hashCode() {
        return sparqlQuery.hashCode() + conditions.hashCode();
    }

    @Override
    public String toString() {
        return "query: '" +sparqlQuery+ "', conditions: '" +conditions.toString()+ "'";
    }
}
