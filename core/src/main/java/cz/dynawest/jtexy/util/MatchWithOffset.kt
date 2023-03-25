package cz.dynawest.jtexy.util

import cz.dynawest.openjdkregex.Matcher
import org.apache.commons.lang.StringUtils
import java.util.*

/**
 *
 * @author Ondrej Zizka
 */
class MatchWithOffset
/** Const  */(@JvmField val match: String?, @JvmField val offset: Int) {
    /** Returns "offset: match".  */
    override fun toString(): String {
        var str = StringUtils.abbreviate(match, 25)
        //str = StringEscapeUtils.escapeJava(str); // Converts diacritics to \u00E1 etc.
        str = StringUtils.replaceEach(str, arrayOf("\r", "\n", "\t"), arrayOf("\\r", "\\n", "\\t"))
        val sb = StringBuilder("@").append(offset).append(": ")
        return if (null == str) sb.append("nul").toString() else sb.append('"').append(str).append('"').toString()
    }

    companion object {
        /** Get list of match groups after successful match.  */
        fun fromMatcherState(matcher: Matcher?): List<MatchWithOffset> {
            //if( matcher.groupCount() == 0 )
            //	return Collections.EMPTY_LIST;
            val groups: MutableList<MatchWithOffset> = ArrayList(matcher!!.groupCount() + 1)
            for (i in 0..matcher.groupCount()) {
                groups.add(MatchWithOffset(matcher.group(i), matcher.start(i)))
            }
            return groups
        }

        /** Get list of lists of groups of all matches.  */
		@JvmStatic
		fun fromMatcherAll(matcher: Matcher?): List<List<MatchWithOffset>> {

            // If not matched, return empty list.
            if (!matcher!!.find()) return emptyList()

            // Else loop trough the matches and groups and build 2-dim array.
            val matches: MutableList<List<MatchWithOffset>> = ArrayList()
            do {
                val groups: MutableList<MatchWithOffset> = ArrayList()
                for (i in 0 until matcher.groupCount() + 1) {
                    groups.add(MatchWithOffset(matcher.group(i), matcher.start(i)))
                }
                matches.add(groups)
            } while (matcher.find())
            return matches
        }
    }
}
