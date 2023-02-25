
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *
 * @author Ondrej Zizka
 */
public class ImageEvent extends AroundEvent {

	ImageModule.ImageInfo img;
	TexyLink link;

	public ImageEvent(TexyParser parser,
					ImageModule.ImageInfo img,
					TexyLink link,
					TexyModifier modifier) {
		super(parser, null, modifier);
		this.img = img;
		this.link = link;
	}

}
