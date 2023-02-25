package cz.dynawest.jtexy.util

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.Protector
import cz.dynawest.jtexy.RegexpPatterns
import cz.dynawest.jtexy.TexyException
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.ArrayUtils
import org.apache.commons.lang.StringUtils
import org.dom4j.dom.DOMElement
import org.dom4j.io.HTMLWriter
import org.jsoup.Jsoup
import java.io.*
import java.net.URLEncoder
import java.text.Normalizer
import java.util.*
import java.util.logging.*

/**
 *
 * @author Ondrej Zizka
 */
object JTexyStringUtils {
    private val log = Logger.getLogger(JTexyStringUtils::class.java.name)
    // IndexOfAny chars
    //-----------------------------------------------------------------------
    /**
     *
     * Search a String to find the first index of any
     * character in the given set of characters.
     *
     *
     * A `null` String will return `-1`.
     * A `null` or zero length search array will return `-1`.
     *
     * <pre>
     * StringUtils.indexOfAny(null, *)                = -1
     * StringUtils.indexOfAny("", *)                  = -1
     * StringUtils.indexOfAny(*, null)                = -1
     * StringUtils.indexOfAny(*, [])                  = -1
     * StringUtils.indexOfAny("zzabyycdxx",['z','a']) = 0
     * StringUtils.indexOfAny("zzabyycdxx",['b','y']) = 3
     * StringUtils.indexOfAny("aba", ['z'])           = -1
    </pre> *
     *
     * @param str  the String to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     */
    fun indexOfAny(str: String?, searchChars: CharArray, offset: Int): Int {
        if (StringUtils.isEmpty(str) || ArrayUtils.isEmpty(searchChars)) return -1
        if (offset >= str!!.length) return -1
        for (i in offset until str.length) {
            val ch = str[i]
            for (j in searchChars.indices) {
                if (searchChars[j] == ch) return i
            }
        }
        return -1
    }

    /** Prepends the given prefix to the given url.  */
    fun prependUrlPrefix(prefix: String?, url: String?): String? {
        return if (prefix == null || "" == prefix || !isRelativeUrl(url)) url else StringUtils.stripEnd(prefix, "/\\") + "/" + url
    }

    /** Checks whether an URL is relative ( == is not absolute or a doc anchor).  */
    private fun isRelativeUrl(url: String?): Boolean {
        // check for scheme, or absolute path, or absolute URL
        return !url!!.matches(( /*(?A)*/"^" + RegexpPatterns.Companion.TEXY_URLSCHEME + "|[\\#/?]").toRegex()) // (?A) in Texy
    }

    /**
     * Substitute for PHP's preg_replace_callback().
     *
     */
    @JvmStatic
    @Deprecated("Use Matcher#appendReplacement() instead.")
    fun replaceWithCallback(
        str: String?, regexp: String, cb: StringsReplaceCallback
    ): String {
        val pat: Pattern = Pattern.Companion.compile(regexp)
        return replaceWithCallback(str, pat, cb)
    }

    /**
     * Substitute for PHP's preg_replace_callback().
     *
     */
    @Deprecated("Use Matcher#appendReplacement() instead.")
    fun replaceWithCallback(
        str: String?, pat: Pattern, cb: StringsReplaceCallback
    ): String {
        val mat = pat.matcher(str)
        val sb = StringBuilder(str!!.length * 11 / 10)
        var prevStart = 0
        var prevEnd = 0
        val offset = 0
        while (mat!!.find()) {

            // Create the groups array.
            val groups = arrayOfNulls<String>(mat.groupCount() + 1)
            for (i in groups.indices) {
                groups[i] = mat.group(i)
            }

            // Append string before match.
            sb.append(str.substring(prevEnd + offset, mat.start() + offset))

            // Call the callback and append what it returns.
            val newStr = cb.replace(groups)
            sb.append(newStr)

            // Set the offset according to the lengths difference.
            //offset -= mat.group().length();
            prevStart = mat.start() + offset
            prevEnd = mat.end() + offset
        }
        sb.append(str.substring(prevEnd))
        return sb.toString()
    }

