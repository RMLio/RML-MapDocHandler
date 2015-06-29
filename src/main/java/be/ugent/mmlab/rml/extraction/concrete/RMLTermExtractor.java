/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : RMLTermExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabulary.RMLVocabulary;
import org.apache.log4j.LogManager;
import org.openrdf.model.URI;

public class RMLTermExtractor {
    
    // Log
    private static final org.apache.log4j.Logger log = LogManager.getLogger(RMLTermExtractor.class);

    protected static URI getTermURI(
            RMLSesameDataSet rmlMappingGraph, Enum term) {
        String namespace = R2RMLVocabulary.R2RML_NAMESPACE;
        
        if (term instanceof RMLVocabulary.RMLTerm) {
            namespace = RMLVocabulary.RML_NAMESPACE;
        } else if (!(term instanceof R2RMLVocabulary.R2RMLTerm)) {
            log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + term + " is not valid.");
        }
        
        return rmlMappingGraph
                .URIref(namespace + term);
    }

}
