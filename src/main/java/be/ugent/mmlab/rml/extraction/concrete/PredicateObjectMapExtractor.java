/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : PredicateObjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.ObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.PredicateMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RDFTerm.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdPredicateObjectMap;
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

/**
 * RML Processor
 *
 * @author andimou
 */
public class PredicateObjectMapExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(PredicateObjectMapExtractor.class);
    
    public PredicateObjectMap extractPredicateObjectMap(
            RMLSesameDataSet rmlMappingGraph,
            Resource triplesMapSubject,
            Resource predicateObject,
            Set<GraphMap> savedGraphMaps,
            Map<Resource, TriplesMap> triplesMapResources,
            TriplesMap triplesMap) {
        URI p = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.PREDICATE_MAP);
        
        List<Statement> predicate_statements = rmlMappingGraph.tuplePattern(
                predicateObject, p, null);
        Set<PredicateMap> predicateMaps = new HashSet<PredicateMap>();
        for (Statement predicate_statement : predicate_statements) {
            PredicateMapExtractor predMapExtractor = new PredicateMapExtractor();
            PredicateMap predicateMap = predMapExtractor.extractPredicateMap(
                    rmlMappingGraph, predicate_statement,
                    savedGraphMaps, triplesMap);
            predicateMaps.add(predicateMap);
        }
        // Extract object maps
        URI o = rmlMappingGraph.URIref(R2RMLVocabulary.R2RML_NAMESPACE
                + R2RMLVocabulary.R2RMLTerm.OBJECT_MAP);
        List<Statement> object_statements = rmlMappingGraph.tuplePattern(predicateObject, o, null);

        Set<ObjectMap> objectMaps = new HashSet<ObjectMap>();
        Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
        for (Statement object_statement : object_statements) {
            ReferencingObjectMapExtractor refObjMapExtractor = new ReferencingObjectMapExtractor();
            refObjectMaps = refObjMapExtractor.processReferencingObjectMap(
                    rmlMappingGraph, object_statements, savedGraphMaps,
                    triplesMapResources, triplesMap, triplesMapSubject, predicateObject);
            if (refObjectMaps.isEmpty()) {
                ObjectMapExtractor objMapExtractor = new ObjectMapExtractor();
                ObjectMap objectMap = objMapExtractor.extractObjectMap(rmlMappingGraph,
                        (Resource) object_statement.getObject(), savedGraphMaps, triplesMap);
                try {
                    objectMap.setOwnTriplesMap(triplesMapResources.get(triplesMapSubject));
                //} catch (InvalidR2RMLStructureException ex) {
                } catch (Exception ex) {    
                    log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
                }
                objectMaps.add(objectMap);
            }
            PredicateObjectMap predicateObjectMap = new StdPredicateObjectMap(
                    predicateMaps, objectMaps, refObjectMaps);
            GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
            graphMapExtractor.processGraphMaps(rmlMappingGraph, predicateObject, triplesMap, predicateObjectMap, savedGraphMaps);

            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract predicate-object map done.");
            return predicateObjectMap;
        }

        return null;
    }

}
