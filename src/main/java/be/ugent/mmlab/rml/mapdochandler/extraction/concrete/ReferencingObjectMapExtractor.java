package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.condition.extractor.BindingConditionExtractor;
import be.ugent.mmlab.rml.condition.model.BindingCondition;
import be.ugent.mmlab.rml.condition.model.std.BindingReferencingObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.RDFTerm.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdJoinCondition;
import be.ugent.mmlab.rml.model.std.StdReferencingObjectMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
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
            Repository repository, Statement object_statement, 
            Set<GraphMap> savedGraphMaps, Map<Resource, TriplesMap> triplesMapResources, 
            TriplesMap triplesMap, Resource triplesMapSubject, Resource predicateObject) {
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        try {
            log.debug("Extracting Referencing Object Map..");
            ReferencingObjectMap refObjectMap = extractReferencingObjectMap(
                    repository, (Resource) object_statement.getObject(),
                    savedGraphMaps, triplesMapResources, triplesMap);
            if (refObjectMap != null) {
                //refObjectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                refObjectMaps.add(refObjectMap);
            }
        } catch (ClassCastException e) {
            log.error("A resource was expected in object of objectMap of "
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
            Repository repository, Resource object, Set<GraphMap> graphMaps,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap) {
        boolean contains = false;
        TriplesMap parent = null;
        ReferencingObjectMap refObjectMap;

        try {
            URI parentTriplesMap = (URI) TermMapExtractor.extractValueFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP, triplesMap);
            log.debug("Parent Triples Maps were found " + parentTriplesMap);

            Set<JoinCondition> joinConditions = extractJoinConditions(
                    repository, object, triplesMap);

            BindingConditionExtractor bindingConditionsExtractor =
                    new BindingConditionExtractor();
            Set<BindingCondition> bindingConditions =
                    bindingConditionsExtractor.extractBindCondition(
                    repository, object);

            // Extract parent
            for (Resource triplesMapResource : triplesMapResources.keySet()) {
                log.debug("Current Triples Map resource "
                        + triplesMapResource.stringValue());
                if (triplesMapResource.stringValue().equals(
                        parentTriplesMap.stringValue())) {
                    contains = true;
                    parent = triplesMapResources.get(triplesMapResource);
                    log.debug("Parent triples map found : "
                            + triplesMapResource.stringValue());
                    break;
                }
            }
            if (!contains) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + object.stringValue()
                        + " reference to parent triples maps is broken : "
                        + parentTriplesMap.stringValue() + " not found.");
            }

            if (parentTriplesMap == null && !joinConditions.isEmpty()
                    && !bindingConditions.isEmpty()) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + object.stringValue()
                        + " has no parentTriplesMap map defined"
                        + " whereas one or more joinConditions exist"
                        + " : exactly one parentTripleMap is required.");
            }
            if (parentTriplesMap == null && joinConditions.isEmpty()) {
                log.debug("Not a Referencing Object Map.");
                return null;
            }

            if (!bindingConditions.isEmpty()) {
                log.debug("Referencing Object Map "
                        + "with Binding Condition is being extracted..");
                refObjectMap = new BindingReferencingObjectMap(null,
                        parent, joinConditions, bindingConditions);
            } // Link between this reerencing object and its triplesMap parent will be
            // performed at the end f treatment.
            else {
                refObjectMap = new StdReferencingObjectMap(null,
                        parent, joinConditions);
            }

            return refObjectMap;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        }
        return null;
    }
    
    private Set<JoinCondition> extractJoinConditions(
            Repository repository, Resource object, TriplesMap triplesMap) {
        RepositoryResult<Statement> statements ;
        Set<JoinCondition> result = new HashSet<JoinCondition>();
        try {
            log.debug("Extract join conditions..");
            
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
