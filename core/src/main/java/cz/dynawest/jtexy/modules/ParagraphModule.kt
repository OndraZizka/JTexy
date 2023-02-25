
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.*;
import cz.dynawest.jtexy.parsers.TexyLineParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.dom4j.Dom4jUtils;
import cz.dynawest.openjdkregex.Pattern;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.*;




/**
 *
 * @author Ondrej Zizka
 */
public class ParagraphModule extends TexyModule
{
  private static final Logger log = Logger.getLogger( ParagraphModule.class.getName() );


	// -- Module metainfo -- //

	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{ parListener };
	}

	/*
	@Override protected void onRegister() {
		getTexy().getAroundHandlers().addHandler( parListener );
	}*/
	
	@Override protected PatternHandler getPatternHandlerByName(String name) {
		return null; // No patterns in this module.
	}



	// -- EventListener stuff -- //

	ParagraphEventListener parListener = new ParagraphEventListener() {
		@Override public Class getEventClass() { return ParagraphEvent.class; }

		@Override public Node onEvent(ParagraphEvent event) throws TexyException {
			return solve( null, event.getText(), ((ParagraphEvent)event).getModifier());
		}
	};


	private static final Pattern TM_PAT =
					Pattern.compile("(?u)[^\\s"+RegexpPatterns.TEXY_MARK+"]");

	/**
	 *  Creates a DOM node for this paragraph.
	 */
	public DOMElement solve( Invocation invocation, String content, TexyModifier modifier ) throws TexyException
	{

		// Find hard linebreaks.
		if( this.getTexy().options.mergeLines ) {
			// ....
			//  ...  => \r means break line
			content = content.replaceAll("\\n +(?=\\S)", "\r");
		} else {
			content = content.replace("\n", "\r");
		}

		DOMElement elm = new DOMElement("p");
		//$el->parseLine($tx, $content);
		//$content = $el->getText(); // string
		// TODO: Debug. Hapruje to.
		new TexyLineParser(this.getTexy(), elm).parse( content );

		// Get the code out of the element,
		// process it textually, and put it back.

		//content = Dom4jUtils.getInnerCode( elm );
		//content = ProtectedHTMLWriter.fromElement( elm, getTexy().getProtector() );
		// Something like "new ProtectedHTMLWriter( Protector ).fromElement( elm )" might be cleaner...
		content = Dom4jUtils.getInnerText( elm );

		log.finer("Paragraph content after parse: "+content);
		

		// Check content type.

		// Block contains block tag.
		if( -1 != content.indexOf( ContentType.BLOCK.getDelim() ) ) {
			elm.setName( Constants.HOLDER_ELEMENT_NAME );  // ignores modifier!
		}
		// Block contains text (protected) - leave element p.
		else if( -1 != content.indexOf( ContentType.TEXTUAL.getDelim() ) ) {
		}
		// Block contains text - leave element p.
		else if( TM_PAT.matcher(content).find() ) {
		}
		// Block contains only a replaced element.
		else if( -1 != content.indexOf( ContentType.REPLACED.getDelim() ) ) {
			elm.setName( this.getTexy().options.nontextParagraph );
		}
		// Block contains only markup tags or spaces or nothing.
		else {
			// if{ ignoreEmptyStuff } return FALSE;
			if( null == modifier ) elm.setName( Constants.HOLDER_ELEMENT_NAME );
		}


		// If not holder element...
		if( !Constants.HOLDER_ELEMENT_NAME.equals( elm.getName() ) ) {
			// Apply the modifier.
			if( null != modifier ) modifier.decorate( elm );

			// Add <br />.
			if( -1 != content.indexOf("\r") ) {
				String key = getTexy().protect( "<br />", ContentType.REPLACED );
				content = content.replace("\r", key);
			}
		}

		content = StringUtils.replaceChars( content, "\r\n", "  " );
		elm.setText( content );

		return elm;
	}

	


	
}
