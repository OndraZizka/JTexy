package cz.dynawest.jtexy;

import cz.dynawest.jtexy.util.JTexyStringUtils;
import org.apache.commons.lang.StringUtils;


/**
 * Interface of protector - a key-value storage for strings.
 * @author Ondrej Zizka
 */
abstract public class Protector {

    /**
     * Stores the given string and returns a key (with delimiters) under which it's stored.
     */
    abstract public String protect(String str, ContentType type);
    
    /**
     * Retrieves a string stored under given key (with delimiters).
     */
    abstract public String unprotect(String wholeKey);

    /**
     * @param key  Encoded ID, i.e. without delimiters.
     */
    //protected abstract String getStringByEncodedID( String key ); // Not used.

    public static final String CONTENT_TYPE_CHARS = "\u0014\u0015\u0016\u0017";
    

    
	/**
	 *	Unprotects all protected sub-strings in given string.
	 */
	public String unprotectAll( String str ) throws TexyException {

		StringBuilder sb = new StringBuilder();

		int nextPos = 0;
		int keyStart;

		// For each protector key found in the string...
		while( -1 != (keyStart = JTexyStringUtils.indexOfAny( str, CONTENT_TYPE_CHARS.toCharArray(), nextPos)) ){

			// Append text between this and the prev key (if any).
			if( nextPos != keyStart )
				sb.append( str.substring( nextPos, keyStart ) );

			// The key.
			char delimChar = str.charAt(keyStart);
			int keyEnd = str.indexOf( delimChar, keyStart+1 );
			if( keyEnd == -1 )
				throw new TexyException("Protector key end missing: " + StringUtils.abbreviate(str, keyStart, 20));

            // Get the string.
			//String key = str.substring( keyStart+1, keyEnd );
			//String ret = getStringByEncodedID(key); // this.safe.get(Utils.texyMarkupToInt(key));
            String wholeKey = str.substring( keyStart, keyEnd +1 );
            String ret = this.unprotect(wholeKey);
			if( null == ret )
				throw new TexyException("Protector key ["+Utils.texyMarkupToInt(wholeKey)+"] not found: "+StringUtils.abbreviate(str, keyStart, 20) );
			sb.append( ret );

			// Advance to the next position.
			nextPos = keyEnd + 1;
		}

		sb.append( str.substring(nextPos, str.length()) );

		return sb.toString();
		
	}// unprotectAll( String str )
    
    

    
    
    /**
     * Key string conversion helper methods.
     */
    public static class Utils
    {

        /**
         *  Encodes integer to a key value (without delimiters).
         */
        public static String intToKey( int val ){
            String key = Integer.toOctalString( val );
            key = StringUtils.replaceChars( key, "01234567", "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F");
            return key;
        }

        /**
         *  Translates the whole key, i.e. with content delimiters, to an integer.
         */
        public static int keyToInt( String key ){
            //key = StringUtils.strip(key, "\u0014\u0015\u0016\u0017");
            key = key.substring(1, key.length() - 1);
            return texyMarkupToInt(key);
        }

        /**
         *  Translates the encoded ID (from between delimiters) to an integer.
         */
        public static int texyMarkupToInt( String code ){
            String num = StringUtils.replaceChars( code, "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F", "01234567");
            return Integer.parseInt(num, 8);
        }
    }


}
