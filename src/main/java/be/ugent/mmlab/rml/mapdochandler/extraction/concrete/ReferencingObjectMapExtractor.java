package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.RDFTerm.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdJoinCondition;
import be.ugent.mmlab.rml.model.std.StdReferencingObjectMap;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : ReferencingObjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class ReferencingObjectMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(ReferencingObjectMapExtractor.class);
    
    public Set<ReferencingObjectMap> processReferencingObjectMap(
            Repository repository, RepositoryResult<Statement> object_statements, Set<GraphMap> savedGraphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap, Resource triplesMapSubject, Resource predicateObject) {
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        try {
            if (object_statements.hasNext()) {
                Statement object_statement = object_statements.next();
                log.debug("Trying to extract Referencing Object Map..");
                ReferencingObjectMap refObjectMap = extractReferencingObjectMap(
                        repository, (Resource) object_statement.getObject(),
                        savedGraphMaps, triplesMapResources, triplesMap);
                if (refObjectMap != null) {
                    //refObjectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                    refObjectMaps.add(refObjectMap);
                }
            }
        } catch (ClassCastException e) {
            log.error("A resource was expected in object of objectMap of "
                    + predicateObject.stringValue());
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        } 
        return refObjectMaps;
    }
    
    /**
     *
     * @param rmlMappingGraph
     * @param object
     * @param graphMaps
     * @param triplesMapResources
     * @param triplesMap
     * @return
     */
    protected ReferencingObjectMap extractReferencingObjectMap(
            Repository repository, Resource object,
            Set<GraphMap> graphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap){
        try {
            URI parentTriplesMap = (URI) TermMapExtractor.extractValueFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP, triplesMap);
            Set<JoinCondition> joinConditions = extractJoinConditions(
                    repository, object, triplesMap);
            
            if (parentTriplesMap == null && !joinConditions.isEmpty()) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + object.stringValue()
                        + " has no parentTriplesMap map defined whereas one or more joinConditions exist"
                        + " : exactly one parentTripleMap is required.");
            }
            if (parentTriplesMap == null && joinConditions.isEmpty()) {
                return null;
            }
            // Extract parent
            boolean contains = false;
            TriplesMap parent = null;
            for (Resource triplesMapResource : triplesMapResources.keySet()) {
                if (triplesMapResource.stringValue().equals(
                        parentTriplesMap.stringValue())) {
                    contains = true;
                    parent = triplesMapResources.get(triplesMapResource);
                    log.debug(
                            Thread.currentThread().getStackTrace()[1].getMethodName() + ": " 
                            + "Parent triples map found : "
                            + triplesMapResource.stringValue());
                    break;
                }
            }
            if (!contains) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        +  object.stringValue()
                        + " reference to parent triples maps is broken : "
                        + parentTriplesMap.stringValue() + " not found.");
            }
            // Link between this reerencing object and its triplesMap parent will be
            // performed
            // at the end f treatment.
            ReferencingObjectMap refObjectMap = new StdReferencingObjectMap(null,
                    parent, joinConditions);
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract referencing object map done.");
            return refObjectMap;
        } catch (Exception ex) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
        }
        return null;
    }
    
    private Set<JoinCondition> extractJoinConditions(
            Repository repository, Resource object, TriplesMap triplesMap) {
        RepositoryResult<Statement> statements ;
        Set<JoinCondition> result = new HashSet<JoinCondition>();
        try {
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract join conditions..");
            
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            // Extract predicate-object maps
            URI p = vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE
                    + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION);
            statements = connection.getStatements(object, p, null, true);

            try {
                while(statements.hasNext()) {
                    Resource jc = (Resource) statements.next().getObject();
                    String child = TermMapExtractor.extractLiteralFromTermMap(repository, jc,
                            R2RMLVocabulary.R2RMLTerm.CHILD, triplesMap);
                    String parent = TermMapExtractor.extractLiteralFromTermMap(repository,
                            jc, R2RMLVocabulary.R2RMLTerm.PARENT, triplesMap);
                    if (parent == null || child == null) {
                        log.error(
                                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                                + object.stringValue()
                                + " must have exactly two properties child and parent. ");
                    }
                    try {
                        result.add(new StdJoinCondition(child, parent));
                    } catch (Exception ex) {
                        log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
                    }
                }
            } catch (ClassCastException e) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "A resource was expected in object of predicateMap of "
                        + object.stringValue());
            }
            log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + " Extract join conditions done.");
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }
}
