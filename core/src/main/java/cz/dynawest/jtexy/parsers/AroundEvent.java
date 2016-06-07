
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.modules.TexyModifier;

/**
 *
 * @author Ondrej Zizka
 */
public abstract class AroundEvent extends TexyParserModEvent {

	public AroundEvent(TexyParser parser, String text, TexyModifier modifier) {
		super(parser, text, modifier);
	}

}// class
