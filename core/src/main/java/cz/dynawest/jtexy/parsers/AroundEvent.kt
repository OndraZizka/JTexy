package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.modules.TexyModifier

/**
 *
 * @author Ondrej Zizka
 */
abstract class AroundEvent(parser: TexyParser?, text: String?, modifier: TexyModifier?) : TexyParserModEvent(parser, text, modifier)
