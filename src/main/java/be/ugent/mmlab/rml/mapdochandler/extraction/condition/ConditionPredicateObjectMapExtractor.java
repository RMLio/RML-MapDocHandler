
package be.ugent.mmlab.rml.mapdochandler.extraction.condition;

import be.ugent.mmlab.rml.condition.extractor.BooleanConditionExtractor;
import be.ugent.mmlab.rml.condition.extractor.StdConditionExtractor;
import be.ugent.mmlab.rml.condition.model.Condition;
import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.PredicateObjectMapExtractor;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdConditionPredicateObjectMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;

/**
 * RML Processor
 *
 * @author andimou
 */
public class ConditionPredicateObjectMapExtractor extends PredicateObjectMapExtractor {
    
    //Log
    private static final Logger log = 
            LogManager.getLogger(
            ConditionPredicateObjectMapExtractor.class.getSimpleName());
    
    @Override
    public PredicateObjectMap extractPredicateObjectMap(
            Repository repository,
            Resource triplesMapSubject,
            Resource predicateObject,
            GraphMap graphMap,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap){
        PredicateObjectMap predicateObjectMap = null;
        log.debug("Extracting Predicate Object Map with conditions....");
        PredicateObjectMap pom =
                super.extractPredicateObjectMap(
                repository, triplesMapSubject, predicateObject,
                graphMap, triplesMapResources, triplesMap);
        if (pom != null) {
            Set<Condition> conditions =
                    extractConditions(repository, predicateObject);
            log.debug("Found " + conditions.size() + " conditions.");
            Set<PredicateObjectMap> fallbackPOMs =
                    extractFallback(repository, predicateObject,triplesMapResources,triplesMap);
            log.debug("Found " + fallbackPOMs.size() + " Fallback POMs.");

            if (conditions.size() > 0 || fallbackPOMs.size() > 0) {
                log.debug("Conditional Predicate Object Map was extracted!");
                //predicateObjectMap = (StdConditionPredicateObjectMap) pom;
                predicateObjectMap = new StdConditionPredicateObjectMap(
                        pom.getPredicateMaps(), pom.getObjectMaps(),
                        pom.getReferencingObjectMaps(), conditions, fallbackPOMs);
                return predicateObjectMap;
            } else {
                log.error("Conditions were not properly extracted");
            }
        }
        return predicateObjectMap;
    }
    
    public static Set<Condition> extractConditions(
            Repository repository, Resource object) {
        Set<Condition> conditions = new HashSet<Condition>();

        try {
            log.debug("Extracting Conditions...");
            try {
                //Extract Boolean Conditions
                StdConditionExtractor conditionsExtractor =
                        new BooleanConditionExtractor();
                Condition booleanCondition = null;

                //Extract all condition resources
                Set<Resource> conditionResources =
                        conditionsExtractor.extractConditionResources(
                        repository, object);

                //Parse the condition resources
                for (Resource conditionResource : conditionResources) {
                    booleanCondition =
                            conditionsExtractor.extractBooleanCondition(
                            repository, conditionResource);

                    log.debug("Extracting Fallback Maps...");
                    List<Value> fallbackTerms = conditionsExtractor.extractFallback(
                            repository, conditionResource);
                    log.debug("Found " + fallbackTerms.size() + " fallback Maps");

                    //Extract the fallbackTerms
                    for (Value fallbackTerm : fallbackTerms) {
                        PredicateObjectMapExtractor preObjMapExtractor =
                                new PredicateObjectMapExtractor();
                        log.debug("Extracting nested POMs...");
                        PredicateObjectMap predicateObjectMap =
                                preObjMapExtractor.extractPredicateObjectMap(
                                repository, conditionResource,
                                (Resource) fallbackTerm, null, null, null);
                        log.debug("Setting the fallback POM");
                        //extractFallback(fallbackTerm);
                        booleanCondition.setFallback(predicateObjectMap);
                    }
                }

                //Add Boolean Conditions to the total
                if (booleanCondition != null) {
                    conditions.add(booleanCondition);
                    log.debug("Boolean Conditions were extracted");
                }
            } catch (Exception ex) {
                log.error("Exception " + ex + " because of Boolean Conditions");
            }

        } catch (ClassCastException e) {
            log.error("Class cast exception " + e
                    + " A resource was expected in object of predicateMap of "
                    + object.stringValue());
        }
        log.debug("Extracting conditions completed.");

        return conditions;
    }
    
    public static Set<PredicateObjectMap> extractFallback(
            Repository repository, Resource object,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap) {
        Set<PredicateObjectMap> nestedPOMs = new HashSet();
        StdConditionExtractor conditionsExtractor =
                new StdConditionExtractor();
        List<Value> conditionResources =
                conditionsExtractor.extractFallback(repository, object);
        log.debug("Fallback condition resources found " 
                + conditionResources.size());

        log.debug("Extracting fallbacks details...");
        for (Value conditionResource : conditionResources) {
            //fallbackTerms = conditionsExtractor.extractFallback(
            //        repository, (Resource) conditionResource);
            //log.debug("found " + fallbackTerms.size());

            //for (Value fallbackTerm : fallbackTerms) {
            PredicateObjectMapExtractor preObjMapExtractor =
                    new PredicateObjectMapExtractor();
            log.debug("Extracting nested POMs...");

            PredicateObjectMap predicateObjectMap =
                    preObjMapExtractor.extractPredicateObjectMap(
                    repository, null, (Resource) conditionResource,
                    null, triplesMapResources,triplesMap);
            log.debug("fallback POM extracted "
                    + predicateObjectMap.toString());

            log.debug("Setting the fallback POM");
            nestedPOMs.add(predicateObjectMap);
            //extractFallback(fallbackTerm);
            //booleanCondition.setFallback(predicateObjectMap);
            //}
        }
        return nestedPOMs;
    }
}
