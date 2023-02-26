package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.events.AroundEvent
import cz.dynawest.jtexy.events.AroundEventListener
import cz.dynawest.jtexy.parsers.TexyParser

/**
 *
 * @author Ondrej Zizka
 */
class BlockEvent : AroundEvent {
    val blockType: String
    val param: String?

    constructor(parser: TexyParser, text: String, modifier: TexyModifier, blockType: String, param: String?) : super(
        parser,
        text,
        modifier
    ) {
        this.blockType = blockType
        this.param = param
        this.text = param
    }

    constructor(orig: BlockEvent, blockType: String, param: String?) : super(orig.parser, orig.text!!, orig.modifier) {
        this.blockType = blockType
        this.param = param
        this.text = param
    }
}


interface BlockEventListener : AroundEventListener<BlockEvent>
