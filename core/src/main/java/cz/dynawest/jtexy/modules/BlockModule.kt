
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.Constants;
import cz.dynawest.jtexy.ContentType;
import cz.dynawest.jtexy.parsers.TexyBlockParser;
import cz.dynawest.jtexy.parsers.TexyLineParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMText;

/**
 *
 * @author Ondrej Zizka
 */
public class BlockModule extends TexyModule {


	// --- Module meta-info --- //

	@Override protected PatternHandler getPatternHandlerByName(String name) {
		if( "blocks".equals(name) )
			return blocksPatternHandler;
		return null;
	}

	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{ beforeBlockListener, blockListener };
	}



	/**
	 *  blocksPatternHandler
	 */
	private PatternHandler blocksPatternHandler = new PatternHandler() {
		@Override public String getName() { return "blocks"; }

		@Override public Node handle(TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern) throws TexyException {
			//    [1] => code | text | ...
			//    [2] => ... additional parameters
			//    [3] => .(title)[class]{style}<>
			//    [4] => ... content
			String param = groups.get(1).match;
			TexyModifier mod = new TexyModifier( groups.get(2).match );
			String content = groups.get(3).match;

			String[] parts = param.split("(?u)\\s+", 2);
			String blockType = parts.length == 0 ? "block/default" : "block/"+parts[0];
			param = parts.length >= 2 ? parts[1] : null;

			BlockEvent event = new BlockEvent(parser, content, mod, blockType, param);
			return getTexy().invokeAroundHandlers(event);
		}
	};









	/**
	 * Finish invocation.
	 * @return TexyHtml|string|FALSE
	 */
	public final BlockEventListener blockListener = new BlockEventListener(){
		
        @Override public Class getEventClass() { return BlockEvent.class; }


		
		@Override	public Node onEvent(BlockEvent event) throws TexyException
		{
			String str = event.getText();

			String blockType = event.getBlockType();

			JTexy texy = event.getParser().getTexy();

			
			if( "block/texy".equals( blockType ) ) {
				/*$el = TexyHtml::el();
				$el->parseBlock(texy, str, $parser->isIndented());
				return $el;
				 */
				DOMElement elm = new DOMElement( Constants.HOLDER_ELEMENT_NAME );
				new TexyBlockParser( texy, elm, event.getParser().isIndented() ).parse(str);
				return elm;
			}

			if( !texy.isAllowed(blockType) ) return null;

			if( "block/texysource".equals( blockType ) ) {
				str = JTexyStringUtils.outdent(str);
				if( "".equals(str) )  return new DOMText("\n");

				DOMElement elm = new DOMElement( Constants.HOLDER_ELEMENT_NAME );
				if( "line".equals( event.getParam() ))
					new TexyLineParser(texy, elm).parse(str);
				else
					new TexyBlockParser(texy, elm, false).parse(str);

				str =  JTexyStringUtils.elementToHTML(elm, texy); // $s = $el->toHtml($tx);
				blockType = "block/code";
				event = new BlockEvent(event, blockType, "html"); // To be continued (as block/code).
			}

			if( "block/code".equals( blockType ) ) {
				str = JTexyStringUtils.outdent(str);
				if( "".equals(str) )  return new DOMText("\n");
				str = JTexyStringUtils.escapeHtml(str);
				str = texy.protect( str, ContentType.BLOCK );
				DOMElement elm = new DOMElement("pre");
                event.getModifier().classes.add( event.getParam() ); // Code language.
				event.getModifier().decorate( texy, elm );
				elm.addElement("code").addText(str);
				return elm;
			}

			if( "block/default".equals( blockType ) ) {
				str = JTexyStringUtils.outdent(str);
				if( "".equals(str) )  return new DOMText("\n");
				DOMElement elm = new DOMElement("pre");
                event.getModifier().classes.add( event.getParam() ); // Code language.
				event.getModifier().decorate( texy, elm );
				str = JTexyStringUtils.escapeHtml(str);
				str = texy.protect( str, ContentType.BLOCK );
				elm.setText(str);
				return elm;
			}

			if( "block/pre".equals( blockType ) ) {
				str = JTexyStringUtils.outdent(str);
				if( "".equals(str) )  return new DOMText("\n");
				DOMElement elm = new DOMElement("pre");
				event.getModifier().decorate( texy, elm );

				TexyLineParser lp = new TexyLineParser(texy, elm);
				// Special mode - parse only HTML tags and comments.
				/* TODO
				 $tmp = $lineParser->patterns;
				$lineParser->patterns = array();
				if (isset($tmp['html/tag'])) $lineParser->patterns['html/tag'] = $tmp['html/tag'];
				if (isset($tmp['html/comment'])) $lineParser->patterns['html/comment'] = $tmp['html/comment'];
				unset($tmp);
				 */

				lp.parse(str);
				//str = elm.asXML(); //$el->getText();
				//str = Dom4jUtils.getInnerCode( elm );
				str = ProtectedHTMLWriter.fromElement( elm, BlockModule.this.getTexy().getProtector() );
				str = JTexyStringUtils.unescapeHtml(str);     // Get rid of HTML entities.
				str = JTexyStringUtils.escapeHtml(str);       // Escape HTML spec chars.
				str = texy.unprotect(str);                    // Expand protected sub-strings.
				str = texy.protect(str, ContentType.BLOCK);   // Protect the whole string.
				elm.setText(str);
				return elm;
			}

			if( "block/html".equals( blockType ) ) {
				str = StringUtils.strip(str, "\n");
				if( "".equals(str) )  return new DOMText("\n");
				DOMElement elm = new DOMElement( Constants.HOLDER_ELEMENT_NAME );
				TexyLineParser lp = new TexyLineParser(texy, elm);
				// special mode - parse only html tags
				/* TODO
				$tmp = $lineParser->patterns;
				$lineParser->patterns = array();
				if (isset($tmp['html/tag'])) $lineParser->patterns['html/tag'] = $tmp['html/tag'];
				if (isset($tmp['html/comment'])) $lineParser->patterns['html/comment'] = $tmp['html/comment'];
				unset($tmp);
				 */

				lp.parse(str);
				//str = Dom4jUtils.getInnerCode( elm ); //$el.getText();
				str = ProtectedHTMLWriter.fromElement( elm, BlockModule.this.getTexy().getProtector() );
				str = JTexyStringUtils.unescapeHtml(str);     // Get rid of HTML entities.
				str = JTexyStringUtils.escapeHtml(str);
				str = texy.unprotect(str);
				return new DOMText( texy.protect(str, ContentType.BLOCK) + "\n");
			}

			if( "block/text".equals( blockType ) ) {
				str = StringUtils.strip(str, "\n");
				if( "".equals(str) )  return new DOMText("\n");
				str = JTexyStringUtils.escapeHtml(str);
				//str = str_replace("\n", TexyHtml::el('br')->startTag() , str); // nl2br
				str = str.replace("\n", "<br />");
				return new DOMText( texy.protect(str, ContentType.BLOCK) + "\n");
			}

			if( "block/comment".equals( blockType ) ) {
				return new DOMText("\n");
			}

			if( "block/div".equals( blockType ) ) {
				str = JTexyStringUtils.outdent(str);
				if( "".equals(str) )  return new DOMText("\n");
				DOMElement elm = new DOMElement("div");
				event.getModifier().decorate(texy, elm);
				//$el->parseBlock(texy, str, $parser->isIndented()); // TODO: INDENT or NORMAL ?
				new TexyBlockParser(texy, elm, event.getParser().isIndented() ).parse(str);
				return elm;
			}

			return null;

		}

	};



	/**
	 * Single block pre-processing.
	 */
	public final BeforeBlockEventListener<BeforeBlockEvent> beforeBlockListener = new BeforeBlockEventListener<BeforeBlockEvent>(){
		@Override public Class getEventClass() { return BeforeBlockEvent.class; }

		@Override	public Node onEvent(BeforeBlockEvent event ){
			// Autoclose exclusive blocks.
			/*$text = preg_replace(
				'#^(/--++ *+(?!div|texysource).*)$((?:\n.*+)*?)(?:\n\\\\--.*$|(?=(\n/--.*$)))#mi',
				"\$1\$2\n\\--", 	$text 	); */
			String text = event.getText().replaceAll(
                /*  /--<something> - not div or texysource
                 *  followed by 0+ lines reluctantly
                 *  and then (takes first) either 
                 *  \--... or /--...
                 */
                "(?:mi)^(/--++ *+(?!div|texysource).*)$" +
                "((?:\\n.*+)*?)" +
                "(?:\\n\\\\--.*$|(?=(\\n/--.*$)))",
                // Normalize to /--... <content> \--
                "$1$2\n\\--" );
			event.setText( text );
			return new DOMText(text);
		}
	};




}


