package eu.lod2.rsine.dissemination.messageformatting;

import eu.lod2.util.Namespaces;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

public class FormatterFactory {

    public BindingSetFormatter createFormatter(Model subscription, Resource formatter) {
        URI formatterType = subscription.filter(
            formatter,
            RDF.TYPE,
            null).objectURI();

        if (formatterType != null) {
            if (formatterType.equals(new URIImpl(Namespaces.RSINE_NAMESPACE.getName() + "vtlFormatter"))) {
                return createVelocityBindingSetFormatter(subscription, formatter);
            }
        }

        return new ToStringBindingSetFormatter();
    }

    private VelocityBindingSetFormatter createVelocityBindingSetFormatter(Model subscription, Resource formatter) {
        Literal message = subscription.filter(
            formatter,
            new URIImpl(Namespaces.RSINE_NAMESPACE + "message"),
            null).objectLiteral();

        return new VelocityBindingSetFormatter(message);
    }

}
