package eu.lod2.rsine.dissemination.messageformatting;

import org.openrdf.query.BindingSet;

public interface BindingSetFormatter {

    public String toMessage(BindingSet bindingSet);

}
