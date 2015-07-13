package be.ugent.mmlab.rml.extraction;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import java.util.Map;
import org.openrdf.model.Resource;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : RMLMappingExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public interface RMLMappingExtractor {
    
    /**
     *
     * @param rmlMappingGraph
     */
    public RMLSesameDataSet replaceShortcuts(RMLSesameDataSet rmlMappingGraph);
    
    /**
     *
     * @param rmlMappingGraph
     * @return
     */
    public RMLSesameDataSet skolemizeStatements(RMLSesameDataSet rmlMappingGraph);
    
    /**
     *
     * @param rmlMappingGraph
     * @return
     */
    public Map<Resource, TriplesMap> extractTriplesMapResources(
            RMLSesameDataSet rmlMappingGraph);
        
}
