
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.RegexpPatterns;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.BeforeParseEvent;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Node;
import org.dom4j.dom.DOMText;


/**
 * Typography replacements module; 
 * Works by simply replacing regex matches at the end of LINE parsing.
 *
 * @author  Ondrej Zizka
 * @author  David Grudl
 */
public class TypographyModule extends TexyModule
{
    private static final Logger log = Logger.getLogger( TypographyModule.class.getName() );

    
    // "Register" listeners.
    @Override public TexyEventListener[] getEventListeners() {
        return new TexyEventListener[]{
            
            // BeforeParseEvent. (Currently, it re-configures quotes based on locale setting.)
            new TexyEventListener<BeforeParseEvent>() {
                @Override public Class getEventClass() { return BeforeParseEvent.class; }
                @Override public Node onEvent(BeforeParseEvent event) throws TexyException {
                    // TODO: Reconfigure quotes.
                    return null;
                }
            },
            
            // PostLineEvent.
            new TexyEventListener<AfterLineEvent>() {
                @Override public Class getEventClass() { return AfterLineEvent.class; }
                @Override public Node onEvent(AfterLineEvent event) throws TexyException {
                    return new DOMText( TypographyModule.this.performReplaces(event.getText()) );
                }
            }
        };
    }
    

    // TypographyModule's patterns are not scanned for during parsing.
    @Override protected PatternHandler getPatternHandlerByName(String name) {
        return null;
    }
    

    
    // Config
    
    /**
     *  Conversions - regexps and replacements for them.
     */
    private List<RegexpInfo> conversions = new ArrayList(40);
    
    
	/**
     *  @see <a href="http://www.unicode.org/cldr/data/charts/by_type/misc.delimiters.html">Unicode</a>
     */
	public static final Map<String, LocaleInfo> LOCALES = new HashMap();


    // Options
    
	public String localeKey = "cs";

	

    // Const
	public TypographyModule()
	{
		// @ $texy->registerPostLine(array($this, "postLine"), "typography");
        // This is implemented by AfterLineEvent.
        
		// @ $texy->addHandler("beforeParse", array($this, "beforeParse"));
        // This is implemented by BeforeParseEvent.
	}



