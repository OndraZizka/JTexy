
package cz.dynawest.jtexy.util;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.Protector;
import cz.dynawest.jtexy.RegexpPatterns;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.HTMLWriter;
import org.jsoup.Jsoup;

/**
 *
 * @author Ondrej Zizka
 */
public final class JTexyStringUtils {
    private static final Logger log = Logger.getLogger( JTexyStringUtils.class.getName() );

    // IndexOfAny chars
    //-----------------------------------------------------------------------
    /**
     * <p>Search a String to find the first index of any
     * character in the given set of characters.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> or zero length search array will return <code>-1</code>.</p>
     *
     * <pre>
     * StringUtils.indexOfAny(null, *)                = -1
     * StringUtils.indexOfAny("", *)                  = -1
     * StringUtils.indexOfAny(*, null)                = -1
     * StringUtils.indexOfAny(*, [])                  = -1
     * StringUtils.indexOfAny("zzabyycdxx",['z','a']) = 0
     * StringUtils.indexOfAny("zzabyycdxx",['b','y']) = 3
     * StringUtils.indexOfAny("aba", ['z'])           = -1
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     */
    public static int indexOfAny(String str, char[] searchChars, int offset) {
        if (StringUtils.isEmpty(str) || ArrayUtils.isEmpty(searchChars))
            return -1;
        if( offset >= str.length() )
            return -1;

        for( int i = offset; i < str.length(); i++ ) {
            char ch = str.charAt(i);
            for( int j = 0; j < searchChars.length; j++ ) {
                if( searchChars[j] == ch )
                    return i;
            }
        }
        return -1;
    }




    /** Prepends the given prefix to the given url. */
    public static final String prependUrlPrefix( String prefix, String url ){
        if( prefix == null || "".equals(prefix) || !isRelativeUrl(url))
            return url;
        return StringUtils.stripEnd(prefix, "/\\") + "/" + url;
    }

    /** Checks whether an URL is relative ( == is not absolute or a doc anchor). */
    private static boolean isRelativeUrl(String url) {
        // check for scheme, or absolute path, or absolute URL
        return !url.matches(/*(?A)*/"^"+RegexpPatterns.TEXY_URLSCHEME+"|[\\#/?]"); // (?A) in Texy
    }




    /**
     * Substitute for PHP's preg_replace_callback().
     * 
     * @deprecated  Use Matcher#appendReplacement() instead.
     */
    public static final String replaceWithCallback(
                    String str, String regexp, StringsReplaceCallback cb)
    {
        Pattern pat = Pattern.compile(regexp);
        return replaceWithCallback(str, pat, cb);
    }


    /**
     * Substitute for PHP's preg_replace_callback().
     * 
     * @deprecated  Use Matcher#appendReplacement() instead.
     */
    public static final String replaceWithCallback(
                    String str, Pattern pat, StringsReplaceCallback cb
    ){
        Matcher mat = pat.matcher(str);

        StringBuilder sb = new StringBuilder(str.length() * 11 / 10);

        int prevStart = 0;
        int prevEnd = 0;
        int offset = 0;

        while( mat.find() ){

            // Create the groups array.
            String[] groups = new String[mat.groupCount()+1];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = mat.group(i);
            }

            // Append string before match.
            sb.append( str.substring(prevEnd+offset, mat.start()+offset) );

            // Call the callback and append what it returns.
            String newStr = cb.replace( groups );
            sb.append( newStr );

            // Set the offset according to the lengths difference.
            //offset -= mat.group().length();
            prevStart = mat.start() + offset;
            prevEnd = mat.end() + offset;
        }
        sb.append( str.substring(prevEnd) );

