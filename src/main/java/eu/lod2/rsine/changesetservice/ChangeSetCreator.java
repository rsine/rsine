package eu.lod2.rsine.changesetservice;

import eu.lod2.util.Namespaces;
import org.openrdf.model.*;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class ChangeSetCreator {

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    public Model assembleChangeset(Statement affectedStatement, Statement secondaryStatement, String changeType) {
        Model model = new TreeModel(new HashSet<Namespace>(Arrays.asList(Namespaces.RSINE_NAMESPACE, Namespaces.CS_NAMESPACE)));

        URI changeSet = valueFactory.createURI(
            Namespaces.RSINE_NAMESPACE.getName(),
            "cs" +System.currentTimeMillis()+"_" +Math.round(Math.random() * 1000));

        model.add(new StatementImpl(changeSet,
            RDF.TYPE,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), "ChangeSet")));
        model.add(new StatementImpl(changeSet,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), "createdDate"),
            valueFactory.createLiteral(new Date())));

        if (changeType.equals(ChangeTripleHandler.CHANGETYPE_REMOVE)) {
            addActionStatement(model, changeSet, affectedStatement, "removal");
        }
        else if (changeType.equals(ChangeTripleHandler.CHANGETYPE_ADD)) {
            addActionStatement(model, changeSet, affectedStatement, "addition");
        }
        else if (changeType.equals(ChangeTripleHandler.CHANGETYPE_UPDATE)) {
            addActionStatement(model, changeSet, affectedStatement, "removal");
            addActionStatement(model, changeSet, secondaryStatement, "addition");
        }

        return model;
    }

    private void addActionStatement(Graph graph, Resource changeSet, Statement statement, String action) {
        graph.add(new StatementImpl(changeSet,
            valueFactory.createURI(Namespaces.CS_NAMESPACE.getName(), action),
            createStatementNode(statement, graph)));
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
