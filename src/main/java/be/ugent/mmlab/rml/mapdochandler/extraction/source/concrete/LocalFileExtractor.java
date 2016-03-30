package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.source.std.StdLocalFileSource;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : LocalFileExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class LocalFileExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            LocalFileExtractor.class.getSimpleName());
    
    //TODO: Change extractInput to Value instead of Resource
    public Set<Source> extractInput(Repository repository, String source) {
        Set<Source> inputSources = new HashSet<Source>();
        
        inputSources.add(new StdLocalFileSource(source, source));
        
        return inputSources;
    }

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> inputSources = new HashSet<Source>();
        
        inputSources.add(new StdLocalFileSource(value.stringValue(), value.stringValue()));
        
        return inputSources;
    }
     
}
