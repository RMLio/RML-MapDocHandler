package be.ugent.mmlab.rml.skolemization;

import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import info.aduna.iteration.Iterations;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.Dataset;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : skolemizationFactory
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class skolemizationFactory {
    // Log
    private static final Logger log = LoggerFactory.getLogger(skolemizationFactory.class);
    
    private static ValueFactory vf = new ValueFactoryImpl();

    public static void skolemSubstitution(
            Value resource, Resource skolemizedMap, RMLSesameDataSet rmlMappingGraph) {
        
        List<Statement> triplesSubject = rmlMappingGraph.tuplePattern(
                (Resource) resource, null, null);

        for (Statement tri : triplesSubject) {
            int size1 = rmlMappingGraph.getSize();
            if (resource instanceof BNode){
                rmlMappingGraph.remove(
                    (BNode) tri.getSubject(),
                    (URI) tri.getPredicate(),
                    (Value) tri.getObject());
            }
            else
                rmlMappingGraph.remove(
                    (Resource) tri.getSubject(),
                    (URI) tri.getPredicate(),
                    (Value) tri.getObject());

            rmlMappingGraph.add(skolemizedMap, tri.getPredicate(), tri.getObject());

            int size2 = rmlMappingGraph.getSize();
            /*if (size1 != size2) {
                log.error("didn't delete again..");
            }*/
        }
        List<Statement> triplesObject = rmlMappingGraph.tuplePattern(
                null, null, resource);
        for (Statement tri : triplesObject) {
            rmlMappingGraph.remove(
                    (Resource) tri.getSubject(),
                    (URI) tri.getPredicate(),
                    (Value) tri.getObject());
            //rmlMappingGraph.remove(tri);
            rmlMappingGraph.add(tri.getSubject(), tri.getPredicate(), skolemizedMap);
        }
    }
    
    public static void skolemSubstitution(
            Value resource, Resource skolemizedMap, Dataset dataset) {
        try {
            Repository repo = new SailRepository(new MemoryStore());
            repo.initialize();
            
            RepositoryConnection con = repo.getConnection();
            
            
            
            skolemSubjectSubsitution(repo);
            
            RepositoryResult<Statement> statements = 
                    con.getStatements(null, null, null, true);
            
            Model statementsModel = Iterations.addAll(statements, new LinkedHashModel());
            
            

            
            
            List<Statement> triplesSubject = dataset.tuplePattern(
                    (Resource) resource, null, null);

            for (Statement tri : triplesSubject) {
                int size1 = rmlMappingGraph.getSize();
                if (resource instanceof BNode){
                    rmlMappingGraph.remove(
                        (BNode) tri.getSubject(),
                        (URI) tri.getPredicate(),
                        (Value) tri.getObject());
                }
                else
                    rmlMappingGraph.remove(
                        (Resource) tri.getSubject(),
                        (URI) tri.getPredicate(),
                        (Value) tri.getObject());

                rmlMappingGraph.add(skolemizedMap, tri.getPredicate(), tri.getObject());

                int size2 = rmlMappingGraph.getSize();
                /*if (size1 != size2) {
                    log.error("didn't delete again..");
                }*/
            }
            List<Statement> triplesObject = rmlMappingGraph.tuplePattern(
                    null, null, resource);
            for (Statement tri : triplesObject) {
                rmlMappingGraph.remove(
                        (Resource) tri.getSubject(),
                        (URI) tri.getPredicate(),
                        (Value) tri.getObject());
                //rmlMappingGraph.remove(tri);
                rmlMappingGraph.add(tri.getSubject(), tri.getPredicate(), skolemizedMap);
            }
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(skolemizationFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Resource skolemizeBlankNode(Value re) {
        if (re != null && re.stringValue().contains(".well-known/genid/")) {
            return (Resource) re;
        }
        if (re == null) {
            re = vf.createBNode();
            Resource ree =
                    vf.createURI("http://example.com/.well-known/genid/" + re.stringValue().substring(0));
            return ree;
        }
        return vf.createURI("http://example.com/.well-known/genid/" + re.stringValue().substring(0));
    }
}
