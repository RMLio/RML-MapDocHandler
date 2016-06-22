package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.condition.extractor.AbstractConditionExtractor;
import be.ugent.mmlab.rml.condition.model.Condition;
import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : StdTermMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class StdTermMapExtractor implements TermMapExtractor {
    protected Value constantValue = null;
    protected String stringTemplate = null, inverseExpression = null;
    protected URI termType = null;
    protected TermExtractor termMapExtractor = null;
    protected ReferenceMap referenceValue = null;
    protected Set<Condition> conditions = null;
    protected Set<Value> graphMapValues = null;
    protected Set<GraphMap> graphMaps = null;
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(
            StdTermMapExtractor.class.getSimpleName());
    
    //TODO: Spring it!
    public void extractProperties(
            Repository repository, TriplesMap triplesMap, Resource object) {
        // Extract object maps properties
        constantValue = TermExtractor.extractValueFromTermMap(repository,
                object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
        stringTemplate = TermExtractor.extractLiteralFromTermMap(repository,
                object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
        termType = (URI) TermExtractor.extractValueFromTermMap(repository, object,
                R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
        inverseExpression = TermExtractor.extractLiteralFromTermMap(repository,
                object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
        termMapExtractor = new TermExtractor();
        //MVS: Decide on ReferenceIdentifier
        //TODO:Add check if the referenceValue is a valid reference 
        //     according to the reference formulation
        referenceValue = termMapExtractor.extractReferenceIdentifier(
                repository, object, triplesMap);
        
        //AD:Move it a separate function that extracts the GraphMaps
        graphMaps = new HashSet<GraphMap>();
        graphMapValues = TermExtractor.extractValuesFromResource(
                    repository, object, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);
        if(graphMapValues != null)
            log.debug("Found Graph Maps!");
        //TODO: Generalize the following for every condition
        AbstractConditionExtractor conditionsExtractor =
                new AbstractConditionExtractor();
        conditions = conditionsExtractor.extractConditions(repository, object);

    }

    public  GraphMap extractGraphMap(Repository repository, TriplesMap triplesMap, GraphMap graphMap){
        if (graphMapValues != null && graphMapValues.size() > 0) {
            GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
            graphMaps = graphMapExtractor.extractGraphMapValues(
                    repository, graphMapValues, triplesMap);
            if(graphMaps != null && graphMaps.size() > 0){
                graphMap = graphMaps.iterator().next();
            }

        }
        return graphMap;
    }

}