    // Init.
    @Override protected synchronized void loadRegexFromPropertiesFile(String propsFilePath) throws TexyException {
        // TODO: Move regexps to a properties file; read it, init conversions.

        // CONTENT_MARKUP mark:   \x17-\x1F
		// CONTENT_REPLACED mark: \x16
		// CONTENT_TEXTUAL mark:  \x17
		// CONTENT_BLOCK: not used in postLine

        LocaleInfo loc = LOCALES.get(this.localeKey);
        if( null == loc )
            loc = LOCALES.get("en");


        // TODO: Convert from UTF-8 to UNICODE-16
        // http://www.utf8-chartable.de/unicode-utf8-table.pl?number=1024
        // http://www.utf8-chartable.de/unicode-utf8-table.pl?start=8192&number=1024
        
        this.conversions.add( tryCreateRegexpInfo("#(?<![.\u2026])\\.{3,4}(?![.\u2026])#mu",        "\u2026",               "ellipsis  ..." ));
        this.conversions.add( tryCreateRegexpInfo("#(?<=[\\d ]|^)-(?=[\\d ]|$)#",                   "\u2013",               "en dash 123-123"));
        this.conversions.add( tryCreateRegexpInfo("#(?<=[^!*+,/:;<=>@\\\\_|-])--(?=[^!*+,/:;<=>@\\\\_|-])#", "\u2013",      "en dash alphanum--alphanum"));
        this.conversions.add( tryCreateRegexpInfo("#,-#",                                           ",\u2013",              "en dash ,-"));
        this.conversions.add( tryCreateRegexpInfo("#(?<!\\d)(\\d{1,2}\\.) (\\d{1,2}\\.) (\\d\\d)#", "$1\u00A0$2\u00A0$3",   "date 23. 1. 1978"));
        this.conversions.add( tryCreateRegexpInfo("#(?<!\\d)(\\d{1,2}\\.) (\\d{1,2}\\.)#",          "$1\u00A0$2",                "date 23. 1."));
        this.conversions.add( tryCreateRegexpInfo("# --- #",                                        "\u00A0\u2014 ",        "em dash ---"));
        this.conversions.add( tryCreateRegexpInfo("# ([\u2013\u2014])#u",                           "\u00A0$1",             "&nbsp; after dash (dash stays at line end)"));
        this.conversions.add( tryCreateRegexpInfo("# <-{1,2}> #",                                   " \u2194 ",             "left right arrow <-->"));
        this.conversions.add( tryCreateRegexpInfo("#-{1,}> #",                                      " \u2192 ",             "right arrow -->"));
        this.conversions.add( tryCreateRegexpInfo("# <-{1,}#",                                      " \u2190 ",             "left arrow <--"));
        this.conversions.add( tryCreateRegexpInfo("#={1,}> #",                                      " \u2192 ",             "right arrow ==>"));
        this.conversions.add( tryCreateRegexpInfo("#\\+-#",                                         "\u00b1",               "+-"));
        this.conversions.add( tryCreateRegexpInfo("#(\\d+)( ?)x\\2(?=\\d)#",                        "$1\u0097",             "dimension sign 123 x 123..."));
        this.conversions.add( tryCreateRegexpInfo("#(?<=\\d)x(?= |,|.|$)#m",                        "\u0097",               "dimension sign 123x"));
        this.conversions.add( tryCreateRegexpInfo("#(\\S ?)\\(TM\\)#i",                             "$1\u2122",             "trademark (TM)"));
        this.conversions.add( tryCreateRegexpInfo("#(\\S ?)\\(R\\)#i",                              "$1\u00ae",             "registered (R)"));
        this.conversions.add( tryCreateRegexpInfo("#\\(C\\)( ?\\S)#i",                              "\u00a9$1",             "copyright (C)"));
        this.conversions.add( tryCreateRegexpInfo("#\\(EUR\\)#",                                    "\u20AC",               "Euro (EUR)"));
        this.conversions.add( tryCreateRegexpInfo("#(\\d{1,3}) (?=\\d{3})#",                        "$1\u00A0",             "(phone) number 1 123 123 123..."));

        this.conversions.add( tryCreateRegexpInfo("#(?<=[^\\s\u0017])\\s+([\u0017-\u001F]+)(?=\\s)#u",  "$1",               "Remove intermarkup space, phase 1"));
        this.conversions.add( tryCreateRegexpInfo("#(?<=\\s)([\u0017-\u001F]+)\\s+#u",                  "$1",               "Remove intermarkup space, phase 2"));

        this.conversions.add( tryCreateRegexpInfo("#(?<=.{50})\\s+(?=[\u0017-\u001F]*\\S{1,6}[\u0017-\u001F]*$)#us", "\u00A0",  "nbsp before last short word"));

        // &nbsp; space between numbers (optionally followed by dot) and word, symbol, punctation, currency symbol.
        this.conversions.add( tryCreateRegexpInfo("#(?<=^| |\\.|,|-|\\+|\u0016|\\()([\u0017-\u001F]*\\d+\\.?[\u0017-\u001F]*)\\s+(?=[\u0017-\u001F]*[%"
                + RegexpPatterns.TEXY_CHAR + "\u00b0-\u00be\u2020-\u214f])#mu",                     "$1\u00A0", "&nbsp; between numbers"));

        // Space between preposition and word.
        this.conversions.add( tryCreateRegexpInfo("#(?<=^|[^0-9" + RegexpPatterns.TEXY_CHAR + "])([\u0017-\u001F]*[ksvzouiKSVZOUIA][\u0017-\u001F]*)\\s+(?=[\u0017-\u001F]*[0-9"
                + RegexpPatterns.TEXY_CHAR + "])#mus",                                              "$1\u00A0", "space after preposition"));

        // Double and single ""
        // TODO: Escape with 0x18 or something, then replace in performReplaces(). Then make this.conversions static final.
        this.conversions.add( tryCreateRegexpInfo("#(?<!\"|\\w)\"(?!\\ |\")(.+)(?<!\\ |\")\"(?!\")()#U",  
                loc.getDoubleQuotes().getA() + "$1" + loc.getDoubleQuotes().getB(), "Double \"" ));
        this.conversions.add( tryCreateRegexpInfo("#(?<!\"|\\w)\"(?!\\ |\")(.+)(?<!\\ |\")\"(?!\")()#Uu", 
                loc.getDoubleQuotes().getA() + "$1" + loc.getDoubleQuotes().getB(), "Single \""  ));

	}


    
    /**
     * Performs the regex replacement for each conversion.
     */
    private String performReplaces(String text) {
        // For each conversion...
        for( RegexpInfo ri : this.conversions ) {
            if( ri == null )   continue;
            text = ri.getPattern().matcher(text).replaceAll(ri.getReplacement());
        }
        return text;
    }
    
    
    // Locales static init
    static {
    
		LOCALES.put("cs", new LocaleInfo(
			new Pair("\u201A", "\u2018"), // "\xe2\x80\x9a", "\xe2\x80\x98"
			new Pair("\u201E", "\u201C")  // "\xe2\x80\x9e", "\xe2\x80\x9c"
		));

		LOCALES.put("en", new LocaleInfo(
			new Pair("\u2018", "\u2019"), // "\xe2\x80\x98", "\xe2\x80\x99"
			new Pair("\u201C", "\u201D")  // "\xe2\x80\x9c", "\xe2\x80\x9d"
		));

		LOCALES.put("fr", new LocaleInfo(
			new Pair("\u2039", "\u203A"), // "\xe2\x80\xb9", "\xe2\x80\xba"
			new Pair("\u00AB", "\u00BB")  // "\xc2\xab",     "\xc2\xbb"
		));

		LOCALES.put("de", new LocaleInfo(
			new Pair("\u201A", "\u2018"), // "\xe2\x80\x9a", "\xe2\x80\x98"
			new Pair("\u201E", "\u201C")  // "\xe2\x80\x9e", "\xe2\x80\x9c"
		));

		LOCALES.put("pl", new LocaleInfo(
			new Pair("\u201A", "\u2019"), // "\xe2\x80\x9a", "\xe2\x80\x99"
			new Pair("\u201E", "\u201D")  // "\xe2\x80\x9e", "\xe2\x80\x9d"
		));

    }

    
    /**
     *  Helper method to catch exception and log about it - to make list of conversions concise.
     */
    private static RegexpInfo tryCreateRegexpInfo(String regex, String replacement, String name) {
        try {
            return RegexpInfo.fromRegexp(regex, replacement, name);
        } catch (TexyException ex) {
            log.log(Level.SEVERE, "Error parsing regex:\n    {0}\n    {1}", new Object[]{regex, ex.toString()});
            return null;
        }
    }
    

}// class



class LocaleInfo {
    private Pair<String, String> singleQuotes;
    private Pair<String, String> doubleQuotes;

    public LocaleInfo(Pair<String, String> singleQuotes, Pair<String, String> doubleQuotes) {
        this.singleQuotes = singleQuotes;
        this.doubleQuotes = doubleQuotes;
    }

    public Pair<String, String> getSingleQuotes() { return singleQuotes; }
    public Pair<String, String> getDoubleQuotes() { return doubleQuotes; }
}


// TODO: Once switched to commons-lang 3.0, use it's Pair.
class Pair<A,B> {
    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {return a; }
    public void setA(A a) { this.a = a; } 
    public B getB() { return b; }
    public void setB(B b) { this.b = b; }
}
