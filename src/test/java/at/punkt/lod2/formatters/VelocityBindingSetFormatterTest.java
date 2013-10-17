package at.punkt.lod2.formatters;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.dissemination.messageformatting.VelocityBindingSetFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;

public class VelocityBindingSetFormatterTest {

    private VelocityBindingSetFormatter velocityBindingSetFormatter;
    private RepositoryConnection repCon;

    @Before
    public void setUp() throws RDFParseException, IOException, RDFHandlerException, RepositoryException {
        velocityBindingSetFormatter = new VelocityBindingSetFormatter(new LiteralImpl(
            "The preferred label of the concept '$bindingSet.getValue('to_concept')' " +
            "has been changed to $bindingSet.getValue('added_label')", "en"));

        Model vocab = Helper.createModelFromResourceFile("/reegle.rdf", RDFFormat.RDFXML);
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        repCon = repository.getConnection();
        repCon.add(vocab);
    }

    @After
    public void tearDown() throws RepositoryException {
        repCon.close();
    }

    @Test
    public void variableSubstitution() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        TupleQueryResult result =  repCon.prepareTupleQuery(QueryLanguage.SPARQL, generateQuery()).evaluate();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();

            String message = velocityBindingSetFormatter.toMessage(bindingSet);
            Assert.assertTrue(message.contains("http://reegle.info/glossary/676"));
            Assert.assertTrue(message.contains("\"Klimawandel Parameter\"@de"));
        }
    }

    private String generateQuery() {
        return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
               "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> "+
               "SELECT ?added_label ?to_concept WHERE {"+
                   "?to_concept skos:prefLabel ?added_label ."+
                   "FILTER (?to_concept=<http://reegle.info/glossary/676> && LANG(?added_label)='de')"+
               "}";
    }

}
