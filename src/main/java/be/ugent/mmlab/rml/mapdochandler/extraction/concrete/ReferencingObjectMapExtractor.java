package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.condition.extractor.AbstractConditionExtractor;
import be.ugent.mmlab.rml.condition.model.Condition;
import be.ugent.mmlab.rml.condition.model.std.StdJoinConditionMetric;
import be.ugent.mmlab.rml.mapdochandler.extraction.condition.ConditionPredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.RDFTerm.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.ConditionReferencingObjectMap;
import be.ugent.mmlab.rml.model.std.StdJoinCondition;
import be.ugent.mmlab.rml.model.std.StdReferencingObjectMap;
import be.ugent.mmlab.rml.vocabularies.CRMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
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
    protected Set<Condition> conditions = null;
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(
            ReferencingObjectMapExtractor.class.getSimpleName());
    
    public Set<ReferencingObjectMap> processReferencingObjectMap(
            Repository repository, Statement object_statement, 
            GraphMap savedGraphMap, Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap, Resource triplesMapSubject, Resource predicateObject) {
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        try {
            log.debug("Extracting Referencing Object Map..");
            ReferencingObjectMap refObjectMap = extractReferencingObjectMap(
                    repository, (Resource) object_statement.getObject(),
                    savedGraphMap, triplesMapResources, triplesMap,
                    triplesMapSubject, predicateObject);
            if (refObjectMap != null) {
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
     * @param object
     * @param graphMap
     * @param triplesMapResources
     * @param triplesMap
     * @return
     */
    protected ReferencingObjectMap extractReferencingObjectMap(
            Repository repository, Resource object, GraphMap graphMap,
            Map<Resource, TriplesMap> triplesMapResources, TriplesMap triplesMap, 
            Resource triplesMapSubject, Resource predicateObject) {
        TriplesMap parent = null;
        ReferencingObjectMap refObjectMap = null;

        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            
            URI parentTriplesMap = (URI) TermExtractor.extractValueFromTermMap(
                    repository, object, R2RMLVocabulary.R2RMLTerm.PARENT_TRIPLES_MAP, triplesMap);
            log.debug("Parent Triples Maps were found " + parentTriplesMap);
            
            if(parentTriplesMap == null)
                return null;

            Set<JoinCondition> joinConditions = extractJoinConditions(
                    repository, object, triplesMap);

            //TODO: Transfer the following condition implementation to a separate function
            if (connection.hasStatement(
                    object, vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BOOLEAN_CONDITION), null, true) || 
                connection.hasStatement(
                    object, vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.BINDING_CONDITION), null, true)) {
                //TODO: Clean up the conditions mess - Properly rename classess
                //ConditionPredicateObjectMapExtractor is for equalCondition
                //AbstractConditionExtractor is for bindingCondition
                ConditionPredicateObjectMapExtractor preObjMapExtractor =
                        new ConditionPredicateObjectMapExtractor();
                conditions = preObjMapExtractor.extractConditions(
                        repository, object);
                AbstractConditionExtractor conditionsExtractor =
                new AbstractConditionExtractor();
                Set<Condition> conditions2 = conditionsExtractor.extractConditions(repository, object);
                conditions.addAll(conditions2);
                if(conditions != null) {
                    log.debug("Conditional Referencing Object Map extracted.");
                    log.debug(conditions.size() + " conditions were found");
                }
            }

            if (graphMap != null) {
                GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
                graphMap = graphMapExtractor.extractGraphMap(
                        repository, (Resource) graphMap.getConstantValue(), triplesMap);
            }
            if (graphMap != null)
                log.debug("Found Graph Map for this Referencing Object Map " + graphMap.getConstantValue());

            //Extracting fallbacks
            Set<ReferencingObjectMap> fallbackReferencingObjectMaps =
                    extractFallbackObjectMap(repository, object, graphMap,
                    triplesMapResources, triplesMap, triplesMapSubject, predicateObject);

            if (fallbackReferencingObjectMaps != null)
                log.debug("Found " + fallbackReferencingObjectMaps.size()
                        + " Fallback Maps.");

            // Extract parent
            if(triplesMapResources != null)
            for (Resource triplesMapResource : triplesMapResources.keySet()) {
                log.debug("Current Triples Map resource "
                        + triplesMapResource.stringValue());
                if (parentTriplesMap != null && 
                        triplesMapResource.stringValue().equals(
                        parentTriplesMap.stringValue())) {
                    parent = triplesMapResources.get(triplesMapResource);
                    break;
                }
            }
            else {
                //TODO: Check if this is needed in the end or not
                //Set main PreObjMap as FalMap's own Triples Map
                RepositoryResult<Statement> triplesMapStatements = connection.getStatements(
                        null, null, object, true);
                triplesMapStatements = connection.getStatements(
                        null, vf.createURI(CRMLVocabulary.CRML_NAMESPACE + CRMLVocabulary.cRMLTerm.FALLBACK),
                        triplesMapStatements.next().getSubject(), true);

                triplesMapStatements = connection.getStatements(
                    null, vf.createURI(R2RMLVocabulary.R2RML_NAMESPACE + R2RMLVocabulary.R2RMLTerm.PREDICATE_OBJECT_MAP),
                        triplesMapStatements.next().getSubject(), true);
                Resource tm = triplesMapStatements.next().getSubject();
                //TODO: Properly handle duplicate code
                for (Resource triplesMapResource : triplesMapResources.keySet()) {
                    if (parentTriplesMap != null &&
                            triplesMapResource.stringValue().equals(tm.stringValue())) {
                        parent = triplesMapResources.get(triplesMapResource);
                        break;
                    }
                }
            }

            connection.close();
            
            //TODO: Move error checking elsewhere
            if (parentTriplesMap == null && !joinConditions.isEmpty()
                    && !conditions.isEmpty()) {
                log.error(object.stringValue()
                        + " has no parentTriplesMap map defined"
                        + " whereas one or more joinConditions exist"
                        + " : exactly one parentTripleMap is required.");
            }
            if (parentTriplesMap == null && joinConditions.isEmpty()
                    && conditions.isEmpty()) {
                log.debug("Not a Referencing Object Map.");
                return null;
            }
            if (conditions != null) {
                if (!conditions.isEmpty() && conditions.size() > 0) {
                    log.debug("Referencing Object Map "
                            + "with Conditions is being extracted..");
                    //if (conditions != null && conditions.size() > 0) {
                    refObjectMap = new ConditionReferencingObjectMap(null,
                            parent, joinConditions, conditions, fallbackReferencingObjectMaps, graphMap);
                }
            } // Link between this reerencing object and its triplesMap parent will be
            // performed at the end f treatment.
            else {
                log.debug("Referencing Object Map "
                        + "without Conditions is being extracted..");
                refObjectMap = new StdReferencingObjectMap(null,
                        parent, joinConditions, fallbackReferencingObjectMaps, graphMap);
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
                    String child = TermExtractor.extractLiteralFromTermMap(repository, jc,
                            R2RMLVocabulary.R2RMLTerm.CHILD, triplesMap);
                    String parent = TermExtractor.extractLiteralFromTermMap(repository,
                            jc, R2RMLVocabulary.R2RMLTerm.PARENT, triplesMap);
                    Value metric = TermExtractor.extractValueFromTermMap(repository, jc, 
                            new URIImpl(CRMLVocabulary.CRML_NAMESPACE + 
                            CRMLVocabulary.cRMLTerm.METRIC) , triplesMap);
                    log.debug("Metric " + metric + " was extracted.");
                    if (parent == null || child == null) {
                        log.error(object.stringValue()
                                + " must have exactly two properties child and parent. ");
                    }
                    if (metric != null) {
                        try {
                            result.add(new StdJoinConditionMetric(
                                    child, parent, (Resource) metric));
                            log.debug("Join Condition with Metric was extracted.");
                        } catch (Exception ex) {
                            log.error("Exception: " + ex);
                        }
                    }
                    else
                        try {
                            result.add(new StdJoinCondition(child, parent));
                        } catch (Exception ex) {
                            log.error("Exception: " + ex);
                        }
                }
            } catch (ClassCastException e) {
                log.error("A resource was expected in object of predicateMap of "
                        + object.stringValue());
            }
            log.debug("Extract join conditions done.");
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }
    
    private Set<ReferencingObjectMap> extractFallbackObjectMap(
            Repository repository, Resource object, GraphMap graphMap,
            Map<Resource,TriplesMap> triplesMapResources, TriplesMap triplesMap,
            Resource triplesMapSubject, Resource predicateObject) {
        Set<ReferencingObjectMap> fallbackReferencingObjectMaps = 
                new HashSet<ReferencingObjectMap>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            if (connection.hasStatement(object,
                    vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                    + CRMLVocabulary.cRMLTerm.FALLBACK),
                    null, true)) {
                log.debug("Referencing Object Map with fallback ObjMap");
                URI p = vf.createURI(CRMLVocabulary.CRML_NAMESPACE
                        + CRMLVocabulary.cRMLTerm.FALLBACK);
                RepositoryResult<Statement> fallbackStatements =
                        connection.getStatements(object, p, null, true);
                
                while (fallbackStatements.hasNext()) {
                    Statement fallbackStatement = fallbackStatements.next();

                    //Extract Referencing Object Maps
                    log.debug("Extracting Referencing Object Maps..");
                    ReferencingObjectMapExtractor refObjMapExtractor =
                            new ReferencingObjectMapExtractor();
                    fallbackReferencingObjectMaps =
                            refObjMapExtractor.processReferencingObjectMap(
                            repository, fallbackStatement, graphMap,
                            triplesMapResources, triplesMap,
                            triplesMapSubject, predicateObject);
                    log.debug("Extract Fallback Object Map done.");

                }
            }
        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        } finally {
            return fallbackReferencingObjectMaps;
        }
    }
}
