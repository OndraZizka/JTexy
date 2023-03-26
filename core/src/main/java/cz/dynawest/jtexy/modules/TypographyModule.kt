package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.RegexpPatterns
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.AfterLineEvent
import cz.dynawest.jtexy.events.BeforeParseEvent
import cz.dynawest.jtexy.events.TexyEventListener
import org.dom4j.Node
import org.dom4j.dom.DOMText
import java.util.logging.*

/**
 * Typography replacements module;
 * Works by simply replacing regex matches at the end of LINE parsing.
 *
 * @author  Ondrej Zizka
 * @author  David Grudl
 */
class TypographyModule  // Const
    : TexyModule() {
    override val eventListeners: Array<TexyEventListener<*>>
        // "Register" listeners.
        get() = arrayOf( // BeforeParseEvent. (Currently, it re-configures quotes based on locale setting.)
            object : TexyEventListener<BeforeParseEvent> {
                override val eventClass: Class<*>
                    get() = BeforeParseEvent::class.java

                @Throws(TexyException::class)
                override fun onEvent(event: BeforeParseEvent): Node? {
                    // TODO: Reconfigure quotes.
                    return null
                }
            },  // PostLineEvent.
            object : TexyEventListener<AfterLineEvent> {
                override val eventClass: Class<*>
                    get() = AfterLineEvent::class.java

                @Throws(TexyException::class)
                override fun onEvent(event: AfterLineEvent): Node {
                    return DOMText(performReplaces(event.text))
                }
            }
        )

    // TypographyModule's patterns are not scanned for during parsing.
    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return null
    }

    // Config

    /**
     * Conversions - regexps and replacements for them.
     */
    private val conversions: MutableList<RegexpInfo> = ArrayList(40)

    // Options
    var localeKey = "cs"

    // Init.
    @Synchronized
    @Throws(TexyException::class)
    override fun loadRegexFromPropertiesFile(propsFilePath: String?) {
        // TODO: Move regexps to a properties file; read it, init conversions.

        // CONTENT_MARKUP mark:   \x17-\x1F
        // CONTENT_REPLACED mark: \x16
        // CONTENT_TEXTUAL mark:  \x17
        // CONTENT_BLOCK: not used in postLine
        var loc = LOCALES[localeKey]
        if (null == loc) loc = LOCALES["en"]


        val conversions: MutableList<RegexpInfo?> = ArrayList(40)

        // TODO: Convert from UTF-8 to UNICODE-16
        // http://www.utf8-chartable.de/unicode-utf8-table.pl?number=1024
        // http://www.utf8-chartable.de/unicode-utf8-table.pl?start=8192&number=1024
        conversions.add(tryCreateRegexpInfo("#(?<![.\u2026])\\.{3,4}(?![.\u2026])#mu", "\u2026", "ellipsis  ..."))
        conversions.add(tryCreateRegexpInfo("#(?<=[\\d ]|^)-(?=[\\d ]|$)#", "\u2013", "en dash 123-123"))
        conversions.add(
            tryCreateRegexpInfo(
                "#(?<=[^!*+,/:;<=>@\\\\_|-])--(?=[^!*+,/:;<=>@\\\\_|-])#",
                "\u2013",
                "en dash alphanum--alphanum"
            )
        )
        conversions.add(tryCreateRegexpInfo("#,-#", ",\u2013", "en dash ,-"))
        conversions.add(tryCreateRegexpInfo("#(?<!\\d)(\\d{1,2}\\.) (\\d{1,2}\\.) (\\d\\d)#", "$1\u00A0$2\u00A0$3", "date 23. 1. 1978"))
        conversions.add(tryCreateRegexpInfo("#(?<!\\d)(\\d{1,2}\\.) (\\d{1,2}\\.)#", "$1\u00A0$2", "date 23. 1."))
        conversions.add(tryCreateRegexpInfo("# --- #", "\u00A0\u2014 ", "em dash ---"))
        conversions.add(tryCreateRegexpInfo("# ([\u2013\u2014])#u", "\u00A0$1", "&nbsp; after dash (dash stays at line end)"))
        conversions.add(tryCreateRegexpInfo("# <-{1,2}> #", " \u2194 ", "left right arrow <-->"))
        conversions.add(tryCreateRegexpInfo("#-{1,}> #", " \u2192 ", "right arrow -->"))
        conversions.add(tryCreateRegexpInfo("# <-{1,}#", " \u2190 ", "left arrow <--"))
        conversions.add(tryCreateRegexpInfo("#={1,}> #", " \u2192 ", "right arrow ==>"))
        conversions.add(tryCreateRegexpInfo("#\\+-#", "\u00b1", "+-"))
        conversions.add(tryCreateRegexpInfo("#(\\d+)( ?)x\\2(?=\\d)#", "$1\u0097", "dimension sign 123 x 123..."))
        conversions.add(tryCreateRegexpInfo("#(?<=\\d)x(?= |,|.|$)#m", "\u0097", "dimension sign 123x"))
        conversions.add(tryCreateRegexpInfo("#(\\S ?)\\(TM\\)#i", "$1\u2122", "trademark (TM)"))
        conversions.add(tryCreateRegexpInfo("#(\\S ?)\\(R\\)#i", "$1\u00ae", "registered (R)"))
        conversions.add(tryCreateRegexpInfo("#\\(C\\)( ?\\S)#i", "\u00a9$1", "copyright (C)"))
        conversions.add(tryCreateRegexpInfo("#\\(EUR\\)#", "\u20AC", "Euro (EUR)"))
        conversions.add(tryCreateRegexpInfo("#(\\d{1,3}) (?=\\d{3})#", "$1\u00A0", "(phone) number 1 123 123 123..."))
        conversions.add(tryCreateRegexpInfo("#(?<=[^\\s\u0017])\\s+([\u0017-\u001F]+)(?=\\s)#u", "$1", "Remove intermarkup space, phase 1"))
        conversions.add(tryCreateRegexpInfo("#(?<=\\s)([\u0017-\u001F]+)\\s+#u", "$1", "Remove intermarkup space, phase 2"))
        conversions.add(
            tryCreateRegexpInfo(
                "#(?<=.{50})\\s+(?=[\u0017-\u001F]*\\S{1,6}[\u0017-\u001F]*$)#us",
                "\u00A0",
                "nbsp before last short word"
            )
        )

        // &nbsp; space between numbers (optionally followed by dot) and word, symbol, punctation, currency symbol.
        conversions.add(
            tryCreateRegexpInfo(
                "#(?<=^| |\\.|,|-|\\+|\u0016|\\()([\u0017-\u001F]*\\d+\\.?[\u0017-\u001F]*)\\s+(?=[\u0017-\u001F]*[%"
                        + RegexpPatterns.TEXY_CHAR + "\u00b0-\u00be\u2020-\u214f])#mu", "$1\u00A0", "&nbsp; between numbers"
            )
        )

        // Space between preposition and word.
        conversions.add(
            tryCreateRegexpInfo(
                "#(?<=^|[^0-9" + RegexpPatterns.TEXY_CHAR + "])([\u0017-\u001F]*[ksvzouiKSVZOUIA][\u0017-\u001F]*)\\s+(?=[\u0017-\u001F]*[0-9"
                        + RegexpPatterns.TEXY_CHAR + "])#mus", "$1\u00A0", "space after preposition"
            )
        )

        // Double and single ""
        // TODO: Escape with 0x18 or something, then replace in performReplaces(). Then make this.conversions static final.
        conversions.add(
            tryCreateRegexpInfo(
                "#(?<!\"|\\w)\"(?!\\ |\")(.+)(?<!\\ |\")\"(?!\")()#U",
                loc!!.doubleQuotes.a + "$1" + loc.doubleQuotes.b, "Double \""
            )
        )
        conversions.add(
            tryCreateRegexpInfo(
                "#(?<!\"|\\w)\"(?!\\ |\")(.+)(?<!\\ |\")\"(?!\")()#Uu",
                loc.doubleQuotes.a + "$1" + loc.doubleQuotes.b, "Single \""
            )
        )

        this.conversions.addAll(conversions.filterNotNull())
    }

    /**
     * Performs the regex replacement for each conversion.
     */
    private fun performReplaces(text: String?): String? {
        if (text == null) return null
        // For each conversion...
        var text = text
        for (ri in conversions) {
            text = ri.pattern.matcher(text).replaceAll(ri.replacement)
        }
        return text
    }

    companion object {
        private val log = Logger.getLogger(TypographyModule::class.java.name)

        /**
         * @see [Unicode](http://www.unicode.org/cldr/data/charts/by_type/misc.delimiters.html)
         */
        internal val LOCALES: MutableMap<String, LocaleInfo> = HashMap()

        // Locales static init
        init {
            LOCALES["cs"] = LocaleInfo(
                Pair("\u201A", "\u2018"),  // "\xe2\x80\x9a", "\xe2\x80\x98"
                Pair("\u201E", "\u201C") // "\xe2\x80\x9e", "\xe2\x80\x9c"
            )
            LOCALES["en"] = LocaleInfo(
                Pair("\u2018", "\u2019"),  // "\xe2\x80\x98", "\xe2\x80\x99"
                Pair("\u201C", "\u201D") // "\xe2\x80\x9c", "\xe2\x80\x9d"
            )
            LOCALES["fr"] = LocaleInfo(
                Pair("\u2039", "\u203A"),  // "\xe2\x80\xb9", "\xe2\x80\xba"
                Pair("\u00AB", "\u00BB") // "\xc2\xab",     "\xc2\xbb"
            )
            LOCALES["de"] = LocaleInfo(
                Pair("\u201A", "\u2018"),  // "\xe2\x80\x9a", "\xe2\x80\x98"
                Pair("\u201E", "\u201C") // "\xe2\x80\x9e", "\xe2\x80\x9c"
            )
            LOCALES["pl"] = LocaleInfo(
                Pair("\u201A", "\u2019"),  // "\xe2\x80\x9a", "\xe2\x80\x99"
                Pair("\u201E", "\u201D") // "\xe2\x80\x9e", "\xe2\x80\x9d"
            )
        }

        /**
         * Helper method to catch exception and log about it - to make list of conversions concise.
         */
        private fun tryCreateRegexpInfo(regex: String, replacement: String, name: String): RegexpInfo? {
            return try {
                RegexpInfo.Companion.fromRegexp(regex, replacement, name)
            } catch (ex: TexyException) {
                log.log(Level.SEVERE, "Error parsing regex:\n    {0}\n    {1}", arrayOf<Any>(regex, ex.toString()))
                null
            }
        }
    }
}

internal class LocaleInfo(
    val singleQuotes: Pair<String, String>,
    val doubleQuotes: Pair<String, String>
)

internal class Pair<A, B>(var a: A, var b: B)
