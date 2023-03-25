package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.RegexpPatterns
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.modules.BeforeBlockEvent
import cz.dynawest.jtexy.modules.ParagraphEvent
import cz.dynawest.jtexy.modules.TexyModifier
import cz.dynawest.jtexy.util.MatchWithOffset
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.StringUtils
import org.dom4j.dom.DOMElement
import org.dom4j.dom.DOMText
import java.util.*
import java.util.logging.*

/**
 *
 * @author Ondrej Zizka
 */
class TexyBlockParser(texy: JTexy, element: DOMElement, val indented: Boolean) : TexyParser(texy, element) {
    private var text: String? = null
    private var offset = 0
    override val patterns: List<RegexpInfo>
        protected get() = texy.blockPatterns

    /**
     * Match current line against the given RE.
     * If successful, increments current position to the end of the match.
     * @returns the list of matches.
     */
    fun next(pattern: String): List<MatchWithOffset> {
        //pattern = "^"+pattern; // Instead of $pattern . 'A' - anchored
        return this.next(Pattern.compile(pattern, Pattern.MULTILINE))
    }

    /**
     * Match current line against the given Pattern.
     * If successful, increments current position to the end of the match.
     * @returns the list of matches.
     */
    fun next(pattern: Pattern): List<MatchWithOffset> {
        if (offset >= text!!.length) {
            if (offset != text!!.length) log.warning("Offset overflow! Length: " + text!!.length + " Off: " + offset)
            return null
        }

        /*
        Matcher mat = pattern.matcher( this.text );
        if( ! mat.find( this.offset ) )
            return null;
         */

        // Instead of (?A) - anchored. Otherwise, "^Ahoj" also matched "\n\n|Ahoj".
        val substr = text!!.substring(offset)
        val mat = pattern.matcher(substr)
        if (!mat.find()) return null
        /**/


        //TODO: Replace this.offset with substr througout this method.
        //      Or, is \A enough?  No, it's not.
        // DONE.


        // !! Added to this.offset because the regex was applied on a substring!
        offset = offset + mat.end() + 1 // 1 = "\n"

        // Get the groups of the match.
        val matches: MutableList<MatchWithOffset> = ArrayList(mat.groupCount() + 1)
        for (i in 0 until mat.groupCount() + 1) {
            matches.add(MatchWithOffset(mat.group(i), offset + mat.start(i)))
        }
        return matches
    } // next()
    /**
     * Moves the current position after the previous newline.
     *
     * If at the beginning of a line,
     * moves the current position of this parser to the beginning of the previous line.
     * If in the middle of a line,
     * moves at the beginning of this line.
     */
    /** Moves the current position after the previous newline.  */
    @JvmOverloads
    fun moveBackward(linesCount: Int = 1) {
        // We're at the start of a line - skip before the last \n.
        var linesCount = linesCount
        if (offset != 0) offset--
        do {
            offset = text!!.lastIndexOf('\n', offset - 1)
            //if( this.offset == -1 ){ this.offset = 0; break; }
            offset++ // Go after the \n; also handles the -1 case.
            linesCount--
            if (linesCount < 1) break
        } while (true)
    }

    @Deprecated("TODO: Dump. ")
    fun moveBackwardUgly(linesCount: Int) {
        var linesCount = linesCount
        val chars = CharArray(offset)
        text!!.toCharArray(chars, 0, 0, offset)
        while (--offset > 0) {
            //if( '\n' == this.text.charAt( this.offset - 1 ) ){
            if ('\n' == chars[offset - 1]) {
                linesCount--
                if (linesCount < 1) break
            }
        }
        offset = Math.max(offset, 0)
    }

    /**
     * Parses the given text.
     * 1) Fires a BeforeBlockEvent.
     * 2) Calls [.parseLoop] .
     * 3) Calls [.processLoop] .
     */
    @Throws(TexyException::class)
    override fun parse(text: String) {
        var text = text
        log.fine("===================================================================")
        log.fine("Parsing: " + StringUtils.abbreviate(text, 45).replace("\n", "\\n") + " (" + text.length + ")")


        // BeforeBlockEventListener's can modify the text before parsing.
        val ev = BeforeBlockEvent(this, text)
        texy.invokeNormalHandlers(ev)
        text = ev.text

        // --- Parse loop. --- //
        val allMatches = parseLoop(text, patterns)

        // TODO: Handle the case with no matches.

        // --- Process Loop. ---
        processLoop(text, allMatches)
    }

    /**
     * Processes the text using all given matches -
     * passes matches to their handlers and replaces
     * the matched part of the text with the handler's result.
     * Also processes text between matches.
     */
    @Throws(TexyException::class)
    private fun processLoop(text: String, allMatches: MutableList<ParserMatchInfo>) {

        // Terminal cap.
        var pmi = ParserMatchInfo(null, null, text.length)
        allMatches.add(pmi)
        val it: ListIterator<ParserMatchInfo> = allMatches.listIterator()
        offset = 0
        this.text = text
        do {
            // Set the initial match info to the end of the whole content to handle the case with no matches.
            // Consider: Perhaps this should be appended to the PMI list?
            while (it.hasNext()) {
                pmi = it.next()
                if (pmi.offset >= offset) break
            }

            // Between-matches content.
            if (pmi.offset > offset) {
                val textBetween = text.substring(offset, pmi.offset).trim { it <= ' ' }
                if (0 != textBetween.length) {
                    processTextBetweenMatches(textBetween)
                }
            }

            // Terminal cap - Finito.
            if (null == pmi.groups) break

            // Advance to the place after the matched string (block).
            offset = pmi.offset + pmi.groups!![0]!!.match!!.length + 1 // 1 = \n

            // Call the handler.
            val result = pmi.pattern!!.handler!!.handle(this, pmi.groups, pmi.pattern)

            // this.offset <= pmi.offset should never happen, see this.offset = ... above
            if (null == result || offset <= pmi.offset) {
                // Prevent generic block split.
                offset = pmi.offset // Set the offset back.
                continue
            } else if (result is DOMElement) {
                element.add(result)
            } else if (result is DOMText) {
                element.add(result)
            }
        } while (true)
    }

