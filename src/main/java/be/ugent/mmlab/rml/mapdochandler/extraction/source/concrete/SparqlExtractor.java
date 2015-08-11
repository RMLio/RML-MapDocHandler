package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.mapdochandler.extraction.concrete.StdSourceExtractor;
import be.ugent.mmlab.rml.model.source.SparqlSdSource;
import be.ugent.mmlab.rml.vocabulary.SPARQLSDVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * RML - Data Retrieval Handler : SparqlExtractor
 *
 * @author andimou
 */
public class SparqlExtractor extends StdSourceExtractor {

    // Log
    private static final Logger log = LoggerFactory.getLogger(SparqlExtractor.class);

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> inputSources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();

            URI predicate = vf.createURI(SPARQLSDVocabulary.SPARQLSD_NAMESPACE
                    + SPARQLSDVocabulary.SparqlSdTerm.SPARQL_QUERY_TEMPLATE);
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, null, true);

            while (statements.hasNext()) {
                inputSources.add(
                        new SparqlSdSource(
                        value.stringValue(), statements.next().getObject().stringValue()));
            }
            connection.close();

        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return inputSources;
    }
}
