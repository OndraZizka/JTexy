
package cz.dynawest.jtexy.modules;


import cz.dynawest.jtexy.Constants;
import cz.dynawest.jtexy.ContentType;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.RegexpPatterns;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter;
import cz.dynawest.jtexy.modules.TexyLink.Type;
import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.AroundEventListener;
import cz.dynawest.jtexy.parsers.BeforeAfterEventListener;
import cz.dynawest.jtexy.parsers.BeforeParseEvent;
import cz.dynawest.jtexy.parsers.LinkProcessEvent;
import cz.dynawest.jtexy.parsers.TexyLineParser;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.jtexy.util.StringsReplaceCallback;
import cz.dynawest.jtexy.util.UrlChecker;
import cz.dynawest.openjdkregex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMText;


/**
 *
 * @author Ondrej Zizka
 */
public class LinkModule extends TexyModule
{
  private static final Logger log = Logger.getLogger( LinkModule.class.getName() );


	// --- Module metainfo --- //
	
	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{ 
			linkListener,
			linkRefListener,
			linkProcessListener,
			NEW_REF_LISTENER,
			beforeParseListener,
		};
	}

	@Override protected PatternHandler getPatternHandlerByName( String name ){
		// [reference]
		if( "reference".equals(name) )  return referencePH;
		// Direct url or email.
		if( "urlOrEmail".equals(name) ) return URL_OR_MAIL_PH;
		return null;
	}



	// References map - predefined links.
	private Map<String, TexyLink> refs = new HashMap();
	protected void addRef( String key, TexyLink link ){
		this.refs.put( key.toLowerCase(), link);
	}
	protected TexyLink getRef( String key ){
		TexyLink link = this.refs.get(key.toLowerCase());
		if( null == link )  return null;
		return link.clone(); // Cloning because we will change it.
	}



	// --- PatternHandler's --- //

	/**
	 * Callback for: [ref].
	 * Fires either a NewReference or a LinkReference event.
	 */
	private PatternHandler referencePH = new PatternHandler() {
		@Override public String getName() { return "reference"; }

		@Override public Node handle(TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern) throws TexyException {
			//    [1] => [ref]
			String refName = StringUtils.substring( groups.get(0).match, 1, -1 );

			TexyLink link = getRef(refName);
			// New reference encountered?
			if( null == link )
				return getTexy().invokeAroundHandlers( new NewReferenceEvent(parser, refName) );

			link.type = Type.BRACKET;

			String content = null;

			// Label is not empty?
			if( ! StringUtils.isEmpty(link.label) ){
				// Prevent circular references. TBD: Analyze.
				if( liveLock.contains( link.name ) )
					content = link.label;
				else {
					liveLock.add( link.name );
					DOMElement elm = new DOMElement( Constants.HOLDER_ELEMENT_NAME );
					new TexyLineParser(getTexy(), elm).parse( link.label );
					content = ProtectedHTMLWriter.fromElement( elm, getTexy().getProtector() );
					liveLock.remove( link.name );
				}
			}
			// Label is empty...
			else {
				content = link.asText( true, true );
				content = getTexy().protect( content, ContentType.TEXTUAL );
			}

			return getTexy().invokeAroundHandlers( new LinkReferenceEvent(parser, content, link) );
		}
	};

	/** Used by the referencePH. TBD: Analyze. */
	private final Set<String> liveLock = new HashSet();



	/**
	 * Callback for: http://davidgrudl.com   david@grudl.com.
	 * Fires a LinkEvent.
	 */
	private  static final PatternHandler URL_OR_MAIL_PH = new PatternHandler() {
		@Override	public String getName() { return "urlOrEmail"; }

		@Override	public Node handle(TexyParser parser, List<MatchWithOffset> groups, RegexpInfo ri) throws TexyException {
			//    [0] => URL
			if(log.isLoggable(Level.FINEST))
			for (MatchWithOffset match : groups)
				log.finest("  "+match.toString());

			TexyLink link = TexyLink.fromString(groups.get(0).match);
			LinkModule.fixLink( link );

			log.finest( link.toString() );

			boolean isEmail = "link/email".equals( ri.name );
			return parser.getTexy().invokeAroundHandlers( new LinkEvent(parser, link, isEmail) );
		}
	};





	// --- EventListener's --- //


	/**
	 * Before parse event listener, which resets module's internals
	 * and parses and removes the link definitions.
	 */
	private BeforeAfterEventListener<BeforeParseEvent> beforeParseListener
					= new BeforeAfterEventListener<BeforeParseEvent>() {
		@Override	public Class getEventClass(){ return BeforeParseEvent.class; }
		@Override	public Node onEvent(BeforeParseEvent event) throws TexyException {

			// Reset this module's internals.
			liveLock.clear();

			// [la trine]: http://www.latrine.cz/ text odkazu .(title)[class]{style}
			if( getTexy().options.useLinkDefinitions ){
				// Parses the reference defs, add them to this, and removes them from the text.
				String text = event.getText();
				text = JTexyStringUtils.replaceWithCallback(text, PAT_LINK_DEFINITION, REF_DEF_CALLBACK);
				event.setText( text );
			}

			return null;
		}


	};

	/** Link definition pattern */
	private static final Pattern PAT_LINK_DEFINITION = Pattern.compile(
        "^\\[([^\\[\\]#\\?\\*\\n]+)\\]: +(\\S+)(\\ .+)?"+RegexpPatterns.TEXY_MODIFIER+"?\\s*()$",
        Pattern.MULTILINE | Pattern.UNGREEDY | Pattern.UNICODE_CASE );

	/** Reference definition callback for BeforeAfterEventListener. */
	private final StringsReplaceCallback REF_DEF_CALLBACK = new StringsReplaceCallback() {
		@Override public String replace(String[] groups) {
			//    [1] => [ (reference) ]
			//    [2] => link
			//    [3] => ...
			//    [4] => .(title)[class]{style}
			TexyLink link = new TexyLink( groups[2] );
			link.label =  groups[3];
			link.modifier = new TexyModifier( groups[4] );
			fixLink( link );
			LinkModule.this.addRef( groups[1], link );
			return "";
		}
	};



	/** New reference event. */
	public static class NewReferenceEvent extends AroundEvent {
		public NewReferenceEvent(TexyParser parser, String refName) {
			super(parser, refName, null);
		}
		public String getRefName(){ return this.getText(); } // Alias.
	}

	/** New reference event listener - no-op. */
	private static final TexyEventListener<NewReferenceEvent> NEW_REF_LISTENER
					= new AroundEventListener<NewReferenceEvent>() {
		@Override public Class getEventClass() { return NewReferenceEvent.class; }
		@Override public Node onEvent(NewReferenceEvent event) throws TexyException {
			return null;
		}
	};


	
	/** Link reference event. */
	public static class LinkReferenceEvent extends AroundEvent {

		TexyLink link;

		public LinkReferenceEvent(TexyParser parser, String content, TexyLink link) {
			super(parser, content, null);
			this.link = link;
		}
	}

	/** Link reference event listener.   Texy: solve() */
	private TexyEventListener<LinkReferenceEvent> linkRefListener
					= new AroundEventListener<LinkReferenceEvent>() {
		@Override public Class getEventClass() { return LinkReferenceEvent.class; }
		@Override public Node onEvent( LinkReferenceEvent event ) throws TexyException {
			return LinkModule.this.solveLinkReference( event.link, event.getText() );
		}
	};



	/**
	 *  LinkEvent - for both URL and e-mail.
	 */
	public static class LinkEvent extends AroundEvent {
		
		TexyLink link;
		boolean isEmail;

		public LinkEvent(TexyParser parser, TexyLink link, boolean isEmail) {
			super(parser, null, null);
			this.link = link;
			this.isEmail = isEmail;
		}
	}

	/** LinkEvent listener. */
	private TexyEventListener<LinkEvent> linkListener
					= new AroundEventListener<LinkEvent>()
	{
		@Override public Class getEventClass() { return LinkEvent.class; }
		@Override public Node onEvent(LinkEvent event) throws TexyException {
			String content = event.link.asText(
							getTexy().options.makeAutoLinksShorter,
							getTexy().options.obfuscateEmails );
			content = getTexy().protect( content, ContentType.TEXTUAL );
			return solveLinkReference( event.link, content );
		}
	};






	/**
	 * Instead of factoryLink($dest, $mMod, $label).
	 * TBD: Refactorize to get rid of inter-module calls.
	 *      We will possibly need to modify the processing.
	 */
	private AroundEventListener<LinkProcessEvent> linkProcessListener = new AroundEventListener<LinkProcessEvent>() {

		@Override	public Class getEventClass() { return LinkProcessEvent.class; }

		@Override public Node onEvent(LinkProcessEvent event) throws TexyException {

			TexyLink link = null;
			Type linkType = Type.COMMON;

			String dest = event.dest;

			// References.
			if( dest.length() > 1 && dest.charAt(0) == '[' ){
				// [ref]
				if(dest.charAt(1) != '*' ){
					linkType = Type.BRACKET;
					dest = StringUtils.substring(dest, 1,-1);
					link = getRef( dest );
				}
				// [* image *] - not supported yet.
				else {
					linkType = Type.IMAGE;
					dest = StringUtils.substring(dest, 2,-2).trim();
					link = event.linkProvider.getLink(dest);
				}
			}

			// Normal link or an unresolved reference.
			if( null == link ){
				link = new TexyLink(dest.trim());
				fixLink(link);
			}


			// TBD: What is this feature? Seems like expanding of some URL shortcut.
			if( event.label != null  &&  link.url.contains("%s") ){
				String res = JTexyStringUtils.stringToText( event.label, null );
				res = JTexyStringUtils.encodeUrl( res );
				link.url = link.url.replace("%s", res);
			}

			link.modifier = event.getModifier();
			link.type = linkType;

			event.link = link;
			return null;
		}

	};



	/**
	 * Prepare link for a citation.
	 *
	 * TBD: Unify all these ***Link() methods not to be spread over modules.
	 */
	public String citeLink( String linkStr ){
		if( null == linkStr ) return null;

		// [ref]
		if( linkStr.charAt(0) == '[' ){
			linkStr = StringUtils.substring(linkStr, 1, -1);
      // @ Calling wildly across the modules :-/
			TexyLink ref = this.getRef( linkStr );
			if( null != ref ) return JTexyStringUtils.prependUrlPrefix(
							getTexy().options.linkRootUrl, ref.url );
		}

		// Special supported case. TBD: Checked several times? See fixLink().
		if( linkStr.startsWith("www.") ) return "http://" + linkStr;

		// Else just return the URL.
		return JTexyStringUtils.prependUrlPrefix(
							getTexy().options.linkRootUrl, linkStr );
	}





	/**
	 * Checks and corrects $URL.
	 * @param  TexyLink
	 * @return void
	 */
	private static void fixLink( TexyLink link )
	{
		// Remove soft hyphens; if not removed by Texy::process().
		link.url = link.url.replace("\u00C2\u00AD", "");

		// www.
		if( StringUtils.startsWithIgnoreCase( link.url, "www." ) ) {
			link.url = "http://" + link.url;
		}
		// e-mail
		else if( link.url.matches( "(?u)^"+RegexpPatterns.TEXY_EMAIL+"$") ){
			link.url = "mailto:"+link.url;
		}
		else if( ! UrlChecker.checkURL(link.url, UrlChecker.Type.ANCHOR) ){
			link.url = null;
		}
		else {
			// Replace unwanted &amp; .
			link.url = link.url.replace("&amp;", "&");
		}
	}



    /**
     *  Convenience method.
     *  @returns solveLinkReference(link, new DOMText(content)); 
     */
	public Node solveLinkReference( TexyLink link, String content ){
        return solveLinkReference(link, new DOMText(content));
    }

	/**
	 *  Creates a <a href="..." ...>  element for the given link.
     * 
     *  Needs to be public as it's called from PhraseModule (hack from Texy).
	 */
	public Node solveLinkReference( TexyLink link, Node content ){
		
			log.finer("Link: "+link+" Content: "+content);

			if( link.url == null )
				return content;

			DOMElement elm = new DOMElement("a");

			boolean nofollow = false;
			boolean popup = false;

            // Modifier.
			if( link.modifier != null ){
				nofollow = link.modifier.classes.remove("nofollow");
				popup = link.modifier.classes.remove("popup");
				link.modifier.decorate(elm);
			}

			// href="..."
			{
				String href;
				if( link.type == TexyLink.Type.IMAGE ){
					href = JTexyStringUtils.prependUrlPrefix( getTexy().options.imageRootUrl, link.url );
					//elm.setAttribute("onclick", this.imageOnClick);
				}
				else {
					href = JTexyStringUtils.prependUrlPrefix( getTexy().options.linkRootUrl, link.url );
					// Nofollow.
					if( nofollow || getTexy().options.forceNofollow && elm.getAttribute("href").contains("//") )
						elm.setAttribute("rel", "nofollow");
				}
				log.finest("Resulting href = " + href);
				elm.setAttribute("href", href);
			}

			// onclick popup
			if( popup )
				elm.setAttribute("onclick", getTexy().options.popupOnclick);

			if( content != null )
				elm.add( content );

			// TBD: Move to JTexy or DocumentContext?
			allDocumentLinks.add( elm.getAttribute("href") );

			return elm;
			// TBD: Move options to this module?
	}


	/** Used in {@link #solveLinkReference()}. */
	public final List<String> allDocumentLinks = new ArrayList();



	/**  Returns reference using this module's link reference map. */
	public final LinkProvider LINK_PROVIDER = new LinkProvider() {
		@Override public TexyLink getLink(String key) {
			return getRef(key);
		}
	};


}// class LinkModule
