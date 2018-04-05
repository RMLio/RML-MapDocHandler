package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.ReferenceFormulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : Reference Formulation Extractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */

public class ReferenceFormulationExtractor {
    
    // Log
    static final Logger log =
            LoggerFactory.getLogger(
            ReferenceFormulationExtractor.class.getSimpleName());
    
    /**
     *
     * @param repository
     * @param Value
     * @return
     */
    public ReferenceFormulation extractReferenceFormulation(
            Repository repository, Value Value) {

        return null;
    }

}
