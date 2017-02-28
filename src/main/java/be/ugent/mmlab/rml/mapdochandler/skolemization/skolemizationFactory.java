package be.ugent.mmlab.rml.mapdochandler.skolemization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

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
    private static final Logger log = 
            LoggerFactory.getLogger(
            skolemizationFactory.class.getSimpleName());
    
    private static ValueFactory vf = SimpleValueFactory.getInstance();

    public static void skolemSubjectSubsitution(
            Resource resource, Resource skolemizedMap, Repository mapDocRepo) {
        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();
            RepositoryResult<Statement> triplesSubject =
                    mapDocRepoCon.getStatements(resource, null, null, true);
            
            while (triplesSubject.hasNext()) {
                Statement statement = triplesSubject.next();

                if (resource instanceof BNode) {
                    mapDocRepoCon.remove(
                            (BNode) statement.getSubject(),
                            (IRI) statement.getPredicate(),
                            (Value) statement.getObject());
                } else {
                    mapDocRepoCon.remove(
                            (Resource) statement.getSubject(),
                            (IRI) statement.getPredicate(),
                            (Value) statement.getObject());
                }

                mapDocRepoCon.add(
                        skolemizedMap, statement.getPredicate(), statement.getObject());
                
                mapDocRepoCon.commit();
                mapDocRepoCon.close();
            }
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
    }
    
    public static void skolemObjectSubsitution(
            Resource resource, Resource skolemizedMap, Repository mapDocRepo) {
        try {
            RepositoryConnection mapDocRepoCon = mapDocRepo.getConnection();
            RepositoryResult<Statement> triplesObject =
                        mapDocRepoCon.getStatements(null, null, resource, true);
            
            while (triplesObject.hasNext()) {
                Statement statement = triplesObject.next();
                mapDocRepoCon.remove(
                        (Resource) statement.getSubject(),
                        (IRI) statement.getPredicate(),
                        (Value) statement.getObject());
                
                mapDocRepoCon.add(statement.getSubject(), statement.getPredicate(), skolemizedMap);
            }
                
            mapDocRepoCon.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
    }
    
    public static void skolemSubstitution(
            Value resource, Resource skolemizedMap, Repository mapDocRepo) {
        try {
            Repository repo = new SailRepository(new MemoryStore());
            repo.initialize();
            
            skolemSubjectSubsitution((Resource) resource, skolemizedMap, repo);
            
            skolemObjectSubsitution((Resource) resource, skolemizedMap, repo);

        } catch (RepositoryException ex) {
            log.error("RepositoryException " +  ex);
        }
    }

    public static Resource skolemizeBlankNode(Value re) {
        if (re != null && re.stringValue().contains(".well-known/genid/")) {
            return (Resource) re;
        }
        if (re == null) {
            re = vf.createBNode();
            Resource ree =
                    vf.createIRI("http://example.com/.well-known/genid/"
                    + re.stringValue().substring(0));
            return ree;
        }
        return vf.createIRI("http://example.com/.well-known/genid/"
                + re.stringValue().substring(0));
    }
}
