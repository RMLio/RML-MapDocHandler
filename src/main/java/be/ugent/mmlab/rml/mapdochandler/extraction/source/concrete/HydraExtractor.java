package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.model.InputSource;
import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.StdSourceExtractor;
import be.ugent.mmlab.rml.input.model.std.ApiInputSource;
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
    public Set<InputSource> extractSource(Repository repository, Value resource) {
        Set<InputSource> inputSources = new HashSet<InputSource>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    HydraVocabulary.HYDRA_NAMESPACE + HydraVocabulary.HydraTerm.TEMPLATE);
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) resource, predicate, resource, true);

            while (statements.hasNext()) {
                inputSources.add(
                        new ApiInputSource(
                        resource.stringValue(), statements.next().getObject().stringValue()));
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return inputSources;
    }
    
}
