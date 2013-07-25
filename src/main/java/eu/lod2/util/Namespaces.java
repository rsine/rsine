package eu.lod2.util;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;

public class Namespaces {

    public final static Namespace RSINE_NAMESPACE = new NamespaceImpl("rsine", "http://lod2.eu/rsine/");
    public final static Namespace CS_NAMESPACE = new NamespaceImpl("cs", "http://purl.org/vocab/changeset/schema#");
    public final static Namespace SKOS_NAMESPACE = new NamespaceImpl("skos", "http://www.w3.org/2004/02/skos/core#");

    public final static String CHANGESET_CONTEXT = Namespaces.RSINE_NAMESPACE.getName() + "changesets";
    public final static String VOCAB_CONTEXT = Namespaces.RSINE_NAMESPACE.getName() + "vocab";

    public final static String SKOS_PREFIX = "PREFIX skos:<" +SKOS_NAMESPACE.getName()+ ">";
}
