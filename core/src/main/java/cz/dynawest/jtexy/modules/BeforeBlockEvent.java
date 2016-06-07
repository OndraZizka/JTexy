
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.BeforeAfterEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *
 * @author Ondrej Zizka
 */
public class BeforeBlockEvent extends BeforeAfterEvent {

	public BeforeBlockEvent(TexyParser parser, String text) {
		super(parser, text);
	}

}// class
