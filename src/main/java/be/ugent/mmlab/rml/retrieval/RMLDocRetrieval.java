package be.ugent.mmlab.rml.retrieval;

import be.ugent.mmlab.rml.sesame.RMLSesameDataSet;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * *************************************************************************
 *
 * RML - Mapping Document Handler : RMLDocRetrieval
 *
 *
 * @author andimou
 *
 ***************************************************************************
 */
public class RMLDocRetrieval {
    
    // Log
    static final Logger log = LoggerFactory.getLogger(RMLDocRetrieval.class);
    
    /**
     *
     * @param fileToRMLFile
     * @param format
     * @return
     */
    public RMLSesameDataSet getMappingDoc(String fileToRMLFile, RDFFormat format) {
        RMLSesameDataSet rmlMappingGraph = new RMLSesameDataSet();
        //RML document is a URI
        if (!isLocalFile(fileToRMLFile)) {
            try {
                log.info(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                        + "file "
                        + fileToRMLFile + " loaded from URI.");
                HttpURLConnection con = (HttpURLConnection) new URL(fileToRMLFile).openConnection();
                con.setRequestMethod("HEAD");
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try {
                        rmlMappingGraph.addURI(fileToRMLFile, RDFFormat.TURTLE);
                    } catch (Exception e) {
                        log.error(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                                + "[RMLMapping Factory:extractRMLMapping] " + e);
                    }
                }
            } catch (MalformedURLException ex) {
                log.error("MalformedURLException " + ex);
            } catch (IOException ex) {
                log.error("IOException " + ex);
            }
        } 
        else {
            try {
                rmlMappingGraph.loadDataFromFile(fileToRMLFile, RDFFormat.TURTLE);
            } catch (RepositoryException ex) {
                log.error("RepositoryException " + ex);
            } catch (IOException ex) {
                log.error("IOException " + ex);
            } catch (RDFParseException ex) {
                log.error("RDFParseException " + ex);
            }
        }
        log.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + ": "
                + "Number of RML triples in file "
                + fileToRMLFile + " : " + rmlMappingGraph.getSize() + " from local file");

        return rmlMappingGraph;
    }
    
    /**
     *
     * @param source
     * @return
     */
    public static boolean isLocalFile(String source) {
        try {
            //TODO: Find a better way to check for file:/
            if(source.startsWith("file:/"))
                return true;
            new URL(source);
            return false;
        } catch (MalformedURLException e) {
            return true;
        }
    }
    
}
