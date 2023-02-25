
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.modules.TexyModifier;

/**
 *
 * @author Ondrej Zizka
 */
public abstract class BeforeAfterEvent extends TexyParserEvent {

	public BeforeAfterEvent(TexyParser parser, String text) {
		super(parser, text);
	}
	
}
