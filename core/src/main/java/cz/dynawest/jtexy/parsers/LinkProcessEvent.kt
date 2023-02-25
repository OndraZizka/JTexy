package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.modules.LinkProvider
import cz.dynawest.jtexy.modules.TexyLink
import cz.dynawest.jtexy.modules.TexyModifier
import java.io.Serializable

/**
 *
 * @author Ondrej Zizka
 */
class LinkProcessEvent(
    parser: TexyParser?,
    modifier: TexyModifier?,
    val modStr: String?,
    // In.
    val dest: String,
    val label: String?,
    val linkProvider: LinkProvider?
) : AroundEvent(parser, null, modifier), Serializable {
    // Out.
    var link: TexyLink? = null

}
