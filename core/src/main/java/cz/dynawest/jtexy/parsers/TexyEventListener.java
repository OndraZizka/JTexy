
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.events.TexyEvent;
import org.dom4j.Node;

/**
 *
 * @author Ondrej Zizka
 */
public abstract interface TexyEventListener<T extends TexyEvent> {

	public Class getEventClass();

	public Node onEvent( T event ) throws TexyException;

}// class
