
package cz.dynawest.jtexy.util;


import cz.dynawest.jtexy.RegexpPatterns;
import java.util.logging.*;


/**
 *
 * @author Ondrej Zizka
 */
public class UrlChecker
{
  private static final Logger log = Logger.getLogger( UrlChecker.class.getName() );


	public enum Type { ANCHOR, IMAGE };


/*
 		$texy->urlSchemeFilters[Texy::FILTER_ANCHOR] = '#https?:|ftp:|mailto:#A';
		$texy->urlSchemeFilters[Texy::FILTER_IMAGE] = '#https?:#A';
*/

	protected static String getSchemeByType( Type type ){
		switch( type ){
			case ANCHOR: return /*(?A)*/ "https?:|ftp:|mailto:";
			case IMAGE:  return /*(?A)*/ "https?:";
			default: return null;
		}
	}


	/**
	 * Filters bad URLs.
	 * @param  string   user URL
	 * @param  string   type: a-anchor, i-image, c-cite
	 */
	public static boolean checkURL( String url, Type type )
	{
		String scheme = getSchemeByType(type);

		// Absolute URL with scheme? Check against the scheme!
		if( null != scheme 
			&& url.matches( /*"(?A)"*/ "^"+RegexpPatterns.TEXY_URLSCHEME )
			&& ! url.matches( scheme )
		)
			return false;
		
		else
			return true;
	}




}// class UrlChecker
