package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.StdSourceExtractor;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.vocabulary.HydraVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * RML - Data Retrieval Handler : ApiExtractor
 *
 * @author andimou
 */
public class HydraExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = LoggerFactory.getLogger(HydraExtractor.class);
    // Value factory
    private static ValueFactory vf = new ValueFactoryImpl();


    @Override
    public Set<Source> extractSources(Repository repository, Value resource) {
        Set<Source> sources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();

            URI predicate = vf.createURI(
                    HydraVocabulary.HYDRA_NAMESPACE + HydraVocabulary.HydraTerm.TEMPLATE);
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) resource, predicate, null, true);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                Source inputSource = extractSource((Resource) resource, statement);
                sources.add(inputSource);
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return sources;
    }
    
    public Source extractSource(Resource resource, Statement statement) {
        Value source = statement.getObject();
        
        Source inputSource = new StdApiSource(
                resource.stringValue(), source.stringValue());
        
        return inputSource;
    }
    
}
