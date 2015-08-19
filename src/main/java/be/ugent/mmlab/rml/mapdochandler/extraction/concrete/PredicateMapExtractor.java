package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.PredicateMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdPredicateMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;

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
            Repository repository, Statement statement,
            Set<GraphMap> graphMaps, TriplesMap triplesMap) {
        Resource object = (Resource) statement.getObject();

        try {
            // Extract object maps properties
            Value constantValue = TermMapExtractor.extractValueFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
            String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
            URI termType = (URI) TermMapExtractor.extractValueFromTermMap(repository, object,
                    R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);

            String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            TermMapExtractor termMapExtractor = new TermMapExtractor();
            //MVS: Decide on ReferenceIdentifier
            ReferenceMap referenceValue = 
                    termMapExtractor.extractReferenceIdentifier(repository, object, triplesMap);

            PredicateMap result = new StdPredicateMap(null, constantValue,
                    stringTemplate, inverseExpression, referenceValue, termType);
            log.debug("Extracting predicate map done.");
            return result;
        } catch (Exception ex) {
            log.error("Exception: " + ex);
        }
        return null;
    }

}
