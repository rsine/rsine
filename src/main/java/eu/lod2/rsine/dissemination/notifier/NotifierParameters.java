package eu.lod2.rsine.dissemination.notifier;

import eu.lod2.util.ItemNotFoundException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotifierParameters {

    private List<NotifierParameter> parameters = new ArrayList<>();

    public NotifierParameters add(URI id, URI range, boolean required) {
        parameters.add(new NotifierParameter(id, range, required));
        return this;
    }

    public Value getValueById(URI id) {
        for (NotifierParameter notifierParameter : parameters) {
            if (notifierParameter.getId().equals(id)) return notifierParameter.value;
        }
        throw new ItemNotFoundException("No parameter with id '" +id+ "' available");
    }

    public Iterator<NotifierParameter> getParameterIterator() {
        return parameters.iterator();
    }

    public class NotifierParameter {

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

        public boolean isRequired() {
            return required;
        }

        public URI getId() {
            return id;
        }

        public void setValue(Value value) {
            this.value = value;
        }

    }

}
