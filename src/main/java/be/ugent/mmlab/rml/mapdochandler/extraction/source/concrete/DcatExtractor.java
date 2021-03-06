package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.vocabularies.DCATVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
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
    private static final Logger log = 
            LoggerFactory.getLogger(
            DcatExtractor.class.getSimpleName());

    public DcatExtractor() {
    }

    @Override
    public Set<Source> extractSources(Repository repository, Value value) {
        Set<Source> inputSources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            IRI predicate = vf.createIRI(
                    DCATVocabulary.DCAT_NAMESPACE + DCATVocabulary.DcatTerm.DOWNLOADURL);
            
            //Extract DCAT Distribution
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) value, predicate, null, true);
            log.debug("Distribution statements " + statements.hasNext());
            
            if(!statements.hasNext()){
                predicate = vf.createIRI(
                    DCATVocabulary.DCAT_NAMESPACE + DCATVocabulary.DcatTerm.DISTRIBUTION);
                statements = connection.getStatements((Resource) value, predicate, null, true);
                
                log.debug("Dataset statements " + statements.hasNext());
                while(statements.hasNext()){
                    Statement statement = statements.next();
                    predicate = vf.createIRI(
                            DCATVocabulary.DCAT_NAMESPACE + DCATVocabulary.DcatTerm.DOWNLOADURL);
                    RepositoryResult<Statement> distributions = connection.getStatements(
                            (Resource) statement.getObject(), predicate, null, true);
                    while (distributions.hasNext()) {
                        Statement distribution = distributions.next();
                        Source inputSource = extractSource((Resource) value, distribution);
                        inputSources.add(inputSource);
                    }
                }
            } else {
                while (statements.hasNext()) {
                    Statement statement = statements.next();
                    Source inputSource = extractSource((Resource) value, statement);
                    inputSources.add(inputSource);
                }
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
