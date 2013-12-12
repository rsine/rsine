package eu.lod2.rsine.dissemination.messageformatting;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;

import java.io.StringWriter;

public class VelocityBindingSetFormatter implements BindingSetFormatter {

    private Literal velocityTemplate;

    public VelocityBindingSetFormatter(Literal velocityTemplate) {
        this.velocityTemplate = velocityTemplate;
    }

    @Override
    public String toMessage(BindingSet bindingSet) {
        StringWriter stringWriter = new StringWriter();

        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("bindingSet", bindingSet);
        context.put("msgid", System.currentTimeMillis());
        Velocity.evaluate(context, stringWriter, "xxx", velocityTemplate.getLabel());

        return stringWriter.toString();
    }

}
