
package cz.dynawest.jtexy;

import cz.dynawest.jtexy.events.TexyEvent;
import cz.dynawest.jtexy.parsers.TexyParserEvent;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import java.util.*;


/**
 * Set/Map of parse event listeners: Event class => list of listeners.
 *
 * @author Ondrej Zizka
 */
public class HandlersMap<T extends TexyEventListener> {

    // Consider using Google Collections' LinkedListMultimap ?
    // Or JDK's LinkedHashSet?
    Map<Class, List<T>> handlersMap = new HashMap();

    /** @returns getHandlersForEvent(event.getClass()); */
    public <U extends TexyEvent> List<T> getHandlersForEvent( U event ) { return getHandlersForEvent(event.getClass()); }
    
    /** @returns List of handlers for given class, or Collections.EMPTY_LIST. Never null */
    public <U extends TexyEvent> List<T> getHandlersForEvent( Class<U> clazz ) {
        List<T> handlers = handlersMap.get(clazz);
        if( handlers == null )  return Collections.EMPTY_LIST;
        return handlers;
    }

    /** Add a handler (listener) for an event type (class). */
    public void addHandler( T handler ) {
        Class eventClass = handler.getEventClass();
        List<T> list = this.handlersMap.get( eventClass );
        // Create the map entry if not there yet.
        if( null == list ){
            list = new ArrayList();
            this.handlersMap.put( eventClass, list );
        }
        // Skip if we already have this one registered.
        else{
            if( list.contains( handler ) )  return;
        }
        list.add( handler );
    }
	

}// class
