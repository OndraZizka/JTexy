package cz.dynawest.jtexy;

import cz.dynawest.jtexy.modules.PatternHandler;
import cz.dynawest.openjdkregex.Pattern;


/**
 * Regexp info holder.
 * 
 * @author Ondrej Zizka
 */
public class RegexpInfo {

    public String name;

    private String perlRegexp;
    public String getPerlRegexp() {		return perlRegexp;	}

    private String regexp;
    public String getRegexp() {		return regexp;	}

    public String flags;

    public String htmlElement;

    private Pattern pattern;
    public Pattern getPattern() {		return pattern;	}
    
    /** Optional, e.g. in TypographyModule. */
    public String getReplacement() { return replacement; }
    public void setReplacement(String replacement) { this.replacement = replacement; }
    private String replacement = null;
    
    // Handler of this pattern - a "callback".
    public PatternHandler handler;


    /**  */
    @Override public String toString(){
        return "RegexpInfo "+name+" "+type+" {"+perlRegexp+" "+handler+" }";
    }


    public enum Type { 
        LINE, 
        BLOCK, 
        POST_LINE // Not involved in parsing. TypographyModule, LongWordsModule.
    };
    public Type type = Type.LINE;

    public enum Flags {
        CANON_EQ('-'), CASE_INSENSITIVE('i'), COMMENTS('x'), DOTALL('s'), LITERAL('-'), MULTILINE('m'), UNICODE_CASE('u'), UNIX_LINES('d'), UNGREEDY('U');
        char flagChar;
        Flags( char ch ){ this.flagChar = ch; }
        public static Flags byChar( char ch ){
            switch( ch ){
                case 'i': return CASE_INSENSITIVE;
                case 'x': return COMMENTS;
                case 's': return DOTALL;
                case 'm': return MULTILINE;
                case 'u': return UNICODE_CASE;
                case 'd': return UNIX_LINES;
                case 'U': return UNGREEDY;
                default: return null;
            }
        }
    }
    //public List<Flags> parsedFlags;
    public String parsedFlags;




    /** Const */
    public RegexpInfo( String name, Type type) {
        this.name = name;
        this.type = type;
    }



    /**
     * @returns this.fromRegexp(pcreString, null, Type.LINE);
     */
    public static RegexpInfo fromRegexp( String pcreString ) throws TexyException {
        return fromRegexp(pcreString, Type.LINE, null);
    }
    /**
     */
    public static RegexpInfo fromRegexp( String pcreString, String replacement, String name ) throws TexyException {
        RegexpInfo ri = fromRegexp(pcreString, Type.LINE, name);
        ri.setReplacement(replacement);
        return ri;
    }
    
    /**
     *  @param  pcreString   Ex.: #(?&lt;!\_)\_\_(?![\s_])([^\r\n]+)$TEXY_MODIFIER?(?&lt;![\s_])\_\_(?!\_)()#Uu'
     *  @param type BLOCK or LINE
     *  @param name Regular expression's name.
     * 
     *  @returns  RegexpInfo parsed from given string.
     */
    public static RegexpInfo fromRegexp( String pcreString, RegexpInfo.Type type, String name ) throws TexyException {
        RegexpInfo ri = new RegexpInfo(name, type);
        ri.parseRegexp(pcreString);
        return ri;
    }

    /**
     * Replace tokens from RegexpPatterns.java and parse to Pattern and Flags.
     * Fills the values in the current object.
     * 
     * @param  origStr   Ex.: #(?&lt;!\_)\_\_(?![\s_])([^\r\n]+)$TEXY_MODIFIER?(?&lt;![\s_])\_\_(?!\_)()#Uu'
     */
    public void parseRegexp(String origStr) throws TexyException
    {
        this.perlRegexp = origStr;
        try {
            int end = origStr.lastIndexOf('#');                // last #
            String str =         origStr.substring(1, end);    // #...#
            str = str.replace("\\#", "#");                     // PHP's #...# syntax needs hashes in the regex escaped. Java does not.
            String regexpFlags = origStr.substring(end + 1);   // after #

            //this.parsedFlags = new ArrayList(regexpFlags.length());
            //for( int i = 0; i < regexpFlags.length(); i ++ ){
            //	this.parsedFlags.add( Flags.byChar(regexpFlags.charAt(i)) );
            //}
            this.parsedFlags = regexpFlags;


            // Be sure to replace the longer strings first!
            str = str.replace("$TEXY_CHAR",        RegexpPatterns.TEXY_CHAR);
            str = str.replace("$TEXY_MARK",        RegexpPatterns.TEXY_MARK);
            str = str.replace("$TEXY_MODIFIER_HV", RegexpPatterns.TEXY_MODIFIER_HV);
            str = str.replace("$TEXY_MODIFIER_H",  RegexpPatterns.TEXY_MODIFIER_H);
            str = str.replace("$TEXY_MODIFIER",    RegexpPatterns.TEXY_MODIFIER);
            str = str.replace("$TEXY_IMAGE",       RegexpPatterns.TEXY_IMAGE);
            str = str.replace("$TEXY_LINK_URL",    RegexpPatterns.TEXY_LINK_URL);
            str = str.replace("$TEXY_LINK_N",      RegexpPatterns.TEXY_LINK_N);
            str = str.replace("$TEXY_LINK",        RegexpPatterns.TEXY_LINK);
            str = str.replace("$TEXY_EMAIL",       RegexpPatterns.TEXY_EMAIL);
            str = str.replace("$TEXY_URLSCHEME",   RegexpPatterns.TEXY_URLSCHEME);


            // Test-compile without flags.
            Pattern.compile(str);

            // Prepend the flags to the regex (Java style).
            str = "(?" + regexpFlags + ")" + str;

            // Test-compile with flags.
            this.pattern = Pattern.compile(str);

            this.regexp = str;


        }
        catch( Throwable ex ){
            throw new TexyException( "Error while parsing: " + origStr, ex );
        }
    }
	
}
