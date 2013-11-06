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
    private Collection<Condition> conditions = new ArrayList<Condition>();

    NotificationQuery(String sparqlQuery,
                      BindingSetFormatter bindingSetFormatter,
                      Subscription subscription)
    {
        this.sparqlQuery = sparqlQuery;
        this.bindingSetFormatter = bindingSetFormatter;
        this.subscription = subscription;
    }

    NotificationQuery(String sparqlQuery,
                      BindingSetFormatter bindingSetFormatter,
                      Collection<Condition> conditions,
                      Subscription subscription)
    {
        this(sparqlQuery, bindingSetFormatter, subscription);
        this.conditions = conditions;
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

    public Iterator<Condition> getConditions() {
        return conditions.iterator();
    }

    public BindingSetFormatter getBindingSetFormatter() {
        return bindingSetFormatter;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NotificationQuery && sparqlQuery.equals(((NotificationQuery) obj).sparqlQuery);
    }

    @Override
    public int hashCode() {
        return sparqlQuery.hashCode();
    }
}
