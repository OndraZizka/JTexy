
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *
 * @author Ondrej Zizka
 */
public class ParagraphEvent extends AroundEvent {

	public ParagraphEvent(TexyParser parser, String text, TexyModifier modifier) {
		super(parser, text, modifier);
	}

}
