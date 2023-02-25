
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *
 * @author Ondrej Zizka
 */
public class BlockEvent extends AroundEvent {

	private final String blockType;
	public String getBlockType() {		return blockType;	}
	
	private final String param;
	public String getParam() {		return param;	}



	

	public BlockEvent( TexyParser parser, String text, TexyModifier modifier, String blockType, String param ){
		super(parser, text, modifier);
		this.blockType = blockType;
		this.param = param;
	}

	public BlockEvent( BlockEvent orig, String blockType, String param ) {
		super(orig.parser, orig.text, orig.modifier);
		this.blockType = blockType;
		this.param = param;
	}




}
