package be.ugent.mmlab.rml.mapdochandler.extraction.concrete;

import be.ugent.mmlab.rml.model.RDFTerm.LanguageMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.std.StdLanguageMap;
import be.ugent.mmlab.rml.model.termMap.ReferenceMap;
import be.ugent.mmlab.rml.vocabularies.R2RMLVocabulary;
import be.ugent.mmlab.rml.vocabularies.RMLVocabulary;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RML Processor
 *
 * @author andimou
 */
public class LanguageMapExtractor {
    
    // Log
    static final Logger log = 
            LoggerFactory.getLogger(LanguageMapExtractor.class);
    
    public static LanguageMap extractLanguageMap(
            Repository repository, Resource object, TriplesMap triplesMap){
        String languageTag = TermMapExtractor.extractLiteralFromTermMap(repository,
                    object, R2RMLVocabulary.R2RMLTerm.LANGUAGE, triplesMap);
        LanguageMap languageMap = null;
        
        if (languageTag == null) {
            Value languageValue =
                    TermMapExtractor.extractValueFromTermMap(repository,
                    object, RMLVocabulary.RMLTerm.LANGUAGE_MAP, triplesMap);
            if (languageValue != null) {

                Value constantValue = TermMapExtractor.extractValueFromTermMap(
                        repository, (Resource) languageValue,
                        R2RMLVocabulary.R2RMLTerm.CONSTANT, triplesMap);
                
                String stringTemplate = TermMapExtractor.extractLiteralFromTermMap(
                        repository, (Resource) languageValue,
                        R2RMLVocabulary.R2RMLTerm.TEMPLATE, triplesMap);
                
                ReferenceMap referenceValue =
                        TermMapExtractor.extractReferenceIdentifier(
                        repository, (Resource) languageValue, triplesMap);

                languageMap = new StdLanguageMap(
                        constantValue, stringTemplate, referenceValue);
            }
        }
        else {
            RepositoryConnection connection;
            try {
                connection = repository.getConnection();
                ValueFactory vf = connection.getValueFactory();
                Value languageValue = vf.createLiteral(languageTag);
                languageMap = new StdLanguageMap(languageValue, null, null); 
                connection.close();
                        
            } catch (RepositoryException ex) {
                log.error("Repository Exception " + ex);
            }

        }
        return languageMap;
    }

}
