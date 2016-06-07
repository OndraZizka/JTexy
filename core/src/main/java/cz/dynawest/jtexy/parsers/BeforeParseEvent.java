
package cz.dynawest.jtexy.parsers;

import java.io.Serializable;




/**
 *
 * @author Ondrej Zizka
 */
public class BeforeParseEvent extends BeforeAfterEvent implements Serializable
{

	public BeforeParseEvent(TexyParser parser, String text) {
		super(parser, text);
	}
  

}// class BeforeParseEvent
