package cz.cvut.kbss.ontodriver.jena.connector;

import cz.cvut.kbss.ontodriver.Statement.StatementOntology;
import cz.cvut.kbss.ontodriver.config.Configuration;
import cz.cvut.kbss.ontodriver.jena.exception.JenaDriverException;
import cz.cvut.kbss.ontodriver.jena.query.AbstractResultSet;
import cz.cvut.kbss.ontodriver.jena.query.AskResultSet;
import cz.cvut.kbss.ontodriver.jena.query.SelectResultSet;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Main storage connector using the {@link cz.cvut.kbss.ontodriver.jena.config.JenaOntoDriverProperties#READ_COMMITTED} connector strategy.
 * <p>
 * Adding statements to it actually adds them to the repository.
 * <p>
 * Note on transactions:
 * <p>
 * Starting a transaction on this connector also starts a write transaction on the underlying dataset. Commit then commits
 * the transaction. Therefore, these transactions should be short. Reading can happen in parallel (as per Jena documentation).
 */
public class SharedStorageConnector extends AbstractStorageConnector {

    SharedStorageConnector(Configuration configuration) {
        super(configuration);
    }

    @Override
    void initialize() {
        this.storage = Storage.create(configuration);
    }

    @Override
    public synchronized void begin() {
        ensureOpen();
        transaction.begin();
        storage.begin(ReadWrite.WRITE);
    }

    @Override
    public synchronized void commit() throws JenaDriverException {
        ensureTransactionalState();
        transaction.commit();
        storage.writeChanges();
        storage.commit();
        transaction.afterCommit();
    }

    void ensureTransactionalState() {
        ensureOpen();
        transaction.verifyActive();
    }

    @Override
    public void rollback() {
        ensureOpen();
        transaction.rollback();
        storage.rollback();
        transaction.afterRollback();
    }

    @Override
    public Collection<Statement> find(Resource subject, Property property, RDFNode value, String context) {
        ensureOpen();
        return Txn.calculateRead(storage.getDataset(), () -> {
            final Model target = context != null ? storage.getNamedGraph(context) : storage.getDefaultGraph();
            final StmtIterator it = target.listStatements(subject, property, value);
            return it.toList();
        });
    }

    @Override
    public boolean contains(Resource subject, Property property, RDFNode value, String context) {
        ensureOpen();
        return Txn.calculateRead(storage.getDataset(), () -> {
            final Model target = context != null ? storage.getNamedGraph(context) : storage.getDefaultGraph();
            return target.contains(subject, property, value);
        });
    }

    @Override
    public List<String> getContexts() {
        ensureOpen();
        final Iterator<String> it = Txn.calculateRead(storage.getDataset(), () -> storage.getDataset().listNames());
        final List<String> contexts = new ArrayList<>();
        it.forEachRemaining(contexts::add);
        return contexts;
    }

    @Override
    public void add(List<Statement> statements, String context) {
        ensureTransactionalState();
        storage.add(statements, context);
    }

    @Override
    public void remove(List<Statement> statements, String context) {
        ensureTransactionalState();
        storage.remove(statements, context);
    }

    @Override
    public void remove(Resource subject, Property property, RDFNode object, String context) {
        ensureTransactionalState();
        if (context != null) {
            storage.remove(storage.getNamedGraph(context).listStatements(subject, property, object), context);
        } else {
            storage.remove(storage.getDefaultGraph().listStatements(subject, property, object), null);
        }
    }

    @Override
    public AbstractResultSet executeSelectQuery(Query query, StatementOntology target) throws JenaDriverException {
        ensureOpen();
        try {
            QueryExecution exec = QueryExecutionFactory.create(query, storage.getDataset());
            final org.apache.jena.query.ResultSet rs = exec.execSelect();
            return new SelectResultSet(exec, rs);
        } catch (RuntimeException e) {
            throw new JenaDriverException("Execution of query " + query + " failed.", e);
        }
    }

    @Override
    public AbstractResultSet executeAskQuery(Query query, StatementOntology target) throws JenaDriverException {
        ensureOpen();
        try (final QueryExecution exec = QueryExecutionFactory.create(query, storage.getDataset())) {
            return new AskResultSet(exec.execAsk());
        } catch (RuntimeException e) {
            throw new JenaDriverException("Execution of query " + query + " failed.", e);
        }
    }

    @Override
    public void executeUpdate(String query, StatementOntology target) throws JenaDriverException {
        ensureOpen();
        try {
            UpdateAction.parseExecute(query, storage.getDataset());
        } catch (RuntimeException e) {
            throw new JenaDriverException("Execution of update " + query + " failed.", e);
        }
    }

    @Override
    public synchronized void close() {
        if (!isOpen()) {
            return;
        }
        storage.close();
        super.close();
    }
}
