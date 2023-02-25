package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.parsers.TexyParser
import cz.dynawest.jtexy.util.MatchWithOffset
import org.dom4j.Node

/**
 *
 * @author Ondrej Zizka
 */
interface PatternHandler {
    /** @returns the name of this handler.
     */
    val name: String

    /**
     * Module callback which handles a match found by a parser.
     *
     * @param parser   Parser back-reference.
     * @param groups   Groups of the given regexp match.
     * @param pattern  Pattern which created this match.
     *
     * @return  null if this handler rejects the text.
     * Otherwise, DOMElement or TextNode created from the groups.
     */
    @Throws(TexyException::class)
    fun handle(
        parser: TexyParser,
        groups: List<MatchWithOffset?>?,
        pattern: RegexpInfo?
    ): Node?
}
