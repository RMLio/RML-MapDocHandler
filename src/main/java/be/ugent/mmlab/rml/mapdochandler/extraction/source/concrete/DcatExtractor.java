package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.vocabularies.DCATVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : DcatExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class DcatExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = LoggerFactory.getLogger(DcatExtractor.class);

    public DcatExtractor() {
    }

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> inputSources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    DCATVocabulary.DCAT_NAMESPACE + DCATVocabulary.DcatTerm.DOWNLOADURL);
            //TODO: Fix the following: sub and obj same value
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, null, true);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                Source inputSource = extractSource((Resource) value, statement);
                inputSources.add(inputSource);
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return inputSources;
    }
    
    public Source extractSource(Resource resource, Statement statement) {
        Source source = new StdApiSource(
                resource.stringValue(), statement.getObject().stringValue());
        return source;
    }

}