    /**
     * Expands tabs to spaces, according to the tab position (as with typewriter tabs).
     */
    fun expandTabs(text: String, tabWidth: Int): String {
        val sb = StringBuilder(text.length * 108 / 100)
        var lastLineBreak = -1 // "Previous line"
        var nextLineBreak = 0
        var lastTab = 0
        var nextTab = text.indexOf('\t')
        var charsAddedForThisLine = 0
        while (nextTab != -1) {

            // Append everything from the last tab to this one.
            sb.append(text.substring(lastTab, nextTab))

            // After this, lastLB will have the beginning of the line with the next tab,
            // and nextLB the end of it.
            while (nextTab > nextLineBreak) {
                charsAddedForThisLine = 0 // We're going to some other line - reset.
                lastLineBreak = nextLineBreak
                nextLineBreak = text.indexOf('\n')
                if (nextLineBreak == -1) nextLineBreak = text.length
            }

            // Append number of spaces according to the tab's position in the line.
            val tabPos = nextTab - lastLineBreak - 1
            //return $m[1] . str_repeat(' ', $this->tabWidth - strlen($m[1]) % $this->tabWidth);
            val repeats = tabWidth - tabPos % tabWidth
            sb.append(repeatSpaces(repeats))
            charsAddedForThisLine += tabWidth - 1

            //nextLineBreak = text.indexOf('\n', nextTab);
            lastTab = nextTab
            nextTab = text.indexOf('\t', nextTab)
        }
        sb.append(text.substring(lastTab, text.length))
        return sb.toString()
    }

    /**  Optimization  */
    fun repeatSpaces(repeats: Int): String {
        val spaces: String
        spaces = when (repeats) {
            0 -> ""
            1 -> " "
            2 -> "  "
            3 -> "   "
            4 -> "    "
            5 -> "     "
            6 -> "      "
            7 -> "       "
            else -> StringUtils.repeat(" ", repeats)
        }
        return spaces
    }

    fun repeatTabs(repeats: Int): String? {
        var spaces: String? = null
        if (repeats <= 10) {
            when (repeats) {
                0 -> spaces = ""
                1 -> spaces = "\t"
                2 -> spaces = "\t\t"
                3 -> spaces = "\t\t\t"
                4 -> spaces = "\t\t\t\t"
                5 -> spaces = "\t\t\t\t\t"
                6 -> spaces = "\t\t\t\t\t\t"
                7 -> spaces = "\t\t\t\t\t\t\t"
                8 -> spaces = "\t\t\t\t\t\t\t\t"
                9 -> spaces = "\t\t\t\t\t\t\t\t\t"
                10 -> spaces = "\t\t\t\t\t\t\t\t\t\t"
            }
            return spaces
        }
        return if (repeats <= TABS.length) TABS.substring(repeats) else StringUtils.repeat(" ", repeats).also { spaces = it }
    }

    const val TABS = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"

    /**
     * Translate all white spaces (\t \n \r space) to meta-spaces \x01-\x04.
     * which are ignored by TexyHtmlOutputModule routine
     * @param  string
     * @return string
     */
    fun freezeSpaces(str: String?): String {
        return StringUtils.replaceChars(str, " \t\r\n", "\u0001\u0002\u0003\u0004")
    }

    /**
     * Reverts meta-spaces back to normal spaces.
     * @param  string
     * @return string
     */
    fun unfreezeSpaces(str: String?): String {
        return StringUtils.replaceChars(str, "\u0001\u0002\u0003\u0004", " \t\r\n")
    }

    /**  Removes indentation, up to the numer of spaces on the first line. (Tabs are converted before.)   */
    fun outdent(str: String?): String? {
        var str = str
        str = StringUtils.strip(str, "\n")
        val numSpaces = StringUtils.indexOfAnyBut(str, " ")
        return if (0 < numSpaces) str.replace("(?m)^ {1,$numSpaces}".toRegex(), "") else str
    }

    @Deprecated("to see where it is used. Try Dom4jUtils. ")
    @Throws(TexyException::class)
    fun elementToHTML(elm: DOMElement?, texy: JTexy?): String {
        val sw = StringWriter()
        val hw = HTMLWriter(sw)
        try {
            hw.write(elm)
        } catch (ex: IOException) {
            throw TexyException(ex)
        }
        return sw.toString()
    }

