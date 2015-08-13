package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.Source;
import java.util.Set;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;

/**
 * RML - Data Retrieval Handler : InputExtractor
 *
 * @author andimou
 */
public interface SourceExtractor {

    /**
     * @param repository
     * @param resource
     * @return
     */
    public Set<Source> extractSources(Repository repository, Value resource);

}
