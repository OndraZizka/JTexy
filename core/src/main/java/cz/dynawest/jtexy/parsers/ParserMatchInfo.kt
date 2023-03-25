package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.util.MatchWithOffset

/**
 * Stores information about match while parsing.
 * @author Ondrej Zizka
 */
internal class ParserMatchInfo(val pattern: RegexpInfo?, val groups: List<MatchWithOffset>, val offset: Int, val priority: Int) :
    Comparable<ParserMatchInfo> {
    constructor(ri: RegexpInfo?, groups: List<MatchWithOffset>, offset: Int) : this(ri, groups, offset, 0)

    override fun toString(): String {
        return (if (pattern == null) "(no pattern)" else pattern.name) + " @ " + offset + ", 0th group: " + if (groups == null) "(groups == null)" else groups[0]
    }

    /**
     * Compares two matches by offset; if equal, by priority.
     * Null objects go last.
     */
    override fun compareTo(o: ParserMatchInfo?): Int {
        if (null == o) return 1
        var diff = offset - o.offset
        if (diff != 0) return diff
        diff = priority - o.priority
        return if (diff != 0) diff else 0
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as ParserMatchInfo
        if (offset != other.offset) {
            return false
        }
        return if (priority != other.priority) {
            false
        } else true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 17 * hash + offset
        hash = 17 * hash + priority
        return hash
    }

    companion object {
        /** Compares by offset, then priority. Null objects go last.  */
        val COMPARATOR = java.util.Comparator<ParserMatchInfo> { pmi1, pmi2 ->
            var diff = pmi1.offset - pmi2.offset
            if (diff != 0) return@Comparator diff
            diff = pmi1.priority - pmi2.priority
            if (diff != 0) diff else 0
        }
    }
} // ParserMatchInfo
