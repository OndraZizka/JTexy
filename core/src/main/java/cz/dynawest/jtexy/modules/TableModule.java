
package cz.dynawest.jtexy.modules;


import cz.dynawest.jtexy.parsers.TexyEventListener;
import java.util.logging.*;


/**
 *
 * @author Ondrej Zizka
 */
public class TableModule extends TexyModule
{
  private static final Logger log = Logger.getLogger( TableModule.class.getName() );

	private PatternHandler somePH = null;


	// -- Module meta-info -- //

	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{};
	}

	@Override	protected PatternHandler getPatternHandlerByName(String name) {
		if( somePH.getName().equals(name) ) {
			return somePH;
		} else {
			return null;
		}
	}

}// class HtmlModule
