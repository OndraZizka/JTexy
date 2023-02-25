
package cz.dynawest.jtexy.util;

import cz.dynawest.openjdkregex.Matcher;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ondrej Zizka
 */
public class MatchWithOffset
{

	public final String match;
	public final int offset;


	/** Const */
	public MatchWithOffset(String match, int offset) {
		this.match = match;
		this.offset = offset;
	}




	/** Get list of match groups after successful match. */
	public static List<MatchWithOffset> fromMatcherState( Matcher matcher ){
		//if( matcher.groupCount() == 0 )
		//	return Collections.EMPTY_LIST;

		List<MatchWithOffset> groups = new ArrayList( matcher.groupCount() + 1 );
		for (int i = 0; i <= matcher.groupCount(); i++) {
			groups.add( new MatchWithOffset( matcher.group(i), matcher.start(i) ));
		}

		return groups;
	}



	/** Get list of lists of groups of all matches. */
	public static List<List<MatchWithOffset>> fromMatcherAll( Matcher matcher )
	{

		// If not matched, return empty list.
		if( ! matcher.find() )
			return Collections.EMPTY_LIST;

		// Else loop trough the matches and groups and build 2-dim array.
		List<List<MatchWithOffset>> matches = new ArrayList();
		do{
			List<MatchWithOffset> groups = new ArrayList();
			for( int i = 0; i < matcher.groupCount()+1; i++ ) {
				groups.add( new MatchWithOffset( matcher.group(i), matcher.start(i) ));
			}
			matches.add(groups);
		}while( matcher.find() );

		return matches;
	}


	
	/** Returns "offset: match". */
	public String toString(){
		String str = StringUtils.abbreviate(match, 25);
		//str = StringEscapeUtils.escapeJava(str); // Converts diacritics to \u00E1 etc.
        str = StringUtils.replaceEach( str, new String[]{"\r","\n","\t"}, new String[]{"\\r","\\n","\\t"} );
		StringBuilder sb = new StringBuilder("@").append(this.offset).append(": ");
        if( null == str ) return sb.append("nul").toString();
        else              return sb.append('"').append(str).append('"').toString();
	}

	

}
