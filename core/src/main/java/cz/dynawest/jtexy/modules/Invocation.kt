
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.AroundEventListener;
import cz.dynawest.jtexy.parsers.TexyParserEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.dom4j.Node;


/**
 * Context of event listener calls chain; used for around handlers.
 *
 * @author Ondrej Zizka
 */
public class Invocation
{
    private static final Logger log = Logger.getLogger( Invocation.class.getName() );

    protected List<AroundEventListener> handlers;
    Iterator<AroundEventListener> iterator;
    TexyParserEvent event;


    /** Reverses the list of handlers and initializes it's iterator. */
    public Invocation( TexyParserEvent event, List<AroundEventListener> handlers_) {
        this.event = event;
        // Reversing (and thus copying) only needed if more than one (which is most cases).
        if( handlers_.size() <= 1 ){
            this.handlers = handlers_;
        } else {
            //this.handlers = new ArrayList(handlers_.size());
            //Collections.copy(handlers_, this.handlers);
            this.handlers = new ArrayList(handlers_);
            Collections.reverse(this.handlers);
        }
        this.iterator = this.handlers.iterator();
    }


    /** Calls next handler in the queue. */
    public Node proceed() throws TexyException {
        // TBD: How would this happen?
        if( ! this.iterator.hasNext() )
            throw new IllegalStateException("No more handlers.");

        AroundEventListener handler = this.iterator.next();
        Node res = handler.onEvent( this.event );
        if( null == res )
            //throw new TexyInvocationException
            log.warning(
                "Event handler '"+handler.getClass().getName()
                +"' for '"+handler.getEventClass()+ "' returned null.");
        return res;

    }

    // TODO: Position in the list of handlers - integer index or an iterator?

}
