
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *
 * @author Ondrej Zizka
 */
public class HeadingEvent extends AroundEvent {

	public int level;
	public boolean isSurrounded;

	public HeadingEvent(TexyParser parser, String text, TexyModifier modifier, int level, boolean isSurrounded) {
		super(parser, text, modifier);
		this.level = level;
		this.isSurrounded = isSurrounded;
	}

	

}
