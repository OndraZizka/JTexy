package cz.dynawest.jtexy

import cz.dynawest.jtexy.modules.PatternHandler
import cz.dynawest.openjdkregex.Pattern

/**
 * Regexp info holder.
 *
 * @author Ondrej Zizka
 */
class RegexpInfo(var name: String, type: Type) {
    var perlRegexp: String? = null
        private set
    var regexp: String? = null
        private set
    var flags: String? = null
    var htmlElement: String? = null
    lateinit var pattern: Pattern
        private set
    /** Optional, e.g. in TypographyModule.  */
    var replacement: String? = null

    // Handler of this pattern - a "callback".
    var handler: PatternHandler? = null

    /**   */
    override fun toString(): String {
        return "RegexpInfo $name $type {$perlRegexp $handler }"
    }

    enum class Type {
        LINE, BLOCK, POST_LINE // Not involved in parsing. TypographyModule, LongWordsModule.
    }

    var type = Type.LINE

    enum class Flags(var flagChar: Char) {
        CANON_EQ('-'), CASE_INSENSITIVE('i'), COMMENTS('x'), DOTALL('s'), LITERAL('-'), MULTILINE('m'), UNICODE_CASE('u'), UNIX_LINES('d'), UNGREEDY('U');

        companion object {
            fun byChar(ch: Char): Flags? {
                return when (ch) {
                    'i' -> CASE_INSENSITIVE
                    'x' -> COMMENTS
                    's' -> DOTALL
                    'm' -> MULTILINE
                    'u' -> UNICODE_CASE
                    'd' -> UNIX_LINES
                    'U' -> UNGREEDY
                    else -> null
                }
            }
        }
    }

    //public List<Flags> parsedFlags;
    var parsedFlags: String? = null

    /** Const  */
    init {
        this.type = type
    }

    /**
     * Replace tokens from RegexpPatterns.java and parse to Pattern and Flags.
     * Fills the values in the current object.
     *
     * @param  origStr   Ex.: #(?&lt;!\_)\_\_(?![\s_])([^\r\n]+)$TEXY_MODIFIER?(?&lt;![\s_])\_\_(?!\_)()#Uu'
     */
    @Throws(TexyException::class)
    fun parseRegexp(origStr: String) {
        perlRegexp = origStr
        try {
            val end = origStr.lastIndexOf('#') // last #
            var str = origStr.substring(1, end) // #...#
            str = str.replace("\\#", "#") // PHP's #...# syntax needs hashes in the regex escaped. Java does not.
            val regexpFlags = origStr.substring(end + 1) // after #

            //this.parsedFlags = new ArrayList(regexpFlags.length());
            //for( int i = 0; i < regexpFlags.length(); i ++ ){
            //	this.parsedFlags.add( Flags.byChar(regexpFlags.charAt(i)) );
            //}
            parsedFlags = regexpFlags


            // Be sure to replace the longer strings first!
            str = str.replace("\$TEXY_CHAR", RegexpPatterns.TEXY_CHAR)
            str = str.replace("\$TEXY_MARK", RegexpPatterns.TEXY_MARK)
            str = str.replace("\$TEXY_MODIFIER_HV", RegexpPatterns.TEXY_MODIFIER_HV)
            str = str.replace("\$TEXY_MODIFIER_H", RegexpPatterns.TEXY_MODIFIER_H)
            str = str.replace("\$TEXY_MODIFIER", RegexpPatterns.TEXY_MODIFIER)
            str = str.replace("\$TEXY_IMAGE", RegexpPatterns.TEXY_IMAGE)
            str = str.replace("\$TEXY_LINK_URL", RegexpPatterns.TEXY_LINK_URL)
            str = str.replace("\$TEXY_LINK_N", RegexpPatterns.TEXY_LINK_N)
            str = str.replace("\$TEXY_LINK", RegexpPatterns.TEXY_LINK)
            str = str.replace("\$TEXY_EMAIL", RegexpPatterns.TEXY_EMAIL)
            str = str.replace("\$TEXY_URLSCHEME", RegexpPatterns.TEXY_URLSCHEME)


            // Test-compile without flags.
            Pattern.compile(str)

            // Prepend the flags to the regex (Java style).
            str = "(?$regexpFlags)$str"

            // Test-compile with flags.
            pattern = Pattern.compile(str)
            regexp = str
        } catch (ex: Throwable) {
            throw TexyException("Error while parsing: $origStr", ex)
        }
    }

    companion object {
        /**
         */
        @Throws(TexyException::class)
        fun fromRegexp(pcreString: String, replacement: String?, name: String): RegexpInfo {
            val ri = fromRegexp(pcreString, Type.LINE, name)
            ri.replacement = replacement
            return ri
        }
        /**
         * @param  pcreString   Ex.: #(?&lt;!\_)\_\_(?![\s_])([^\r\n]+)$TEXY_MODIFIER?(?&lt;![\s_])\_\_(?!\_)()#Uu'
         * @param type BLOCK or LINE
         * @param name Regular expression's name.
         *
         * @returns  RegexpInfo parsed from given string.
         */
        /**
         * @returns this.fromRegexp(pcreString, null, Type.LINE);
         */
        @JvmOverloads
        @Throws(TexyException::class)
        fun fromRegexp(pcreString: String, type: Type = Type.LINE, name: String): RegexpInfo {
            val ri = RegexpInfo(name, type)
            ri.parseRegexp(pcreString)
            return ri
        }
    }
}
