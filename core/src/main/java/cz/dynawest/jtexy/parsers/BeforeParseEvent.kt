package cz.dynawest.jtexy.parsers

import java.io.Serializable

/**
 *
 * @author Ondrej Zizka
 */
class BeforeParseEvent(parser: TexyParser?, text: String?) : BeforeAfterEvent(parser, text), Serializable
