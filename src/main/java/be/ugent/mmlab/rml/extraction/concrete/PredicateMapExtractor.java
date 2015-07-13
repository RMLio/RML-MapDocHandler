package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.PredicateMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdPredicateMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : PredicateMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class PredicateMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(PredicateMapExtractor.class);
    
    public PredicateMap extractPredicateMap(
            RMLSesameDataSet rmlMappingGraph, Statement statement,
            Set<GraphMap> graphMaps, TriplesMap triplesMap) {
        Resource object = (Resource) statement.getObject();
        try {
            // Extract object maps properties
            Value constantValue = TermMapExtractor.extractValueFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            URI termType = (URI) TermMapExtractor.extractValueFromTermMap(rmlMappingGraph, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);

            String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(rmlMappingGraph,
                    object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            TermMapExtractor termMapExtractor = new TermMapExtractor();
            //MVS: Decide on ReferenceIdentifier
            ReferenceMap referenceValue = 
                    termMapExtractor.extractReferenceIdentifier(rmlMappingGraph, object, triplesMap);

            PredicateMap result = new StdPredicateMap(null, constantValue,
                    stringTemplate, inverseExpression, referenceValue, termType);
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract predicate map done.");
            return result;
        } catch (Exception ex) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
        }
        return null;
    }

}
