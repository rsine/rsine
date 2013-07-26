package eu.lod2.rsine.registrationservice;

import java.util.Date;

public class NotificationQuery {

    private String sparqlQuery;
    private Date lastIssued = new Date(0);

    NotificationQuery(String sparqlQuery) {
        this.sparqlQuery = sparqlQuery;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NotificationQuery) {
            return sparqlQuery.equals(((NotificationQuery) obj).sparqlQuery);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return sparqlQuery.hashCode();
    }
}
