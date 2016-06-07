
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.events.TexyEvent;

/**
 * Informational interface to tell listeners apart.
 * Aka. "normal" listener / handler.
 * 
 * @author Ondrej Zizka
 */
public interface BeforeAfterEventListener<T extends BeforeAfterEvent> extends TexyEventListener<T>
{

}// interface




