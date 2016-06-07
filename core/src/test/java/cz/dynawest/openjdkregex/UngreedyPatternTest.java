
package cz.dynawest.openjdkregex;

/**
 *
 * @author Ondrej Zizka
 */
public class UngreedyPatternTest {


	/** Debug */
  public void testUngreedySmokeTest( String[] args ) throws Exception {

		Pattern pat = Pattern.compile("(?-U)(.*)foo", Pattern.UNGREEDY); // 2 matches
		pat = Pattern.compile("(?U)(.*)foo"); // 1 match
		pat = Pattern.compile("(?U-U)(.*)foo", Pattern.UNGREEDY); // 1 match
		pat = Pattern.compile("(.*)foo", Pattern.UNGREEDY); // 2 matches
		pat = Pattern.compile("(.*)foo"); // 1 match
		pat = Pattern.compile("(.*?)foo"); // 2 matches
		pat = Pattern.compile("(.*+)foo", Pattern.UNGREEDY); // 1 match

		Matcher mat = pat.matcher("xxxfooxxxfoo");

		while( mat.find() ){
			System.out.println(" * " + mat.group() );
		}

		if( ! mat.matches() ){
			System.out.println("No match.");
		}else{
			System.out.println("Matches: ");
			for (int i = 0; i < mat.groupCount(); i++) {
				System.out.println(" * " + mat.group(i) );
			}
		}

	}

}// class
