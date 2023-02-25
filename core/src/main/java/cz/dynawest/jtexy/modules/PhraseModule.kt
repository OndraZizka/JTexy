
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.ContentType;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.LinkProcessEvent;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.jtexy.util.MatchWithOffset;
import java.util.*;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMText;




/**
 *  Handles phrases, of which most can have 
 *     1) Texy modifier
 *     2) a link attached following after ':'
 *  as noted  below:
 * 
 *      <pre>
                                    Mod?     Link?      Example
        # ***strong+emphasis***     MOD      LINK    
        # **strong**                MOD      LINK
        # //emphasis//              MOD      LINK
        # *emphasisAlt*             MOD      LINK
        # *emphasisAlt2*            MOD      LINK
        # ++inserted++              MOD      
        # --deleted--               MOD      
        # ^^superscript^^           MOD      
        # m^2 alternative superscript
        # __subscript__             MOD      
        # m_2 alternative subscript
        # "span"                    MOD      LINK
        # ~alternative span~        MOD      LINK
        # ~~cite~~                  MOD      LINK
        # >>quote&lt;&lt;           MOD      LINK
        # acronym/abbr                                   "et al."((and others))
        # acronym/abbr                                   NATO((North Atlantic Treaty Organisation))
        # ''notexy''
        # `code`                    MOD      LINK
        # ....:LINK                          LINK
        </pre>
 
 * @author Ondrej Zizka
 */
public class PhraseModule extends TexyModule
{
    private static final Logger log = Logger.getLogger( PhraseModule.class.getName() );


