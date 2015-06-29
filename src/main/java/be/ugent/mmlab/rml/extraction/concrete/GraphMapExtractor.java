/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : GraphMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.extraction.RMLUnValidatedMappingExtractor;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdGraphMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class GraphMapExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(GraphMapExtractor.class);
    
    public Set<GraphMap> extractGraphMapValues(
            RMLSesameDataSet rmlMappingGraph, Set<Value> graphMapValues, 
            Set<GraphMap> savedGraphMaps, TriplesMap triplesMap) {
        
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        
            for (Value graphMap : graphMapValues) {
                // Create associated graphMap if it has not already created
                boolean found = false;
                GraphMap graphMapFound = null;
                
                if (found) {
                    graphMaps.add(graphMapFound);
                } else {
                    GraphMap newGraphMap = null;
                    newGraphMap = extractGraphMap(rmlMappingGraph, (Resource) graphMap, triplesMap);
                    
                    savedGraphMaps.add(newGraphMap);
                    graphMaps.add(newGraphMap);
                }
            }
        
        return graphMaps;
    }
    
    protected GraphMap extractGraphMap(
            RMLSesameDataSet rmlMappingGraph,
            Resource graphMap, TriplesMap triplesMap) {
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Extract graph map...");

        Value constantValue = TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
        String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
        String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
        TermMapExtractor termMapExtractor = new TermMapExtractor();
        ReferenceMap referenceValue = 
                termMapExtractor.extractReferenceIdentifier(rmlMappingGraph, graphMap, triplesMap);

        URI termType = (URI) TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                graphMap, R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);

        GraphMap result = null;
        try {
            result = new StdGraphMap(constantValue, stringTemplate,
           inverseExpression, referenceValue, termType);
        } catch (Exception ex) {
            log.error(RMLUnValidatedMappingExtractor.class.getName() + ex);
        } 
        
        log.debug(
                Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Graph map extracted.");
        return result;
    }
    
    protected PredicateObjectMap processGraphMaps(
            RMLSesameDataSet rmlMappingGraph, Resource predicateObject, TriplesMap triplesMap, 
            PredicateObjectMap predicateObjectMap, Set<GraphMap> savedGraphMaps) {
        // Add graphMaps
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        Set<Value> graphMapValues = TermMapExtractor.extractValuesFromResource(
                rmlMappingGraph, predicateObject, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);

        if (graphMapValues != null) {
            graphMaps = extractGraphMapValues(
                    rmlMappingGraph, graphMapValues, savedGraphMaps, triplesMap);
            log.info(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "graph Maps returned " + graphMaps);
        }

        predicateObjectMap.setGraphMaps(graphMaps);
        return predicateObjectMap;
    }

}
