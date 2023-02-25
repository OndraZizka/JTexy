package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.parsers.AroundEventListener
import cz.dynawest.jtexy.parsers.TexyParserEvent
import org.dom4j.Node
import java.util.*
import java.util.logging.*

/**
 * Context of event listener calls chain; used for around handlers.
 *
 * @author Ondrej Zizka
 */
class Invocation(var event: TexyParserEvent, handlers_: List<AroundEventListener<*>?>) {
    protected var handlers: List<AroundEventListener<*>?>? = null
    var iterator: Iterator<AroundEventListener<*>?>

    /** Reverses the list of handlers and initializes it's iterator.  */
    init {
        // Reversing (and thus copying) only needed if more than one (which is most cases).
        if (handlers_.size <= 1) {
            handlers = handlers_
        } else {
            //this.handlers = new ArrayList(handlers_.size());
            //Collections.copy(handlers_, this.handlers);
            handlers = ArrayList<Any?>(handlers_)
            Collections.reverse(handlers)
        }
        iterator = handlers!!.iterator()
    }

    /** Calls next handler in the queue.  */
    @Throws(TexyException::class)
    fun proceed(): Node? {
        // TBD: How would this happen?
        check(iterator.hasNext()) { "No more handlers." }
        val handler = iterator.next()
        val res = handler!!.onEvent(event)
        if (null == res) //throw new TexyInvocationException
            log.warning(
                "Event handler '" + handler.javaClass.name
                        + "' for '" + handler.eventClass + "' returned null."
            )
        return res
    } // TODO: Position in the list of handlers - integer index or an iterator?

    companion object {
        private val log = Logger.getLogger(Invocation::class.java.name)
    }
}
