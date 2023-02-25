
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.modules.TexyModifier;

/**
 *
 * @author Ondrej Zizka
 */
public class TexyParserModEvent extends TexyParserEvent {

	protected TexyModifier modifier;
	public TexyModifier getModifier() {		return modifier;	}
	

	public TexyParserModEvent( TexyParser parser, String text, TexyModifier modifier ){
		super(parser, text);
		this.modifier = modifier;
	}



}
