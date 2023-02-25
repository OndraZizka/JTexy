
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *
 * @author Ondrej Zizka
 */
public class PhraseEvent extends AroundEvent {

	private TexyLink link;
	public TexyLink getLink() {		return link;	}

	private String phraseName;
	public String getPhraseName() {		return phraseName;	}
	

	public PhraseEvent(TexyParser parser, String text, TexyModifier modifier, TexyLink link, String phraseName) {
		super(parser, text, modifier);
		this.link = link;
		this.phraseName = phraseName;
	}

}
