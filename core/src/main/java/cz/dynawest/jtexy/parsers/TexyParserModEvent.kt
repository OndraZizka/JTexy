package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.modules.TexyModifier

/**
 *
 * @author Ondrej Zizka
 */
open class TexyParserModEvent(parser: TexyParser?, text: String?, var modifier: TexyModifier?) : TexyParserEvent(parser, text)
