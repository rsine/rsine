package eu.lod2.rsine.dissemination.messageformatting;

import org.openrdf.query.BindingSet;

public class ToStringBindingSetFormatter implements BindingSetFormatter {

    @Override
    public String toMessage(BindingSet bindingSet) {
        return bindingSet.toString();
    }

}
