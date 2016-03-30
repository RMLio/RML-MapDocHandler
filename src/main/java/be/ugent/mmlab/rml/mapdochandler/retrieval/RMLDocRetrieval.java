package be.ugent.mmlab.rml.mapdochandler.retrieval;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

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
    static final Logger log = LoggerFactory.getLogger(
            RMLDocRetrieval.class.getSimpleName());
    
    /**
     *
     * @param fileToRMLFile
     * @param format
     * @return
     */
    public Repository getMappingDoc(String fileToRMLFile, RDFFormat format) {
        Repository repo = new SailRepository(new MemoryStore());
        try {
            //RML document is a URI
            repo.initialize();
            RepositoryConnection con = repo.getConnection();

            if (!isLocalFile(fileToRMLFile)) {
                try {
                    log.info("Mapping Document "
                            + fileToRMLFile + " loaded from URI.");
                    HttpURLConnection httpCon = (HttpURLConnection) new URL(fileToRMLFile).openConnection();
                    httpCon.setRequestMethod("HEAD");
                    if (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        try {
                            //TODO: Fix the URL
                            //TODO: Change null to base IRI
                            con.add(new URL(fileToRMLFile), null, format);
                            //rmlMappingGraph.addURI(fileToRMLFile, RDFFormat.TURTLE);
                        } catch (Exception e) {
                            log.error("Exception " + e);
                        }
                    }
                } catch (MalformedURLException ex) {
                    log.error("MalformedURLException " + ex);
                } catch (IOException ex) {
                    log.error("IOException " + ex);
                }
            } else {
                try {
                    con.add(new File(fileToRMLFile), null, format);
                    //rmlMappingGraph.loadDataFromFile(fileToRMLFile, RDFFormat.TURTLE);
                } catch (RepositoryException ex) {
                    log.error("RepositoryException " + ex);
                } catch (IOException ex) {
                    log.error("IOException " + ex);
                } catch (RDFParseException ex) {
                    log.error("RDFParseException " + ex);
                }
            }
            log.debug("Number of RML triples in the repository "
                    + fileToRMLFile + " : " + con.size() + " from local file");
            if(con.size() == 0)
                return null;
            con.close();
        } catch (RepositoryException ex) {
            log.error("RepositoryException " + ex);
        } 
        return repo;
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
