package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.events.TexyEvent

/**
 * Parser event; Carries text and parser backref.
 *
 * @author Ondrej Zizka
 */
open class TexyParserEvent
/** Const  */(/* -- Fields -- */
              val parser: TexyParser?, text: String?
) : TexyEvent(text)
