package be.ugent.mmlab.rml.mapdochandler.extraction;

import be.ugent.mmlab.rml.model.TriplesMap;
import java.util.Map;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;

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
     * @return Repository
     */
    public Repository replaceShortcuts(Repository repository);
    
    /**
     *
     * @param rmlMappingGraph
     * @return Repository
     */
    public Repository skolemizeStatements(Repository repository);
    
    /**
     *
     * @param rmlMappingGraph
     * @return Map<Resource, TriplesMap>
     */
    public Map<Resource, TriplesMap> extractTriplesMapResources(Repository repository);
        
}