    /** Converts < > & to the HTML entities. Not quotes (as in TexyHTML, assuming there is a reason).  */
    fun escapeHtml(str: String?): String {
        return StringUtils.replaceEach(str, arrayOf("<", ">", "&"), arrayOf("&lt;", "&gt;", "&amp;"))
    }

    /** Converts HTML entities to < > & .  */
    fun unescapeHtml(str: String?): String {
        return StringUtils.replaceEach(str, arrayOf("&lt;", "&gt;", "&amp;"), arrayOf("<", ">", "&"))
    }

    /**
     * Converts given internal Texy string into plain text - unprotects & strips HTML.
     *
     * TODO:  @ Texy::stringToText(){}. Perhaps already coded somewhere.
     */
    @Throws(TexyException::class)
    fun stringToText(str: String?, protector: Protector?): String {
        var str = str
        if (null != protector) str = protector.unprotectAll(str)
        return stripHtmlTags(str)
    }

    fun stripHtmlTags(str: String?): String {
        return Jsoup.parse(str).text()
    }

    /**
     * Encodes the URL, using UTF-8.
     * @throws UnsupportedOperationException  Wraps UnsupportedEncodingException.
     */
    fun encodeUrl(res: String?): String {
        return try {
            URLEncoder.encode(res, "UTF-8")
        } catch (ex: UnsupportedEncodingException) {
            throw UnsupportedOperationException(ex)
        }
    }

    /**
     * Removes special controls characters and normalizes line endings and spaces.
     */
    fun normalize(s: String?): String? {
        // Standardize line endings to unix-like.
        var s = s
        s = s!!.replace("\r\n", "\n") // DOS
        s = s.replace('\r', '\n') // Mac

        // Remove special chars; leave \t + \n.
        s = StringUtils.replaceChars(
            s,
            "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008" +
                    "\u000B\u000C\u000E\u000F", ""
        )

        // The following allows to leave intentional trailing whitespace
        // with:  "end of text.  \n"

        // Right trim.
        s = StringUtils.stripEnd(s, "\t ")

        // Trailing spaces.
        s = StringUtils.strip(s, "\n")
        return s
    }

    /**
     * Converts given string to something usable in URL, ID, etc.
     * 1) Shortens to 50 chars.
     * 2) Removes diacritics
     * 3) Strips HTML tags.
     */
    @JvmStatic
    fun webalize(str: String?): String {
        var str = str ?: return ""
        str = StringUtils.substring(str, 0, 50).lowercase(Locale.getDefault())
        str = removeDiacritics(str)
        str = stripHtmlTags(str)

        //log.finest("Webalizing: " + Debug.showCodes(str));
        val len = str.length
        if (len == 0) return ""
        val chars = CharArray(len)
        val chars2 = CharArray(len)
        str.toCharArray(chars, 0, 0, len)
        var pos = 0
        var lastDash = -1
        //int firstDash = 0;

        // Replace non-alnum with dash, no dash sequences.
        for (i in chars.indices) {
            val ch = chars[i]
            if (Character.isLetterOrDigit(ch)) {
                chars2[pos++] = ch
                lastDash = -1 // Last char is not a dash.
                //if( firstDash == 0 )
                //	firstDash = -1;
            } else if (-1 == lastDash) {
                chars2[pos] = '-'
                lastDash = pos++ // Last char is a dash.
                //if( firstDash != -1 )
                //	firstDash++;
            }
        } // for each char.
        if (-1 != lastDash) pos = lastDash

        //if( firstDash == pos )
        //	return "";
        var res = kotlin.String(chars2,  /*firstDash*/0, pos)
        res = StringUtils.stripStart(res, "-")
        return res
    } // webalize()

    /**
     * Removes diacritics.
     */
    private fun removeDiacritics(str: String): String {
        var str: String? = str
        str = Normalizer.normalize(str, Normalizer.Form.NFD)
        str = DIA.matcher(str).replaceAll("")
        return str
    }

    private val DIA: Pattern = Pattern.Companion.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+")
}
