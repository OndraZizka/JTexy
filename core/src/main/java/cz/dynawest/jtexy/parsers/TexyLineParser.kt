package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter
import cz.dynawest.jtexy.util.MatchWithOffset
import org.apache.commons.lang.StringUtils
import org.dom4j.dom.DOMElement
import org.dom4j.dom.DOMText
import java.util.logging.*

/**
 * Parser which parses the content of the blocks, using LINE patterns.
 *
 * @author Ondrej Zizka
 */
class TexyLineParser(texy: JTexy, element: DOMElement) : TexyParser(texy, element) {
    var again = false
    protected override val patterns: List<RegexpInfo>
        protected get() = texy.linePatterns

    /**
     * Tries to match all LINE patterns against the text.
     * The one which starts at the earliest position is applied -
     * it's handler is called, and it's match is replaced with
     * the response of the handler (either DOMText, DOMElement or null).
     */
    @Throws(TexyException::class)
    override fun parse(text: String) {
        var text = text
        val finest = log.isLoggable(Level.FINEST)
        if (patterns.isEmpty()) {
            // Nothing to do.
            element.add(DOMText(text))
            return
        }
        var offset = 0
        val allMatches: MutableMap<RegexpInfo, ParserMatchInfo> = HashMap(patterns.size)

        // Store current offset for each line pattern.
        val patternOffsets: MutableMap<RegexpInfo, Int> = HashMap(patterns.size)
        for (ri in patterns) {
            patternOffsets[ri] = -1
        }


        // Parse loop.
        do {
            var minPattern: RegexpInfo? = null
            var minOffset = text!!.length


            // For each line pattern...
            for (ri in patterns) {
                if (finest) log.log(
                    Level.FINEST, "  Parsing with pattern {0} - {1}...", arrayOf(
                        ri!!.name, ri.regexp
                    )
                )
                if (patternOffsets[ri]!! < offset) {
                    val delta = if (patternOffsets[ri] == -2) 1 else 0

                    // Regexp match
                    //Pattern pat = Pattern.compile( ri.getRegexp() );
                    val pat = ri.pattern
                    val mat = pat.matcher(text)
                    if (mat.find(offset + delta)) {
                        // Store match info.
                        val groups: List<MatchWithOffset> = MatchWithOffset.Companion.fromMatcherState(mat)
                        val curMatchInfo = ParserMatchInfo(ri, groups, mat.start())
                        allMatches[ri] = curMatchInfo
                        if (groups[0]!!.match!!.length == 0) continue
                        // Store offset for this pattern.
                        patternOffsets[ri] = mat.start()
                    } else {
                        // Try next time.
                        continue
                    }
                }

                val curOffset = patternOffsets[ri]!!
                if (curOffset < minOffset) {
                    minOffset = curOffset
                    minPattern = ri
                }
            }


            // Nothing matched?
            if (minPattern == null) {
                if (finest) log.finest("No more matches.")
                break
            }
            offset = minOffset
            val start = offset

            // Call the handler of the minimal match.
            again = false
            log.log(Level.FINER, "Using handler: {0}", minPattern.handler?.name)

            val resNode = minPattern.handler!!.handle(this, allMatches[minPattern]!!.groups, minPattern)
            var resString = "Invalid response from handler of " + minPattern.name + ": " + resNode
            if (resNode is DOMElement) {
                //resString = resNode.asXML();
                resString = ProtectedHTMLWriter.Companion.fromElement(resNode, texy.protector)
            } else if (null == resNode) {
                // Store that this was rejected.
                patternOffsets[minPattern] = -2
                continue
            } else if (resNode is DOMText) {
                resString = resNode.text
            }
            if (finest) log.log(Level.FINEST, "Result: {0}", resNode)
            if (finest) log.log(Level.FINEST, "Result string: {0}", resString)
            val matchStart = minOffset //patternOffsets.get(minPattern);
            val matchLen = allMatches[minPattern]!!.groups!![0]!!.match!!.length
            // Replace the matched part of the text with the result.
            text = StringUtils.overlay(text, resString, start, matchStart + matchLen)
            val delta = resString.length - matchLen

            // Adjust all patterns' offset.
            for (entry in patternOffsets.entries) {
                // If this pattern's offset is before the left-most match, reset it back to start.
                if (entry.value!! < matchStart + matchLen) { // TODO: add match end to MatchWithOffset.
                    entry.setValue(-1)
                } else {
                    entry.setValue(entry.value!! + delta)
                }
            }
            if (again) {
                patternOffsets[minPattern] = -2
            } else {
                patternOffsets[minPattern] = -1
                offset += resString.length
            }

            // TODO
        } while (true)
        log.log(Level.FINE, "Resulting string after parsing: {0}", text)

        // $this->element->insert(NULL, $text);
        //((DOMElement)this.element).addCDATA( text );
        element.addText(text)
    } // parse()

    companion object {
        private val log = Logger.getLogger(TexyLineParser::class.java.name)
    }
}
