package cz.dynawest.jtexy.parsers

import org.dom4j.dom.DOMElement

/**
 *
 * @author Ondrej Zizka
 */
class AfterParseEvent(parser: TexyParser?, var isSingleLine: Boolean, var dom: DOMElement) : BeforeAfterEvent(parser, null)
