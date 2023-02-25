package cz.dynawest.jtexy.util;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ondrej Zizka
 */
public class JTexyStringUtilsTest extends TestCase {

	/**
	 * An alternative to preg_replace_callback().
	 */
	public void testReplaceWithCallback(){
		String res1 = JTexyStringUtils.replaceWithCallback(
						"ahoj 123 lidi65, jak se máte",
						"[0-9]+", cbReverse);
		assertEquals("Not equals: ", "ahoj 321 lidi56, jak se máte", res1);

		String res2 = JTexyStringUtils.replaceWithCallback(
						"123 malých prasátek 2010",
						"[0-9]+", cbReverse);
		assertEquals("Not equals: ", "321 malých prasátek 0102", res2);
	}



	private static final StringsReplaceCallback cbReverse
					= new StringsReplaceCallback()
	{
		@Override public String replace(String[] groups ) {
			//return "("+StringUtils.reverse( groups[0] )+")";
			return StringUtils.reverse( groups[0] );
		}
	};


	/**
	 * webalize() - for URLs, IDs etc.
	 */
	public void testWebalize(){
		assertEquals( "", JTexyStringUtils.webalize(null) );
		assertEquals( "", JTexyStringUtils.webalize("") );
		assertEquals( "", JTexyStringUtils.webalize("-") );
		assertEquals( "", JTexyStringUtils.webalize("--") );
		assertEquals( "a", JTexyStringUtils.webalize("-a-") );
		assertEquals( "b", JTexyStringUtils.webalize("--b--") );
		assertEquals( "c", JTexyStringUtils.webalize("#$@--c-!$-!$") );
		assertEquals( "d", JTexyStringUtils.webalize("#$@--d-!$-!$") );
		assertEquals( "b-a-c", JTexyStringUtils.webalize("b#$@--a-!$-!$C") );
		assertEquals( "b-a-d", JTexyStringUtils.webalize("$b#$@--a-!$-!$D@#%%") );
		assertEquals( "b-c", JTexyStringUtils.webalize("$b#$@---!$-!$C@#%%") );
	}

}

