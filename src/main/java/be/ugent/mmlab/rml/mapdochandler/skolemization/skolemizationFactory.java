package be.ugent.mmlab.rml.mapdochandler.skolemization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;
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
    private static final Logger log = 
            LoggerFactory.getLogger(
            skolemizationFactory.class.getSimpleName());
    
    private static ValueFactory vf = new ValueFactoryImpl();

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
                            (URI) statement.getPredicate(),
                            (Value) statement.getObject());
                } else {
                    mapDocRepoCon.remove(
                            (Resource) statement.getSubject(),
                            (URI) statement.getPredicate(),
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
                        (URI) statement.getPredicate(),
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
                    vf.createURI("http://example.com/.well-known/genid/" 
                    + re.stringValue().substring(0));
            return ree;
        }
        return vf.createURI("http://example.com/.well-known/genid/" 
                + re.stringValue().substring(0));
    }
}
