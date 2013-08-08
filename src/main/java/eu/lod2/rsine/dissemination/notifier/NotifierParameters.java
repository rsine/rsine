package eu.lod2.rsine.dissemination.notifier;

import eu.lod2.util.ItemNotFoundException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import java.util.ArrayList;
import java.util.List;

public class NotifierParameters {

    private List<NotifierParameter> parameters = new ArrayList<>();

    public NotifierParameters add(URI id, URI range, boolean required) {
        parameters.add(new NotifierParameter(id, range, required));
        return this;
    }

    public Value getValueById(URI id) {
        for (NotifierParameter notifierParameter : parameters) {
            if (notifierParameter.equals(id)) return notifierParameter.value;
        }
        throw new ItemNotFoundException("No parameter with id '" +id+ "' available");
    }

    private class NotifierParameter {

        private URI id, range;
        private Value value;
        private boolean required;

        NotifierParameter(URI id) {
            this(id, XMLSchema.STRING);
        }

        NotifierParameter(URI id, URI range) {
            this(id, range, true);
        }

        NotifierParameter(URI id, URI range, boolean required) {
            this.id = id;
            this.range = range;
            this.required = required;
        }

    }

}
