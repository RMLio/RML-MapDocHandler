package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.StdSourceExtractor;
import be.ugent.mmlab.rml.input.model.std.ApiInputSource;
import be.ugent.mmlab.rml.model.InputSource;
import be.ugent.mmlab.rml.vocabulary.VoIDVocabulary;
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
 * RML - Data Retrieval Handler : VoID Extractor
 *
 * @author andimou
 */
public class VoidExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = LoggerFactory.getLogger(VoidExtractor.class);

    public VoidExtractor() {
    }

    @Override
    public Set<InputSource> extractSource(Repository repository, Value value) {
        Set<InputSource> inputSources = new HashSet<InputSource>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            URI predicate = vf.createURI(
                    VoIDVocabulary.VOID_NAMESPACE + VoIDVocabulary.VoIDTerm.SPARQLENDPOINT);
            //TODO: Check the following: sub and obj same value
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, null, true);

            if (!statements.hasNext()) {
                predicate = vf.createURI(
                        VoIDVocabulary.VOID_NAMESPACE + VoIDVocabulary.VoIDTerm.DATADUMP);
                //TODO: Check the following: sub and obj same value
                statements = connection.getStatements((Resource) value, predicate, null, true);
            }

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
