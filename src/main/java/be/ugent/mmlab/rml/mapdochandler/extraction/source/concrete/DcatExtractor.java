package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.input.model.std.ApiInputSource;
import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.StdSourceExtractor;
import be.ugent.mmlab.rml.model.InputSource;
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
 * RML - Data Retrieval Handler : DCAT Extractor
 *
 * @author andimou
 */
public class DcatExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = LoggerFactory.getLogger(DcatExtractor.class);

    public DcatExtractor() {
    }

    @Override
    public Set<InputSource> extractSource(Repository repository, Value value) {
        Set<InputSource> inputSources = new HashSet<InputSource>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    DCATVocabulary.DCAT_NAMESPACE + DCATVocabulary.DcatTerm.DOWNLOADURL);
            //TODO: Fix the following: sub and obj same value
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, value, true);
 
            while (statements.hasNext()) {
                inputSources.add(
                        new ApiInputSource(
                        value.stringValue(), statements.next().getObject().stringValue()));
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return inputSources;
    }

}
