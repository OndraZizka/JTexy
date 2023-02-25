
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.events.TexyEvent;

/**
 *  Parser event; Carries text and parser backref.
 * 
 * @author Ondrej Zizka
 */
public class TexyParserEvent extends TexyEvent {

	/* -- Fields -- */
	
	protected final TexyParser parser;
	public final TexyParser getParser() {		return parser;	}

	

	/** Const */
	public TexyParserEvent(TexyParser parser, String text) {
        super(text);
		this.parser = parser;
	}
	
}
