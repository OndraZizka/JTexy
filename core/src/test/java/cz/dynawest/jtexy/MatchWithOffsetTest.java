
package cz.dynawest.jtexy;

import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.junit.VerboseTestBase;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Zizka
 */
public class MatchWithOffsetTest extends VerboseTestBase
{
  private static final Logger log = Logger.getLogger( MatchWithOffsetTest.class.getName() );

	public void testMatchWithOffset(){

		String testStr = "Ahoj 123abc ahoj 567def ahoj.";
		String regex = "([0-9]+)([a-z]+)";
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(testStr);


		log.info("Matching: "+testStr );

		List<List<MatchWithOffset>> fromMatcherAll = MatchWithOffset.fromMatcherAll(mat);

		for( List<MatchWithOffset> group : fromMatcherAll ) {
			log.info("  Group: " + group.size() );
			for (MatchWithOffset match : group) {
				log.info("    Match: " + match.offset +": "+match.match );
			}
		}

	}

}// class
