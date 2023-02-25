package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.parsers.AroundEvent
import cz.dynawest.jtexy.parsers.TexyParser

/**
 *
 * @author Ondrej Zizka
 */
class PhraseEvent(parser: TexyParser?, text: String?, modifier: TexyModifier?, val link: TexyLink?, val phraseName: String?) :
    AroundEvent(parser, text, modifier)
