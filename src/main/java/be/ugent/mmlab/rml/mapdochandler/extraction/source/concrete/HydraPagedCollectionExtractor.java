package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.vocabularies.HydraVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : HydraPagedCollectionExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class HydraPagedCollectionExtractor  extends StdSourceExtractor {
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            HydraPagedCollectionExtractor.class.getSimpleName());
    // Value factory
    private static ValueFactory vf = SimpleValueFactory.getInstance();

    @Override
    public Set<Source> extractSources(Repository repository, Value resource) {
        Set<Source> sources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            
            IRI predicate = vf.createIRI(
                    HydraVocabulary.HYDRA_NAMESPACE 
                    + HydraVocabulary.HydraTerm.FIRSTPAGE);
            RepositoryResult<Statement> statements =
                    connection.getStatements(
                    (Resource) resource, predicate, null, true);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                log.debug("collection statement " + statement);
                //Source inputSource = 
                //        extractSource(repository, (Resource) resource, statement);
                Source source = new StdApiSource(resource.stringValue(), 
                        statement.getObject().stringValue(), null);
                sources.add(source);
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return sources;
    }
    
    /*public Source extractSource(
            Repository repository, Resource resource, Statement statement) {
        Value value = statement.getObject();
        
        
        //log.debug("Mapping templates were extracted.");
        //Source source = new StdApiSource(
        //        resource.stringValue(), value.stringValue(), mapTemplates);
        //log.debug("Source was extracted.");
        //return source;
    }*/

}