	// --- Module meta-info --- //

	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{ phraseListener };
	}

	/** Return this module's pattern handler by name. */
	@Override protected PatternHandler getPatternHandlerByName( String name ){
		if( "patternPhrase".equals(name) || "default".equals(name) ){
			return patternPhrase;
		}else if( "patternNoTexy".equals(name) ){
			return PATTERN_NO_TEXY;
		}else if( "patternSubSup".equals(name) ){
			return PATTERN_SUB_SUP;
		}else{
			throw new UnsupportedOperationException("Unknown pattern handler: " + name);
		}
	}



	// --- Settings --- //

    /** Html links allowed? */
    public boolean isLinksAllowed() { return linksAllowed;  }
    public void setLinksAllowed( boolean linksAllowed ) { this.linksAllowed = linksAllowed;  }
    protected boolean linksAllowed = true;

    
    
    

	/**
	 *  patternPhrase handler.
	 */
	public final PatternHandler patternPhrase = new PatternHandler()
	{
		@Override public String getName(){ return "patternPhrase"; }


		/**  TODO: Call solve(). */
		@Override public Node handle( TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern ) throws TexyException {
			
			//    [1] => ** - probably means "enclosing string"; ignored in Texy.
			//    [2] => ...
			//    [3] => .(title)[class]{style}
			//    [4] => LINK
			for (MatchWithOffset match : groups) {
				log.finest("  "+match.toString()); ///
			}

            if( getRegexpInfo("phrase/span") != null ){
                log.finer( pattern.getPerlRegexp() );
                log.finer( pattern.getRegexp() );
            }

            String content = groups.get(1).match;
			String modStr = groups.get(2).match;
			TexyModifier mod = new TexyModifier( modStr );
			String linkStr = (groups.size() <= 3) ? null : groups.get(3).match;
			TexyLink link = TexyLink.fromString( linkStr );


			// TBD: What is this good for?
			//parser.again = "phrase/code".equals(pattern.name) || "phrase/quicklink".equals(pattern.name);
			
			if( pattern.name.startsWith("phrase/span") ){
				if( linkStr == null ){
					if( null == modStr ) return null; // Leave intact.
				}
				else{
					// TBD: Why the hell is a link handled in a PhraseModule?!
					// $link = $tx->linkModule->factoryLink($mLink, $mMod, $mContent);
					LinkProcessEvent linkProcessEvent = new LinkProcessEvent(
									parser, mod, modStr, linkStr, content, null);
					getTexy().invokeAroundHandlers( linkProcessEvent );
					link = linkProcessEvent.getLink();
				}
			}
			else if( pattern.name.startsWith("phrase/acronym") ){
				mod.title = JTexyStringUtils.unescapeHtml( linkStr.trim() );
			}
			else if( pattern.name.startsWith("phrase/quote") ){
				mod.cite = getTexy().linkModule.citeLink( linkStr );
			}
			// Default case: create an ordinary link.
			else if( linkStr != null ){
				//link = getTexy().linkModule.factoryLink( linkStr );
				// @ Fire a processLinkEvent instead of calling LinkModule directly.
				LinkProcessEvent linkEvent = new LinkProcessEvent(
								parser, mod, modStr, linkStr, null, null);
				parser.getTexy().invokeAroundHandlers(linkEvent);
				link = linkEvent.getLink();
			}


			//DOMElement elm = new DOMElement( pattern.htmlElement );
			//elm.addText( content );
			//Node node = solve( pattern.name, content, mod, link );

			PhraseEvent event = new PhraseEvent( parser, content, mod, link, pattern.name );
			Node node = getTexy().invokeAroundHandlers( event );

			return node;
		}


	};


	/**
	 *  patternSubSup handler.  - __sub__  ^^sup^^  m^2  CO_2
     *  TODO: Implement.
	 */
	private static final PatternHandler PATTERN_SUB_SUP = new PatternHandler()
	{
		@Override public String getName(){ return "patternSubSup"; }

		@Override
		public Node handle( TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern ){
			
			for (MatchWithOffset match : groups) {
				log.finer("  "+match.toString()); ///
			}

			throw new UnsupportedOperationException("Not supported yet.");
		}
	};


	/**
	 *  patternNoTexy handler.
     *  TODO: Implement.
	 */
	private static final PatternHandler PATTERN_NO_TEXY = new PatternHandler()
	{
		@Override public String getName(){ return "patternNoTexy"; }

		@Override
		public Node handle( TexyParser parser,List<MatchWithOffset> groups, RegexpInfo pattern ){

			for (MatchWithOffset match : groups) {
				log.finer("  "+match.toString()); ///
			}

			throw new UnsupportedOperationException("Not supported yet.");
		}
	};





	


	/**
	 *  Phrase Listener.
	 */
	PhraseEventListener phraseListener = new PhraseEventListener() {
		@Override public Class getEventClass() { return PhraseEvent.class; }

		@Override public Node onEvent(PhraseEvent event) throws TexyException {
			return solve( event.getPhraseName(), event.getText(), event.getModifier(), event.getLink());
		}
	};





	/**
	 *  Solve.
	 * @param phrase     Phrase ID.
	 * @param content    Text content to process.
	 * @param modifier   Texy content modifier .{...}[...](...)<>^ etc.
	 * @param link       ???  Link is probably always null - see the patterns.
	 * @return
	 */
	private Node solve( String phrase, String content, TexyModifier modifier, TexyLink link ) {

		RegexpInfo ri = this.getRegexpInfo( phrase );
		String elmName = (ri == null) ? null : ri.htmlElement;
		

		if( "a".equals( elmName ) )
			elmName = ( (link != null) && this.linksAllowed ) ? null : "span";

		if( "phrase/code".equals(phrase) )
			content = this.getTexy().protect( StringEscapeUtils.escapeHtml( content ), ContentType.TEXTUAL );

		Node nodeRet = null;


		if( "phrase/strong+em".equals( phrase ) ) {
			DOMElement elmRet = new DOMElement( this.getRegexpInfo("phrase/strong").htmlElement );
			DOMElement eEm = new DOMElement(this.getRegexpInfo("phrase/em").htmlElement);
			eEm.setText( content );
			elmRet.add( eEm );
			modifier.decorate( elmRet );
			nodeRet = elmRet;
		}
		else if( null != elmName ) {
			DOMElement elmRet = new DOMElement( elmName );
			elmRet.setText( content );
			modifier.decorate( elmRet );
			if( "q".equals( elmName ) )
				elmRet.setAttribute("cite", modifier.cite);
			nodeRet = elmRet;
		}
		else {
            // Trick - put's whole content 
			nodeRet = new DOMText( content );
		}

		//* TODO: Without this, links don't work!
        if( (link != null) && this.linksAllowed )
			return getTexy().linkModule.solveLinkReference( link, nodeRet );
		/**/

		return nodeRet;

	}



}
