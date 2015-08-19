package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdGraphMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
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

public class GraphMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(GraphMapExtractor.class);
    
    public Set<GraphMap> extractGraphMapValues(
            Repository repository, Set<Value> graphMapValues,
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
                newGraphMap = extractGraphMap(repository, (Resource) graphMap, triplesMap);

                savedGraphMaps.add(newGraphMap);
                graphMaps.add(newGraphMap);
            }
        }

        return graphMaps;
    }
    
    protected GraphMap extractGraphMap(
            Repository repository, Resource graphMap, TriplesMap triplesMap) {
        GraphMap result = null;
        try {
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract graph map...");

            RepositoryConnection connection = repository.getConnection();

            Value constantValue = TermMapExtractor.extractValueFromTermMap(repository,
                    graphMap, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(repository,
                    graphMap, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(repository,
                    graphMap, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            TermMapExtractor termMapExtractor = new TermMapExtractor();
            ReferenceMap referenceValue =
                    termMapExtractor.extractReferenceIdentifier(repository, graphMap, triplesMap);

            URI termType = (URI) TermMapExtractor.extractValueFromTermMap(repository,
                    graphMap, R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
            try {
                result = new StdGraphMap(constantValue, stringTemplate,
                        inverseExpression, referenceValue, termType);
            } catch (Exception ex) {
                log.error("Exception " + ex);
            }

            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Graph map extracted.");
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }
    
    public PredicateObjectMap processGraphMaps(
            Repository repository, Resource predicateObject, TriplesMap triplesMap, 
            PredicateObjectMap predicateObjectMap, Set<GraphMap> savedGraphMaps) {
        // Add graphMaps
        Set<GraphMap> graphMaps = new HashSet<GraphMap>();
        Set<Value> graphMapValues = TermMapExtractor.extractValuesFromResource(
                repository, predicateObject, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);

        if (graphMapValues != null) {
            graphMaps = extractGraphMapValues(
                    repository, graphMapValues, savedGraphMaps, triplesMap);
            log.info(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "graph Maps returned " + graphMaps);
        }

        predicateObjectMap.setGraphMaps(graphMaps);
        return predicateObjectMap;
    }

}
