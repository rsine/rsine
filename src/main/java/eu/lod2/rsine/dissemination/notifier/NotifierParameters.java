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
            if (notifierParameter.getPredicate().equals(id)) return notifierParameter.value;
        }
        throw new ItemNotFoundException("No parameter with predicate '" +id+ "' available");
    }

    public Iterator<NotifierParameter> getParameterIterator() {
        return parameters.iterator();
    }

    public class NotifierParameter {

        private URI predicate, range;
        private Value value;
        private boolean required;

        NotifierParameter(URI predicate) {
            this(predicate, XMLSchema.STRING);
        }

        NotifierParameter(URI predicate, URI range) {
            this(predicate, range, true);
        }

        NotifierParameter(URI predicate, URI range, boolean required) {
            this.predicate = predicate;
            this.range = range;
            this.required = required;
        }

        public boolean isRequired() {
            return required;
        }

        public URI getPredicate() {
            return predicate;
        }

        public void setValue(Value value) {
            this.value = value;
        }

    }

}
