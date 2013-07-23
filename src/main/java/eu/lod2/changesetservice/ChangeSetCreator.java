package eu.lod2.changesetservice;

import eu.lod2.util.ItemNotFoundException;
import org.openrdf.model.*;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import java.util.Arrays;
import java.util.HashSet;

public class ChangeSetCreator {

    public final static Namespace RSINE_NAMESPACE = new NamespaceImpl("rsine", "http://lod2.eu/rsine#");
    public final static Namespace CS_NAMESPACE = new NamespaceImpl("cs", "http://purl.org/vocab/changeset/schema#");

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    public Graph assembleChangeset(Statement affectedStatement, String changeType) {
        Graph graph = new TreeModel(new HashSet<Namespace>(Arrays.asList(RSINE_NAMESPACE, CS_NAMESPACE)));

        URI changeSet = valueFactory.createURI(
            RSINE_NAMESPACE.getName(),
            "cs" +System.currentTimeMillis()+"_" +Math.round(Math.random() * 1000));

        graph.add(new StatementImpl(changeSet,
            RDF.TYPE,
            valueFactory.createURI(CS_NAMESPACE.getName(), "ChangeSet")));
        graph.add(new StatementImpl(changeSet,
            createChangeTypeUri(changeType),
            createStatementNode(affectedStatement, graph)));

        return graph;
    }

    private URI createChangeTypeUri(String changeType) {
        if (changeType.equals(ChangeTripleHandler.CHANGETYPE_REMOVE)) {
            return valueFactory.createURI(CS_NAMESPACE.getName(), "removal");
        }
        else if (changeType.equals(ChangeTripleHandler.CHANGETYPE_ADD)) {
            return valueFactory.createURI(CS_NAMESPACE.getName(), "addition");
        }
        throw new ItemNotFoundException("Invalid changetype");
    }

    private BNode createStatementNode(Statement affectedStatement, Graph graph) {
        BNode statementNode = valueFactory.createBNode();

        graph.add(new StatementImpl(statementNode, RDF.TYPE, RDF.STATEMENT));
        graph.add(new StatementImpl(statementNode, RDF.SUBJECT, affectedStatement.getSubject()));
        graph.add(new StatementImpl(statementNode, RDF.PREDICATE, affectedStatement.getPredicate()));
        graph.add(new StatementImpl(statementNode, RDF.OBJECT, affectedStatement.getObject()));

        return statementNode;
    }

}
