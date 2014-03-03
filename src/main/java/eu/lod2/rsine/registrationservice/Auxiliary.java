package eu.lod2.rsine.registrationservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Auxiliary {

    private Collection<String> queries = new ArrayList<String>();

    public Iterator<String> getQueriesIterator() {
        return queries.iterator();
    }

    public void addQuery(String query) {
        queries.add(query);
    }

}
