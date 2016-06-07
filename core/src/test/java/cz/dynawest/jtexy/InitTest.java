package cz.dynawest.jtexy;

import cz.dynawest.junit.VerboseTestBase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 */
public class InitTest extends VerboseTestBase
{
   //public InitTest( String testName ){   super( testName );  }

	 public static Test suite() { return new TestSuite(InitTest.class); }



   /**
    *   Init JTexy - load the .properties, parse the regexps... etc.
    */
   public void testJTexyInitialization() throws TexyException
	 {
	    JTexy jtexy = JTexy.create();
   }
}
