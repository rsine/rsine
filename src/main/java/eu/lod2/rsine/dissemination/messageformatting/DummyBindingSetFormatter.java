package eu.lod2.rsine.dissemination.messageformatting;

import org.openrdf.query.BindingSet;

public class DummyBindingSetFormatter implements BindingSetFormatter {

    @Override
    public String toMessage(BindingSet bindingSet) {
        return "No binding set formatting defined";
    }

}
