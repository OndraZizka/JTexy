package cz.dynawest.jtexy

import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.parsers.TexyEventListener
import java.util.*

/**
 * Set/Map of parse event listeners: Event class => list of listeners.
 *
 * @author Ondrej Zizka
 */
class HandlersMap<T : TexyEventListener<in TexyEvent>> {
    // Consider using Google Collections' LinkedListMultimap ?
    // Or JDK's LinkedHashSet?
    var handlersMap: MutableMap<Class<*>, MutableList<T>> = HashMap()

    /** @returns getHandlersForEvent(event.getClass());
     */
    fun <U : TexyEvent> getHandlersForEvent(event: U): List<T> {
        return getHandlersForEvent(event.javaClass)
    }

    /** @returns List of handlers for given class, or Collections.EMPTY_LIST. Never null
     */
    fun <U : TexyEvent> getHandlersForEvent(clazz: Class<U>): List<T> {
        return handlersMap[clazz] ?: return emptyList()
    }

    /** Add a handler (listener) for an event type (class).  */
    fun addHandler(handler: T) {
        val eventClass = handler.eventClass
        var list = handlersMap[eventClass]
        // Create the map entry if not there yet.
        if (null == list) {
            list = ArrayList()
            handlersMap[eventClass] = list
        } else {
            if (list.contains(handler)) return
        }
        list.add(handler)
    }
}
