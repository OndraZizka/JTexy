package cz.dynawest.jtexy.ts;

import cz.dynawest.jtexy.util.TexyFileLister;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.io.File;
import java.util.List;
import java.util.logging.*;
import junit.framework.TestSuite;
import org.apache.commons.lang.StringUtils;


/**
 * This test creates a list of .texy files from the PHP Texy's "TCK" dir,
 * processes each of them in a separate testCaseNN() testcase,
 * and compares the result with the .html file from Texy's TCK.
 * Also drops the result in the dir specified by the "jtexy.ts.out.dir" system prop.
 * Also checks if all necessary files exist.
 * System props should be read only here.
 * 
 * @author Ondrej Zizka
 */
public class TexyGenericTestSuite extends TestSuite
{
    private static final Logger log = Logger.getLogger( TexyGenericTestSuite.class.getName() );

	// Set "jtexy.jtexy.ts.dir" in Maven's settings.xml - see http://code.google.com/p/jtexy/
	private static final String FILES_PATH   = System.getProperty("jtexy.ts.src.dir", "${jtexy.ts.src.dir} not set.");

    // NEXT_TO, INPUT_AND_REF_DIRS, TRIPLET, NO_REF_FILES.
	private static final Layout LAYOUT = Layout.valueOf( System.getProperty("jtexy.ts.src.dir.layout", "INPUT_AND_REF_DIRS") );

	// This will be set automatically to project's target/ts-output dir.
	private static final String OUT_PATH = System.getProperty("jtexy.ts.out.dir", "${jtexy.ts.out.dir} not set.");

    // Testsuites made of some real content do not have ref files. They serve to check for parsing errors.
    private static final boolean REF_FILES_NEEDED = "true".equals( System.getProperty("jtexy.ts.RefFilesNeeded", "false") );

    private static String TEST_MASK = StringUtils.defaultIfBlank( System.getProperty("jtexy.test"), null );

    
    
    /**
     *  Scan all files in dir given by ${jtexy.ts.src.dir} and create a testsuite based on them.
     */
    public static TestSuite suite() {
        
        log.severe("TEST MASK: " + TEST_MASK);

        // Test suite
        TestSuite testSuite = new TestSuite("Texy-TCK");
        
		File dir    = TestSuiteUtils.getAndCheckDir(FILES_PATH);
        // Where the texy files are.
        File inputDir  = dir;
        if( LAYOUT == Layout.INPUT_AND_REF_DIRS ) inputDir = TestSuiteUtils.checkDir(new File(dir, "input"));

        // Where the referential files are.
        // ERROR only if joined file mode.
        File refDir  = dir;
        if( LAYOUT == Layout.INPUT_AND_REF_DIRS ) refDir = TestSuiteUtils.checkDir(new File(dir, "ref"));

        // Where to put the results.
		File outputDir = TestSuiteUtils.createDir(OUT_PATH);

        List<File> fileList = TexyFileLister.getTexyFiles(inputDir, TEST_MASK);

        
		// For each file...
        for( File srcFile : fileList ) {
            
            // Skip tests not complying to filter.
            if( null != TEST_MASK )
                if( ! matchesWildCards( TEST_MASK, srcFile.getAbsolutePath() ) )
                    continue;
            
            TexyTestCaseInfo tcInfo;
            
            // PHP Texy's "TCK" comes in form of single file with 3 parts separated by =============.
            if( Layout.TRIPLET.equals( LAYOUT ) ){
                // Triplet file.
                tcInfo = TexyTestCaseInfo.fromTripletFile(refDir, outputDir);
            }
            else {
                // Separate files.
                String resFileName = StringUtils.substringBeforeLast( srcFile.getName(), ".") + ".html";
                File outFile = new File(outputDir, resFileName);

                // Prefer a .html file next to .texy file.
                File refFile = new File(inputDir, resFileName);
                if( !refFile.exists() )   // If not there, try the ref dir.
                    refFile = new File(refDir,   resFileName);
                if( !refFile.exists() )
                    refFile = null;
                tcInfo = new TexyTestCaseInfo(srcFile, outFile, refFile);
            }

            // TestSuite with name and Test object
            TestSuite ts = new TestSuite(srcFile.getName());
            ts.addTest( new GenericTexyTest(tcInfo) );
            testSuite.addTest( ts );
		}
        
        return testSuite;
    }// suite()

    
    
    
    
    /**
     *   Matches the pattern like *foo*
     */
    private static boolean matchesWildCards(String pattern, String absolutePath) {
        pattern = pattern.replace(".", "\\.");
        pattern = pattern.replace("*", ".*");
        pattern = pattern.replace("?", ".");
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(absolutePath);
        return mat.matches();
    }

    /**
     *  Layout of the testsuite directory.
     *  NEXT_TO             .html next to .texy.
     *
     *  INPUT_AND_REF_DIRS  input/foo.texy and ref/foo.html
     *
     *  TRIPLET             .html file with three parts separated by ===== (PHP Texy testsuite has it that way).
     *
     *  NO_REF_FILES        Testsuites made of some real content do not have ref files. They serve to check for parsing errors.
     */
    enum Layout {
        NEXT_TO, INPUT_AND_REF_DIRS, TRIPLET, NO_REF_FILES
    }

}// class
