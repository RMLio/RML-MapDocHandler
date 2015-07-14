package be.ugent.mmlab.rml.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.GraphMap;
import be.ugent.mmlab.rml.model.RDFTerm.SubjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdSubjectMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabulary.R2RMLVocabulary;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : SubjectMapExtractor
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class SubjectMapExtractor {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(SubjectMapExtractor.class);
    
    public SubjectMap extractSubjectMap(
            Repository repository, Resource triplesMapSubject,
            Set<GraphMap> savedGraphMaps, TriplesMap triplesMap) {
        SubjectMap result = null;
        RepositoryResult<Statement> statements;
        try {
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Extract subject map...");
            RepositoryConnection connection = repository.getConnection();
            // Extract subject map
            statements = connection.getStatements(triplesMapSubject, RMLTermExtractor.getTermURI(
                    repository, R2RMLVocabulary.R2RMLTerm.SUBJECT_MAP), null, true);

            Resource subjectMap = (Resource) statements.next().getObject();


            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Found subject map : "
                    + subjectMap.stringValue());

            Value constantValue = TermMapExtractor.extractValueFromTermMap(repository,
                    subjectMap, R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);

            String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(repository,
                    subjectMap, R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);

            URI termType = (URI) TermMapExtractor.extractValueFromTermMap(repository,
                    subjectMap, R2RMLVocabulary.R2RMLTerm.TERM_TYPE, triplesMap);
            String inverseExpression = TermMapExtractor.extractLiteralFromTermMap(repository,
                    subjectMap, R2RMLVocabulary.R2RMLTerm.INVERSE_EXPRESSION, triplesMap);
            //TODO:fix the following validation
            //validator.checkTermMap(constantValue, stringTemplate, null, subjectMap.toString());

            //TODO:Add check if the referenceValue is a valid reference according to the reference formulation
            TermMapExtractor termMapExtractor = new TermMapExtractor();
            ReferenceMap referenceValue =
                    termMapExtractor.extractReferenceIdentifier(repository, subjectMap, triplesMap);

            //AD: The values of the rr:class property must be IRIs. 
            //AD: Would that mean that it can not be a reference to an extract of the input or a template?
            Set<URI> classIRIs = TermMapExtractor.extractURIsFromTermMap(repository,
                    subjectMap, R2RMLVocabulary.R2RMLTerm.CLASS);

            //AD:Move it a separate function that extracts the GraphMaps
            Set<GraphMap> graphMaps = new HashSet<GraphMap>();
            Set<Value> graphMapValues = TermMapExtractor.extractValuesFromResource(
                    repository, subjectMap, R2RMLVocabulary.R2RMLTerm.GRAPH_MAP);

            if (graphMapValues != null) {
                GraphMapExtractor graphMapExtractor = new GraphMapExtractor();
                graphMaps = graphMapExtractor.extractGraphMapValues(
                        repository, graphMapValues, savedGraphMaps, triplesMap);
                log.info(
                        Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "graph Maps returned " + graphMaps);
            }


            try {
                result = new StdSubjectMap(triplesMap, constantValue,
                        stringTemplate, termType, inverseExpression,
                        referenceValue, classIRIs, graphMaps);
            } catch (Exception ex) {
                log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": " + ex);
            }
            log.debug(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                    + "Subject map extracted.");
            connection.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        }
        return result;
    }

}
