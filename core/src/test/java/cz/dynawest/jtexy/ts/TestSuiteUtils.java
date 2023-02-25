package cz.dynawest.jtexy.ts;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.util.Levenshtein;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.ComparisonFailure;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;


/**
 *
 * @author Ondrej Zizka
 */
public class TestSuiteUtils {
    private static final Logger log = Logger.getLogger( TestSuiteUtils.class.getName() );
    
    public static void runTest( TexyTestCaseInfo tcInfo ) throws FileNotFoundException, TexyException, IOException {
        
		// Parse the file with JTexy.
		JTexy texy = JTexy.create();
        
        // Config.
        texy.setImagesUrlPrefix("/images/"); // The testsuite assumes this.
        
		String srcStr = FileUtils.readFileToString(tcInfo.getSrc());
		String outStr = texy.process(srcStr).trim();

		// Store the result to a file.
		FileUtils.writeStringToFile( tcInfo.getOut(), outStr );

        // If no ref file, nothing more to do.
        if( tcInfo.getRef() == null )
            return;

        
        // Read the reference file.
		String refStr = FileUtils.readFileToString(tcInfo.getRef())
                .replace("\r\n", "\n").replace("\n\n", "\n").trim();
        
        // Read both template and result with Dom4j, dump source, and compare that.
        
        String outStrNorm = normalizeHTML("<html>\n" + outStr  + "\n</html>");
        String refStrNorm = normalizeHTML("<html>\n" + refStr  + "\n</html>");
        outStrNorm = outStrNorm.replaceAll("[\\s\u00A0]+\n", "\n"); // Hack - there are extra space for some reason.
        refStrNorm = refStrNorm.replaceAll("[\\s\u00A0]+\n", "\n"); // Hack - there are extra space for some reason.
        
		
		// Compare the result with the reference .html file contents.
		
		int distance = Levenshtein.distance( outStr, refStr );
		int distanceNoLines = Levenshtein.distance(outStr.replace("\n", ""), refStr.replace("\n", ""));
		String levComparison = "Levenshtein:      "+distance+"("+distanceNoLines+")/"+refStr.length();
        log.info( levComparison );
        
		distance = Levenshtein.distance( outStrNorm, refStrNorm );
		distanceNoLines = Levenshtein.distance(outStrNorm.replace("\n", ""), refStrNorm.replace("\n", ""));
		levComparison = "Levenshtein norm: "+distance+"("+distanceNoLines+")/"+refStrNorm.length();
        log.info( levComparison );

        // TBD: Currently, we compare code normalized through Dom4J. 
        // Ideally, we would compare the actual output, but whitespace formatting prevents that for now.
        try {
            Assert.assertEquals(getErrMsg(tcInfo, levComparison), refStrNorm, outStrNorm);
        } catch( ComparisonFailure ex ){
            throw new ComparisonFailure(
                    ex.getMessage().replace("expected:", "EXPECTED:\n").replace("but was:", "\n\nACTUAL:\n")
                    , "", "" //, ex.getExpected(), ex.getActual()
            );
        }
	}// runTest()

    
    /** Format comparison failure message. */
    private static String getErrMsg(TexyTestCaseInfo tcInfo, String levComparison) {
        return String.format("Result for '%s' does not match ref '%s'; %s.\n    %s\n\n",
                //+ "EXPECTED:\n%s\n"
                //+ "ACTUAL:\n%s\n",
                tcInfo.getSrc().getName(), tcInfo.getRef().getName(), levComparison, tcInfo.getOut().getAbsolutePath());
    }
    
    
    
    
	/**
	 * Checks whether it's a dir and returns its File object.
     * @returns  Given dir, or null if error and path doesn't point to a valid dir.
	 */
	public static File getAndCheckDir( String path ){
        return getAndCheckDir( path, true );
    }
	public static File getAndCheckDir( String path, boolean throwEx ){

        if( null == path )
            if( throwEx ) throw new IllegalArgumentException("Arg 'path' is null.");
            else { log.warning("Arg 'path' is null."); return null; }

        return checkDir( new File(path), throwEx );
	}

    public static File checkDir( File dirToCheck) { return checkDir( dirToCheck, true ); }
    public static File checkDir( File dirToCheck, boolean throwEx) {
		if( ! dirToCheck.exists() )
			if( throwEx ) Assert.fail(" TS dir does not exist: " + dirToCheck.getPath());
            else{ log.warning(" TS dir does not exist: " + dirToCheck.getPath()); return null; }
		if( ! dirToCheck.isDirectory() )
			Assert.fail(" TS path is not a directory: " + dirToCheck.getPath());
		return dirToCheck;
    }

    
	/**
	 * Checks whether it's a file and returns its File object.
	 */
	public static File getAndCheckFile( String path ){

        if( null == path )  throw new IllegalArgumentException("Arg 'path' is null.");
        
		File file = new File(path);
		if( ! file.exists() )
			throw new RuntimeException(" TS file does not exist: " + path);
		if( ! file.isFile() )
			throw new RuntimeException(" Path does not lead to a file: " + path);
		return file;
	}

    /**
     *  Creates a directory and returns it's File.
     */
    static File createDir(String path) {
        File dir = new File(path);
        if( dir.exists() ){
            if( dir.isDirectory() ) return dir;
            else throw new RuntimeException("Already exists, but not a dir: " + path);
        }
        if( ! dir.mkdirs() )
            throw new RuntimeException("Failed creating dir: " + path);
        return dir;
    }

    
    
    /**
     * Reads with Dom4j, then returns the source.
     * 
     */
    private static String normalizeHTML(String htmlStr) {
        
        // Read.
        final DocumentFactory df = DOMDocumentFactory.getInstance();
        SAXReader reader = new SAXReader();
        
        // To prevent "The entity ”nbsp“ was referenced, but not declared.".
        // See http://evc-cit.info/dom4j/dom4j_groovy.html
        // See http://validator.w3.org/sgml-lib.tar.gz
        // See also /CatalogManager.properties. 
        //reader.setEntityResolver( new CatalogResolver() ); // Removed - overkill.  And, never called if no DOCTYPE decl.

        // Resorted to this hack until I find a programmatical way.
        htmlStr = "<!DOCTYPE html [ <!ENTITY nbsp \"&#xA0;\"><!ENTITY ndash \"&#x2013;\"><!ENTITY mdash \"&#x2014;\"> ]>\n" + htmlStr;
        
        Document doc, outDoc;
        try {
            doc = reader.read( new StringReader(htmlStr) );
        }
        catch( Exception ex ){
            throw new RuntimeException("Error parsing the HTML:\n       " + ex.toString() );
        }
        
        // Write.
        OutputFormat format = OutputFormat.createPrettyPrint();
        
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream( sw );
        try {
            //XMLWriter writer = new XMLWriter(wos, format);
            HTMLWriter writer = new HTMLWriter(wos, format);
            writer.write(doc);
            writer.flush();
            String refStrNormalized = sw.toString();
        }
        catch( Exception ex ){
            throw new RuntimeException("Error writing the HTML:\n       " + ex.toString() );
        }
        
        return sw.toString();
    }// normalizeHTML()

    
}
