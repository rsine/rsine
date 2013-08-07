package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;

import java.util.Date;

public class NotificationQuery {

    private BindingSetFormatter bindingSetFormatter;
    private String sparqlQuery;
    private Date lastIssued = new Date(0);

    NotificationQuery(String sparqlQuery, BindingSetFormatter bindingSetFormatter) {
        this.sparqlQuery = sparqlQuery;
        this.bindingSetFormatter = bindingSetFormatter;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NotificationQuery && sparqlQuery.equals(((NotificationQuery) obj).sparqlQuery);
    }

    @Override
    public int hashCode() {
        return sparqlQuery.hashCode();
    }
}
