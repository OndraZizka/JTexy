
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import java.util.List;
import org.dom4j.dom.DOMElement;

/**
 *
 * @author Ondrej Zizka
 */
public abstract class TexyParser
{

	// Uplink to parent.
	private JTexy texy;
	public JTexy getTexy() {		return texy;	}

	protected DOMElement element;

	// TODO: Perhaps should be moved to TexyBlockParser? It would need to override e.g. BlockModule.getParser().
	boolean indented;
	public boolean isIndented() {		return indented;	}
	public void setIndented(boolean indented) {		this.indented = indented;	}

	
	protected abstract List<RegexpInfo> getPatterns();

	

	public TexyParser(JTexy texy, DOMElement element) {
		this.texy = texy;
		this.element = element;
	}


	public abstract void parse( String text ) throws TexyException;

	

}// class
