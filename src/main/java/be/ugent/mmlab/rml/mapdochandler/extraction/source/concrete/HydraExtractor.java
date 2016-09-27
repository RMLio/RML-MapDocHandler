package be.ugent.mmlab.rml.mapdochandler.extraction.source.concrete;

import be.ugent.mmlab.rml.model.Source;
import be.ugent.mmlab.rml.mapdochandler.extraction.std.StdSourceExtractor;
import be.ugent.mmlab.rml.model.source.std.StdApiSource;
import be.ugent.mmlab.rml.vocabularies.HydraVocabulary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : HydraExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class HydraExtractor extends StdSourceExtractor {
    
    // Log
    private static final Logger log = 
            LoggerFactory.getLogger(
            HydraExtractor.class.getSimpleName());
    // Value factory
    private static ValueFactory vf = SimpleValueFactory.getInstance();


    @Override
    public Set<Source> extractSources(Repository repository, Value resource) {
        Set<Source> sources = new HashSet<Source>();
        try {
            RepositoryConnection connection = repository.getConnection();
            
            IRI predicate = vf.createIRI(
                    HydraVocabulary.HYDRA_NAMESPACE + HydraVocabulary.HydraTerm.TEMPLATE);
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) resource, predicate, null, true);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                Source inputSource = 
                        extractSource(repository, (Resource) resource, statement);
                sources.add(inputSource);
            }
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return sources;
    }
    
    public Source extractSource(
            Repository repository, Resource resource, Statement statement) {
        Value value = statement.getObject();
        
        List<Map<String, Boolean>> mapTemplates = 
                extractMappingTemplates(repository, resource);
        //log.debug("Mapping templates were extracted.");
        Source source = new StdApiSource(
                resource.stringValue(), value.stringValue(), mapTemplates);
        //log.debug("Source was extracted.");
        return source;
    }
    
    private List<Map<String,Boolean>> extractMappingTemplates(
            Repository repository, Resource resource){
        List<Map<String,Boolean>> mapTemplates = 
                new ArrayList<Map<String,Boolean>>();
        try {
            RepositoryConnection connection = repository.getConnection();
            IRI predicate = vf.createIRI(
                    HydraVocabulary.HYDRA_NAMESPACE + HydraVocabulary.HydraTerm.MAPPING);
            RepositoryResult<Statement> statements =
                    connection.getStatements((Resource) resource, predicate, null, true);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                Map<String,Boolean> mapTemplate = 
                        extractMappingTemplate(repository, statement);
                
                mapTemplates.add(mapTemplate);
            }
            connection.close();

        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return mapTemplates;
    }
    
    private Map<String, Boolean> extractMappingTemplate(
            Repository repository, Statement statement) {
        Map<String, Boolean> mapTemplate = new HashMap<String, Boolean>();
        String variable = null;
        Boolean required;

        try {
            RepositoryConnection connection = repository.getConnection();

            //Extract the variable
            IRI predicate = vf.createIRI(
                    HydraVocabulary.HYDRA_NAMESPACE
                    + HydraVocabulary.HydraTerm.VARIABLE);
            RepositoryResult<Statement> statements =
                    connection.getStatements(
                    (Resource) statement.getObject(), predicate, null, true);
            if (statements.hasNext()) {
                Statement variableStatement = statements.next();
                variable = variableStatement.getObject().stringValue();
            }

            //Extract required
            predicate = vf.createIRI(
                    HydraVocabulary.HYDRA_NAMESPACE
                    + HydraVocabulary.HydraTerm.REQUIRED);
            statements =
                    connection.getStatements(
                    (Resource) statement.getObject(), predicate, null, true);

            if (statements != null & statements.hasNext()) {
                Statement requiredStatement = statements.next();
                if (requiredStatement.getObject().stringValue().equals("true")) {
                    required = true;
                } else {
                    required = false;
                }
            } else {
                required = false;
            }

            mapTemplate.put(variable, required);
            connection.close();

        } catch (RepositoryException ex) {
            log.error("Repository Exception " + ex);
        }
        return mapTemplate;
    }
    
}
