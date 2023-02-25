
package cz.dynawest.jtexy;

import cz.dynawest.junit.VerboseTestBase;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;

/**
 *
 * @author Ondrej Zizka
 */
public class RegexTest extends VerboseTestBase {

	public void testMatches(){

		final String content = "abcdABCDabcd";
		final String RE_POSITIVE = "(?u)[A-Z]";
		final String RE_NEGATIVE = "(?u)[^A-Z]";

		{
			Pattern pat = Pattern.compile(RE_POSITIVE);
			Matcher mat = pat.matcher(content);
			assertTrue( mat.find() );
		}

		{
			Pattern pat = Pattern.compile(RE_NEGATIVE);
			Matcher mat = pat.matcher(content);
			assertTrue( mat.find() );
		}
		
		//assertTrue( content.matches(RE_POSITIVE) );
		//assertTrue( content.matches(RE_NEGATIVE) );


	}

}
