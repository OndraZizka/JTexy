package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.events.AroundEvent
import cz.dynawest.jtexy.events.AroundEventListener
import cz.dynawest.jtexy.modules.ImageModule.ImageInfo
import cz.dynawest.jtexy.parsers.TexyParser

/**
 *
 * @author Ondrej Zizka
 */
class ImageEvent(
    parser: TexyParser,
    var img: ImageInfo,
    var link: TexyLink,
    modifier: TexyModifier,
)
    : AroundEvent(parser, null, modifier)

interface ImageEventListener : AroundEventListener<ImageEvent>
