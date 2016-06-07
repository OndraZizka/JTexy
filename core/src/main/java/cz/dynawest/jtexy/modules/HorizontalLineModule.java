
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.MatchWithOffset;
import java.util.List;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;

/**
 *
 * @author Ondrej Zizka
 */
public class HorizontalLineModule extends TexyModule {

	
	// --- Module meta-info --- //

	@Override protected PatternHandler getPatternHandlerByName(String name) {
		if( hrPH.getName().equals(name) )
			return hrPH;
		return null;
	}

	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{ horizLineListener };
	}





	/** Horizontal line pattern handler. */
	protected static PatternHandler hrPH = new PatternHandler() {
		@Override public final String getName(){ return "horizline"; }
		
		@Override public final Node handle( TexyParser parser,
		                              List<MatchWithOffset> groups,
																	RegexpInfo pattern) throws TexyException
		{
			TexyModifier mod = new TexyModifier( groups.get(2).match );
			HorizontalLineEvent ev = new HorizontalLineEvent(parser, null, mod);
			return parser.getTexy().invokeAroundHandlers(ev);
		}
	};


	/**
	 * 
	 */
	public static final HorizontalLineEventListener horizLineListener =
					new HorizontalLineEventListener(){
		@Override public Class getEventClass() { return HorizontalLineEvent.class; }

		@Override public Node onEvent(HorizontalLineEvent event){
			DOMElement elm = new DOMElement("hr");
			event.getModifier().decorate(elm);
			// TBD: Optional HR classes, see Texy.
			return elm;
		}
	};




}// class