    /**
     * Processes text between block patterns matches
     * (line patterns are used inside this and inside block handlers).
     * Usually this is a text spanned over multiple paragraphs.
     * Separates the text into paragraphs and fires a ParagraphEvent for each.
     */
    @Throws(TexyException::class)
    fun processTextBetweenMatches(textBetween: String) {
        val parts: Array<String>
        // Split paragraphs - by 2 or more lines.
        parts = if (this.isIndented) {
            // (?! )  ==  zero-width negative lookahead  ==  no spaces after a newline.
            //$parts = preg_split('#(\n(?! )|\n{2,})#', $content, -1, PREG_SPLIT_NO_EMPTY);
            textBetween.split("(\\n(?! )|\\n{2,})".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else {
            //$parts = preg_split('#(\n{2,})#', $content, -1, PREG_SPLIT_NO_EMPTY);
            textBetween.split("(\\n{2,})".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        // For each paragraph...
        for (i in parts.indices) {
            var parText = parts[i]
            parText = parText.trim { it <= ' ' }
            if (0 == parText.length) continue


            // Try to find a modifier.
            var modifier: TexyModifier? = null
            val pat = PAR_MODIFIER_PATTERN
            val mat = pat.matcher(parText)
            if (mat!!.matches()) {
                // list(, $mC1, $mMod, $mC2) = $mx;
                val mC1 = mat.group(1)
                val mMod = mat.group(2)
                val mC2 = StringUtils.defaultString(mat.group(3))
                parText = (mC1 + mC2).trim { it <= ' ' }
                if ("" == parText) continue
                modifier = TexyModifier(mMod)
            }

            // Fire a ParagraphEvent.
            val ev = ParagraphEvent(this, parText, modifier)
            try {
                val parNode = texy.invokeAroundHandlers(ev)
                log.finer("ParagraphEvent invocation returned: $parNode")
                //String res = ev.getText(); // TODO
                //if( null != res ) this.element.addCDATA(res);
                if (null != parNode) element.add(parNode)
            } catch (ex: TexyException) {
                throw TexyException("""Error when parsing paragraph starting with '${StringUtils.abbreviate(parText, 20)}': $ex""", ex)
            }
        }
    } // processTextBetweenMatches()


    val posAsString: String
        /** Debug function - displays position info in the form of:  "...|..."  */
        get() = getPosAsString(13)

    /** Debug function - displays position info in the form of:  "...|..."  */
    fun getPosAsString(bounds: Int): String {
        val min = Math.max(offset - bounds, 0)
        val max = Math.min(offset + bounds, text!!.length)
        if (min == max) return "(empty text)"
        var delimPos = Math.max(offset, min) // Lower bound.
        delimPos = Math.min(delimPos, max) // Upper bound.
        var strBefore = text!!.substring(min, delimPos)
        val strAfter = text!!.substring(delimPos, max)

        // Add ... around
        if (min != 0) strBefore = "…$strBefore"
        return StringUtils.replaceEach(
            "$strBefore◆$strAfter", arrayOf("\r", "\n"), arrayOf("\\r", "\\n")
        ) + "…"
    }

    /** @returns "@{offset}: "+getPosAsString();
     */
    override fun toString(): String {
        return if (text == null) "[no text yet]" else "@" + offset + ": " + posAsString
    }

    companion object {
        private val log = Logger.getLogger(TexyBlockParser::class.java.name)

        /**
         * Scans the text for the matches of the given list of patterns.
         * @returns a list of matches, ordered by the offset of the start of the match.
         * // TBD: Rename MatchWithOffset to MatchData or similar?
         */
        private fun parseLoop(text: String, patterns: List<RegexpInfo>): MutableList<ParserMatchInfo> {
            val finer = JTexy.Companion.debug && log.isLoggable(Level.FINER)
            val finest = JTexy.Companion.debug && log.isLoggable(Level.FINEST)

            // $matches[] = array($offset, $name, $m, $priority);
            val allMatches: MutableList<ParserMatchInfo> = ArrayList()
            var priority = 0


            /*  For each pattern... */for (pattern in patterns) {
                if (finest) log.finest("Applying pattern: " + pattern.name)
                val pat = pattern.pattern
                val mat = pat.matcher(text)

                // All matches of this pattern throughout the text.
                val matches: List<List<MatchWithOffset>> = MatchWithOffset.Companion.fromMatcherAll(mat)

                // For each match, store its start offset and all match groups.
                for (groups in matches) {
                    allMatches.add(ParserMatchInfo(pattern, groups, groups!![0]!!.offset, priority))
                }
                priority++
            } // for each patterns;

            // Sort the matches by offset, then priority.
            Collections.sort(allMatches)
            log.fine("Matches: " + allMatches.size)
            if (finer) for (pmi in allMatches) log.finer("    Match: " + pmi.offset + ": " + pmi.groups)
            return allMatches
        }

        //preg_match('#\A(.*)(?<=\A|\S)'.TEXY_MODIFIER_H.'(\n.*)?()\z#sUm', $s, $mx);
        private val PAR_MOD_REGEX = "(?sUm)\\A(.*)(?<=\\A|\\S)" + RegexpPatterns.TEXY_MODIFIER_H + "(\\n.*)?()\\z"
        private val PAR_MODIFIER_PATTERN: Pattern = Pattern.compile(PAR_MOD_REGEX)
    }
}
