package eu.lod2.rsine.dissemination.messageformatting;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openrdf.query.BindingSet;

import java.io.StringWriter;

public class VelocityBindingSetFormatter implements BindingSetFormatter {

    private String velocityTemplate;

    public VelocityBindingSetFormatter(String velocityTemplate) {
        this.velocityTemplate = velocityTemplate;
    }

    @Override
    public String toMessage(BindingSet bindingSet) {
        StringWriter stringWriter = new StringWriter();

        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("bindingSet", bindingSet);
        context.put("msgid", System.currentTimeMillis());
        Velocity.evaluate(context, stringWriter, "xxx", velocityTemplate);

        return stringWriter.toString();
    }

}
