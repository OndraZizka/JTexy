package cz.dynawest.jtexy.parsers

import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.TexyEvent
import org.dom4j.Node

/**
 *
 * @author Ondrej Zizka
 */
interface TexyEventListener<in T : TexyEvent> {
    val eventClass: Class<*>

    @Throws(TexyException::class)
    fun onEvent(event: T): Node?
}
