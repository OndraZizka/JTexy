package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import org.dom4j.dom.DOMElement

/**
 *
 * @author Ondrej Zizka
 */
abstract class TexyParser(// Uplink to parent.
    val texy: JTexy?, protected var element: DOMElement
) {
    // TODO: Perhaps should be moved to TexyBlockParser? It would need to override e.g. BlockModule.getParser().
    var isIndented = false
    protected abstract val patterns: List<RegexpInfo?>?

    @Throws(TexyException::class)
    abstract fun parse(text: String?)
}
