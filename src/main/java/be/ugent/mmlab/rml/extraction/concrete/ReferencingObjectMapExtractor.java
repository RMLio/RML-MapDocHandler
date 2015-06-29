/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : TermMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.RDFTerm.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdJoinCondition;
import be.ugent.mmlab.rml.model.std.StdReferencingObjectMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

public class ReferencingObjectMapExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(ReferencingObjectMapExtractor.class);
    
    protected Set<ReferencingObjectMap> processReferencingObjectMap(
            RMLSesameDataSet rmlMappingGraph, List<Statement> object_statements, Set<GraphMap> savedGraphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap, Resource triplesMapSubject, Resource predicateObject) {
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        try {
           log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Try to extract object map..");
            ReferencingObjectMap refObjectMap = extractReferencingObjectMap(
                    rmlMappingGraph, (Resource) object_statements.get(0).getObject(),
                    savedGraphMaps, triplesMapResources, triplesMap);
            if (refObjectMap != null) {
                //refObjectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                refObjectMaps.add(refObjectMap);
            }
            
        } catch (ClassCastException e) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    +  "A resource was expected in object of objectMap of "
                    + predicateObject.stringValue());
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
            RMLSesameDataSet rmlMappingGraph, Resource object,
            Set<GraphMap> graphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap){
        try {
            URI parentTriplesMap = (URI) TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP, triplesMap);
            Set<JoinCondition> joinConditions = extractJoinConditions(
                    rmlMappingGraph, object, triplesMap);
            
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
            RMLSesameDataSet rmlMappingGraph, Resource object, TriplesMap triplesMap){
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract join conditions..");
        Set<JoinCondition> result = new HashSet<JoinCondition>();
        // Extract predicate-object maps
        URI p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.JOIN_CONDITION);
        List<Statement> statements = rmlMappingGraph.tuplePattern(object, p, null);
        try {
            for (Statement statement : statements) {
                Resource jc = (Resource) statement.getObject();
                String child = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph, jc,
                        R2RMLVocabulary.R2RMLTerm.CHILD, triplesMap);
                String parent = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                        jc, R2RMLVocabulary.R2RMLTerm.PARENT, triplesMap);
                if (parent == null || child == null) {
                    log.error(
                            Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                            +  object.stringValue()
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
        return result;
    }

}
