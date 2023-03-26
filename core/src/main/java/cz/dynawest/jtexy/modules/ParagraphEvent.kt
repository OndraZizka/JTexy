package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.events.AroundEvent
import cz.dynawest.jtexy.events.AroundEventListener
import cz.dynawest.jtexy.parsers.TexyParser


class ParagraphEvent(parser: TexyParser, text: String, modifier: TexyModifier?) : AroundEvent(parser, text, modifier)
interface ParagraphEventListener : AroundEventListener<ParagraphEvent>
