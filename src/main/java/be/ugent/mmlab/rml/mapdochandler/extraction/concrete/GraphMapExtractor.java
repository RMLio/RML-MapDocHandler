package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdGraphMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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

public class GraphMapExtractor extends StdTermMapExtractor {
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(
            GraphMapExtractor.class.getSimpleName());
    
    public Set<GraphMap> extractGraphMapValues(
            Repository repository, Set<Value> graphMapValues, TriplesMap triplesMap) {

        Set<GraphMap> graphMaps = new HashSet<GraphMap>();

        if(graphMapValues != null)
        for (Value graphMap : graphMapValues) {
            // Create associated graphMap if it has not already created
            boolean found = false;
            GraphMap graphMapFound = null;

            if (found) {
                graphMaps.add(graphMapFound);
            } else {
                GraphMap newGraphMap = null;
                newGraphMap = extractGraphMap(repository, (Resource) graphMap, triplesMap);

                graphMaps.add(newGraphMap);
            }
        }

        return graphMaps;
    }
    
    protected GraphMap extractGraphMap(
            Repository repository, Resource graphMap, TriplesMap triplesMap) {
        GraphMap result = null;
        log.debug("Extract Graph Map...");
        try {
            RepositoryConnection connection = repository.getConnection();
            
            extractProperties(repository, triplesMap, graphMap);

            try {
                result = new StdGraphMap(constantValue, stringTemplate,
                        inverseExpression, referenceValue, termType);
                log.debug("Graph map extracted.");
            } catch (Exception ex) {
                log.error("Exception " + ex);
            }

            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }
    
    public PredicateObjectMap processGraphMaps(
            Repository repository, Resource predicateObject, TriplesMap triplesMap, 
            PredicateObjectMap predicateObjectMap, GraphMap savedGraphMap) {
        // Add graphMaps
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        Set<Value> graphMapValues = TermExtractor.extractValuesFromResource(
                repository, predicateObject, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);

        if (graphMapValues != null) {
            graphMaps = extractGraphMapValues(
                    repository, graphMapValues, triplesMap);
        }
        predicateObjectMap.setGraphMaps(graphMaps);
        return predicateObjectMap;
    }

}
