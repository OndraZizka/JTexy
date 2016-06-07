
package cz.dynawest.jtexy;

import cz.dynawest.junit.VerboseTestBase;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;

/**
 *
 * @author Ondrej Zizka
 */
public class HeadingPatternsTest extends VerboseTestBase {

	public void testUnderlinedHeadingPattern() throws TexyException {

		// (?mU)^(\S.*)(?: *(?<= |^)\.((?:\([^)\n]+\)|\[[^\]\n]+\]|\{[^}\n]+\}|<>|>|=|<){1,4}?))?\n(\#{3,}|\*{3,}|={3,}|-{3,})$
		// Newlines in the pattern cause misinforming exception -
		// PatternSyntaxException: Dangling meta character '?' near index 0
		//String regex     = "(?mU)^(\\S.*)(?: *(?<= |^)\\.((?:\\([^)\n]+\\)|\\[[^\\]\n]+\\]|\\{[^}\n]+\\}|<>|>|=|<){1,4}?))?\n(\\#{3,}|\\*{3,}|={3,}|-{3,})$";
		String regex = "^(\\S.*)(?: *(?<= |^)\\.((?:\\([^)\\n]+\\)|\\[[^\\]\\n]+\\]|\\{[^}\\n]+\\}|<>|>|=|<){1,4}?))?\\n(\\#{3,}|\\*{3,}|={3,}|-{3,})$";
		String regexFlagged = "(?mU)"+regex;
		String regexTexy = "#"+regex+"#mU";


		String text = "Title\n*****\n\nsubtitle\n========\n\n/--texysource\n**aa**\n======\n\n**bb**\n------\n\\--\n\n\nTitle2\n******\n\nlast title\n----------";

		//Pattern pat = Pattern.compile(regexFlagged);

		RegexpInfo ri = new RegexpInfo("somename", RegexpInfo.Type.BLOCK);
		ri.parseRegexp( regexTexy );
		Pattern pat = ri.getPattern();

		Matcher mat = pat.matcher(text);

		assertTrue( "Heading should be matched.", mat.find() );

	}

}// class
