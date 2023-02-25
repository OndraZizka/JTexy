package cz.dynawest.jtexy;

import cz.dynawest.jtexy.util.Levenshtein;
import cz.dynawest.junit.VerboseTestBase;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *  @see http://texy.info/cs/try
 */
public class ParsingSmokeTest extends VerboseTestBase
{
	//public InitTest( String testName ){   super( testName );  }
	public static Test suite(){ return new TestSuite( ParsingSmokeTest.class ); }



	/**
	 *   Init JTexy - load the .properties, parse the regexps... etc.
	 */
	public void xtestJTexyParsingSmokeTest() throws TexyException
	{
		final String SOURCE = "Beží **Žižka** k //Táboru//, nese `pytel` zázvoru.";
		final String EXPECTED = "<p>Beží <strong>Žižka</strong> k&nbsp;<em>Táboru</em>, nese <code>pytel</code> zázvoru.</p>";

		JTexy jtexy = JTexy.create();
		String res = jtexy.process( SOURCE );
		assertEquals( "Result not as expected.", EXPECTED, res );
	}


	/**
	 * Block parsing test.
	 */
	public void xtestBlockParsingSmokeTest() throws TexyException
	{
		final String SOURCE = "\nNadpis\n========\n\nOdstavec\nse dvěma řádkami.\n\n";
		final String EXPECTED = "<h1>Nadpis</h1><p>Odstavec se dvěma řádkami.</p>";

		JTexy jtexy = JTexy.create();
		String res = jtexy.process( SOURCE );
		assertEquals( "Result not as expected.", EXPECTED, res );
	}


	/**
	 * Block parsing test.
	 */
	public void testAnotherBraveSmokeTest() throws TexyException
	{
		final String SOURCE = "Title  .[main-title]\n*****\n\n" +
						"Hello //world!//\nHow are you?\n I'm fine." +
						" Look at my \"blog\":http://www.blog.cz/. And my photo: [* img/me.png 80x120 *]\n\n" +
						"Subtitle\n========\n\n/--code java\nSystem.out.println(\"That's all.\");\n\\--\n";
		final String EXPECTED = "<h1>Title<h1>\n\n" +
						"<p>Hello <em>world!</em> How are you?\n  <br/>I'm fine." +
						" Look at my <a href=\"http://www.blog.cz/\">blog</a>. " +
						"And my photo: <img src=\"img/me.png\" width=\"80\" height=\"120\" /></p>\n\n" +
						"<h2>Subtitle<h2>\n\n" +
						"<pre class=\"java\"><code>System.out.println(\"That's all.\");\n" +
						"</code></pre>";

		JTexy jtexy = JTexy.create();
		String res = jtexy.process( SOURCE );
		int distance = Levenshtein.distance(EXPECTED, res);
		int distanceNoLines = Levenshtein.distance(EXPECTED.replace("\n", ""), res);
		assertEquals( "Result not as expected. Levenshtein: "
						+distance+"("+distanceNoLines+")/"+EXPECTED.length()+", ",   EXPECTED, res );
	}




}
