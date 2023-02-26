package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.events.AroundEvent
import cz.dynawest.jtexy.events.AroundEventListener
import cz.dynawest.jtexy.parsers.TexyParser

/**
 *
 * @author Ondrej Zizka
 */
class HeadingEvent(parser: TexyParser, text: String, modifier: TexyModifier, var level: Int, var isSurrounded: Boolean) :
    AroundEvent(parser, text, modifier)


interface HeadingEventListener : AroundEventListener<HeadingEvent>
