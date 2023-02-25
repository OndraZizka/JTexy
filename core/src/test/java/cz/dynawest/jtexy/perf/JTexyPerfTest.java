
package cz.dynawest.jtexy.perf;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.ts.TestSuiteUtils;
import cz.dynawest.junit.VerboseTestBase;
import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Simple performance test. To be extended.
 * @author Ondrej Zizka
 */
public class JTexyPerfTest extends VerboseTestBase
{
	private static final Logger log = Logger.getLogger( JTexyPerfTest.class.getName() );


	// Set "jtexy.jtexy.ts.dir" in Maven's settings.xml - see http://code.google.com/p/jtexy/
	private static final String FILES_PATH = System.getProperty("jtexy.ts.dir");

	// This will be set automatically to project's target/ts-output dir.
	private static final String OUT_PATH = System.getProperty("jtexy.ts.out.dir");


	static File dir;
	static File inputDir;

	static {
		init();
	}

	/**
	 * Checks the directories n' stuff.
	 */
	private static void init() {
		dir = TestSuiteUtils.getAndCheckDir(FILES_PATH+"");
		inputDir = TestSuiteUtils.getAndCheckDir(FILES_PATH+"/input");
	}



	public void testPerformance() throws IOException, TexyException {

		String fileName = "smoke.texy";
		final int repeats = 10000;

		


		String content = FileUtils.readFileToString( new File(inputDir.getPath()+"/"+fileName) );
		JTexy texy = JTexy.create();


		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();

		// Turn logging off...
		for( Handler handler : handlers )
			rootLogger.removeHandler(handler);
		Logger.getLogger("").setLevel(Level.OFF);

		// Do the performance test.
		long timeStart = System.currentTimeMillis();
		for( int i = 0; i < repeats; i++ ){
			texy.process( content );
		}
		long time = System.currentTimeMillis() - timeStart;
		
		// Turn logging on.
		for( Handler handler : handlers )
			rootLogger.addHandler(handler);
		Logger.getLogger("").setLevel(Level.ALL);

		log.info("Processing "+fileName+" "+repeats+"x took "+time+" ms.");
		System.out.println("Processing "+fileName+" "+repeats+"x took "+time+" ms.");
	}



}
