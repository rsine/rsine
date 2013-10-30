package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;

import java.util.Date;

public class NotificationQuery {

    private BindingSetFormatter bindingSetFormatter;
    private String sparqlQuery;
    private Date lastIssued = new Date(0);
    private Subscription subscription;

    NotificationQuery(String sparqlQuery, BindingSetFormatter bindingSetFormatter, Subscription subscription) {
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