        return sb.toString();
    }



    /**
     * Expands tabs to spaces, according to the tab position (as with typewriter tabs).
     */
    public static final String expandTabs(String text, int tabWidth) {


        StringBuilder sb = new StringBuilder( text.length() * 108 / 100 );
        int lastLineBreak = -1; // "Previous line"
        int nextLineBreak = 0;
        int lastTab = 0;
        int nextTab = text.indexOf('\t');
        int charsAddedForThisLine = 0;

        while( nextTab != -1 ){

            // Append everything from the last tab to this one.
            sb.append( text.substring( lastTab, nextTab ) );

            // After this, lastLB will have the beginning of the line with the next tab,
            // and nextLB the end of it.
            while ( nextTab > nextLineBreak ){
                charsAddedForThisLine = 0; // We're going to some other line - reset.
                lastLineBreak = nextLineBreak;
                nextLineBreak = text.indexOf('\n');
                if( nextLineBreak == -1 )
                    nextLineBreak = text.length();
            }

            // Append number of spaces according to the tab's position in the line.
            int tabPos = nextTab - lastLineBreak - 1;
            //return $m[1] . str_repeat(' ', $this->tabWidth - strlen($m[1]) % $this->tabWidth);
            int repeats = tabWidth - (tabPos % tabWidth);
            sb.append( repeatSpaces( repeats ) );
            charsAddedForThisLine += tabWidth - 1;

            //nextLineBreak = text.indexOf('\n', nextTab);
            lastTab = nextTab;
            nextTab = text.indexOf('\t', nextTab);
        }

        sb.append( text.substring( lastTab, text.length() ) );

        return sb.toString();

    }


    /**  Optimization */
    public static final String repeatSpaces( int repeats ){
            String spaces;
            switch( repeats ){
                case 0: spaces = ""; break;
                case 1: spaces = " "; break;
                case 2: spaces = "  "; break;
                case 3: spaces = "   "; break;
                case 4: spaces = "    "; break;
                case 5: spaces = "     "; break;
                case 6: spaces = "      "; break;
                case 7: spaces = "       "; break;
                default: spaces = StringUtils.repeat(" ", repeats);
            }
            return spaces;
    }

    public static final String repeatTabs( int repeats ){
            String spaces = null;
            if( repeats <= 10 ){
                switch( repeats ){
                    case 0: spaces = ""; break;
                    case 1: spaces = "\t"; break;
                    case 2: spaces = "\t\t"; break;
                    case 3: spaces = "\t\t\t"; break;
                    case 4: spaces = "\t\t\t\t"; break;
                    case 5: spaces = "\t\t\t\t\t"; break;
                    case 6: spaces = "\t\t\t\t\t\t"; break;
                    case 7: spaces = "\t\t\t\t\t\t\t"; break;
                    case 8: spaces = "\t\t\t\t\t\t\t\t"; break;
                    case 9: spaces = "\t\t\t\t\t\t\t\t\t"; break;
                    case 10: spaces = "\t\t\t\t\t\t\t\t\t\t"; break;
                }
                return spaces;
            }
            if( repeats <= TABS.length() )   // 50.
                return TABS.substring(repeats);
            
            return spaces = StringUtils.repeat(" ", repeats);
    }

    public static final String TABS = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";


    /**
     * Translate all white spaces (\t \n \r space) to meta-spaces \x01-\x04.
     * which are ignored by TexyHtmlOutputModule routine
     * @param  string
     * @return string
     */
    public static final String freezeSpaces( String str )
    {
        return StringUtils.replaceChars( str, " \t\r\n", "\u0001\u0002\u0003\u0004" );
    }



    /**
     * Reverts meta-spaces back to normal spaces.
     * @param  string
     * @return string
     */
    public static final String  unfreezeSpaces( String str )
    {
        return StringUtils.replaceChars( str, "\u0001\u0002\u0003\u0004", " \t\r\n" );
    }



    /**  Removes indentation, up to the numer of spaces on the first line. (Tabs are converted before.)  */
    public static String outdent(String str) {
        str = StringUtils.strip(str, "\n");
        int numSpaces = StringUtils.indexOfAnyBut(str, " ");
        if( 0 < numSpaces )  return str.replaceAll("(?m)^ {1,"+numSpaces+"}", "");
        return str;
    }


    /** @deprecated to see where it is used. Try Dom4jUtils. */
    public static String elementToHTML( DOMElement elm, JTexy texy ) throws TexyException {
        StringWriter sw = new StringWriter();
        HTMLWriter hw = new HTMLWriter( sw );
        try {
            hw.write(elm);
        } catch (IOException ex) {
            throw new TexyException(ex);
        }
        return sw.toString();
    }


    /** Converts < > & to the HTML entities. Not quotes (as in TexyHTML, assuming there is a reason). */
    public static String escapeHtml(String str) {
        return StringUtils.replaceEach(str, new String[]{"<",">","&"}, new String[]{"&lt;","&gt;","&amp;"});
    }

    /** Converts HTML entities to < > & . */
    public static String unescapeHtml(String str) {
        return StringUtils.replaceEach(str, new String[]{"&lt;","&gt;","&amp;"}, new String[]{"<",">","&"});
    }



    /**
     * Converts given internal Texy string into plain text - unprotects & strips HTML.
     * 
     * TODO:  @ Texy::stringToText(){}. Perhaps already coded somewhere.
     */
    public static String stringToText(String str, Protector protector) throws TexyException {
        if( null != protector )
            str = protector.unprotectAll(str);
        return stripHtmlTags(str);
    }
    
    public static String stripHtmlTags( String str ){
        return Jsoup.parse(str).text();
    }


    /**
     * Encodes the URL, using UTF-8.
     * @throws UnsupportedOperationException  Wraps UnsupportedEncodingException.
     */
    public static String encodeUrl( String res ) {
        try {
            return URLEncoder.encode(res, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }


    /**
     * Removes special controls characters and normalizes line endings and spaces.
     */
    public static String normalize(String s) {
        // Standardize line endings to unix-like.
        s = s.replace("\r\n", "\n"); // DOS
        s = s.replace('\r', '\n');   // Mac

        // Remove special chars; leave \t + \n.
        s = StringUtils.replaceChars( s, 
            "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008" +
            "\u000B\u000C\u000E\u000F", "");

        // The following allows to leave intentional trailing whitespace
        // with:  "end of text.  \n"

        // Right trim.
        s = StringUtils.stripEnd(s, "\t ");

        // Trailing spaces.
        s = StringUtils.strip(s, "\n");

        return s;
    }



    /**
     *  Converts given string to something usable in URL, ID, etc.
     *  1) Shortens to 50 chars.
     *  2) Removes diacritics
     *  3) Strips HTML tags.
     */
    public static String webalize( String str ) {

        if( null == str )
            return "";

        str = StringUtils.substring(str, 0, 50).toLowerCase();
        str = removeDiacritics(str);
        str = stripHtmlTags(str);
        
        //log.finest("Webalizing: " + Debug.showCodes(str));

        int len = str.length();
        if( len == 0 )
            return "";

        char[] chars = new char[len];
        char[] chars2 = new char[len];
        str.getChars(0, len, chars, 0);

        int pos = 0;
        int lastDash = -1;
        //int firstDash = 0;

        // Replace non-alnum with dash, no dash sequences.
        for( int i = 0; i < chars.length; i++ ) {

            char ch = chars[i];
            if( Character.isLetterOrDigit(ch) ){
                chars2[pos++] = ch;
                lastDash = -1; // Last char is not a dash.
                //if( firstDash == 0 )
                //	firstDash = -1;
            }
            else if( -1 == lastDash ) {
                chars2[pos] = '-';
                lastDash = pos++; // Last char is a dash.
                //if( firstDash != -1 )
                //	firstDash++;
            }

        }// for each char.

        if( -1 != lastDash )
            pos = lastDash;

        //if( firstDash == pos )
        //	return "";

        String res = new String( chars2, /*firstDash*/ 0, pos );
        res = StringUtils.stripStart(res, "-");
        return res;

    }// webalize()

    
    
    /**
     *   Removes diacritics.
     */
    private static String removeDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIA.matcher(str).replaceAll("");
        return str;
    }

    private static final Pattern DIA = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");


}
