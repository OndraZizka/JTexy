
package cz.dynawest.jtexy.parsers;

import org.dom4j.dom.DOMElement;

/**
 *
 * @author Ondrej Zizka
 */
public class AfterParseEvent extends BeforeAfterEvent {

	public boolean isSingleLine;
	public DOMElement dom;

	public AfterParseEvent(TexyParser parser, boolean isSingleLine, DOMElement dom) {
		super(parser, null);
		this.isSingleLine = isSingleLine;
		this.dom = dom;
	}



}
