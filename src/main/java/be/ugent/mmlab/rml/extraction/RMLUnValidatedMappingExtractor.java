package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.model.std.StdTriplesMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : RMLUnValidatedMappingExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class RMLUnValidatedMappingExtractor extends StdRMLMappingExtractor implements RMLMappingExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(RMLUnValidatedMappingExtractor.class);
       
    /**
     * Construct TriplesMap objects rule. A triples map is represented by a
     * resource that references the following other resources : - It must have
     * exactly one subject map * using the rr:subjectMap property.
     *
     * @param rmlMappingGraph
     * @return
     */
    @Override
    public Map<Resource, TriplesMap> extractTriplesMapResources(
            Repository repo) {
        Map<Resource, TriplesMap> triplesMapResources = new HashMap<Resource, TriplesMap>();
        
        RepositoryResult<Statement> statements = getTriplesMapResources(repo);

        triplesMapResources = putTriplesMapResources(statements, triplesMapResources);
        
        return triplesMapResources;
    }
    
    /**
     *
     * @param rmlMappingGraph
     * @return
     */
    protected RepositoryResult<Statement> getTriplesMapResources(Repository repo){
        RepositoryResult<Statement> statements = null;
        
        try {
            RepositoryConnection connection = repo.getConnection();
            ValueFactory vf = connection.getValueFactory();
            
            URI o = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.TRIPLES_MAP_CLASS);
            URI p = vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            
            statements = connection.getStatements(null,p, (Value) o, true);
            //return statements;
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return statements;
    }
    
    /**
     *
     * @param statements
     * @param triplesMapResources
     * @return
     */
    protected Map<Resource, TriplesMap> putTriplesMapResources(
            RepositoryResult<Statement> statements, Map<Resource, TriplesMap> triplesMapResources) {
        try {
            while (statements.hasNext()) {
                Statement statement = statements.next();
                triplesMapResources.put(statement.getSubject(),
                        new StdTriplesMap(null, null, null, statement.getSubject().stringValue()));
            }
            
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return triplesMapResources;
    }
}
