package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.ReferenceFormulation;
import be.ugent.mmlab.rml.model.Source;
import java.util.Set;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : Source Extractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public interface SourceExtractor {

    /**
     * @param repository
     * @param resource
     * @return
     */
    public Set<Source> extractSources(Repository repository, Value resource);
    
    //Extract a customly defined Reference Formulation
    //that's described at the Source side, e.g. the CSVW dialect
    /**
     *
     * @param repository
     * @param resource
     * @return
     */
    public ReferenceFormulation extractCustomReferenceFormulation(
            Repository repository, Value resource);

}
