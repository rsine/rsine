package eu.lod2.util;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.RDF;

public class Namespaces {

    public final static Namespace RSINE_NAMESPACE = new NamespaceImpl("rsine", "http://lod2.eu/rsine/");
    public final static Namespace CS_NAMESPACE = new NamespaceImpl("cs", "http://purl.org/vocab/changeset/schema#");
    public final static Namespace SKOS_NAMESPACE = new NamespaceImpl("skos", "http://www.w3.org/2004/02/skos/core#");
    public final static Namespace DCTERMS_NAMESPACE = new NamespaceImpl("dcterms", "http://purl.org/dc/terms/");

    public final static String SKOS_PREFIX = "PREFIX skos:<" +SKOS_NAMESPACE.getName()+ ">";
    public final static String CS_PREFIX = "PREFIX cs:<" +CS_NAMESPACE.getName()+ ">";
    public final static String RDF_PREFIX = "PREFIX rdf:<" + RDF.NAMESPACE+ ">";
    public final static String DCTERMS_PREFIX = "PREFIX dcterms:<" + DCTERMS_NAMESPACE.getName()+ ">";
}
