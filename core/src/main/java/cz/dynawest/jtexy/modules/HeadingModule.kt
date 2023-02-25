
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.events.TexyEvent;
import cz.dynawest.jtexy.parsers.AfterParseEvent;
import cz.dynawest.jtexy.parsers.AfterParseListener;
import cz.dynawest.jtexy.parsers.BeforeParseEvent;
import cz.dynawest.jtexy.parsers.BeforeParseListener;
import cz.dynawest.jtexy.parsers.TexyLineParser;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.Debug;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.jtexy.util.MatchWithOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;

/**
 * Heading module - takes care of headings:
 * 
 * <pre>
 * Heading
 * =======
 *  
 * ###  Heading ###
 * </pre>
 * 
 * Handlers and listeners:
 * 
 * <li> private PatternHandler underlinedHeadingPH = new PatternHandler()
 * <li> private PatternHandler surroundedHeadingPH = new PatternHandler()
 * <li> private HeadingEventListener headingListener = new HeadingEventListener()
 * <li> private AfterParseListener afterParse = new AfterParseListener()
 * 
 * @author Ondrej Zizka
 */
public class HeadingModule extends TexyModule {
    private static final Logger log = Logger.getLogger( HeadingModule.class.getName() );
    
	// TODO: Create some `ParsingContext` for each parsed document
	//       and store these state variables there.

    public static class Options {
        
        /** More chars in surrounded heading means bigger heading. */
        public boolean moreMeansHigher = false;

        public boolean generateIDs = true;
        public String idPrefix = "toc-";

        public enum Balancing { DYNAMIC, STATIC }
        public Balancing balancing = Balancing.DYNAMIC;
    }
    
    protected static class Context {
        // Context state.
        public String documentTitle;

        /** Level of the top heading. */
        protected int topLevel = 1;

        /**
         * TOC.  Re-set in BeforeParseListener. 
         * TODO: Make thread-safe.
         */
        protected List<HeadingInfo> toc = new LinkedList();

        /** Used auto-generated TOC IDs. */
        protected Set<String> usedIDs = new HashSet<String>();
    }
    
    
    public final Options opt = new Options();
    
    public final Context ctx = new Context();





	// --- Module meta-info --- //

	@Override public TexyEventListener[] getEventListeners() {
		return new TexyEventListener[]{
			beforeParse,
			headingListener,
			afterParse
		};
	}

	@Override	protected PatternHandler getPatternHandlerByName(String name) {
		if( "surrounded".equals(name) ) return surroundedHeadingPH;
		if( "underlined".equals(name) ) return underlinedHeadingPH;
		return null;
	}



	/**
	 * Callback for underlined heading.
	 *
	 *  Heading .(title)[class]{style}>
	 *  -------------------------------
	 */
	private PatternHandler underlinedHeadingPH = new PatternHandler() {
		@Override public String getName(){ return "underlined"; }

		@Override	public Node handle(TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern) throws TexyException {

			//    [1] => Heading content.
			//    [2] => .(title)[class]{style}<>
			//    [3] => Underline chars.

			char underChar = groups.get(3).match.charAt(0);
			int level = 1;
			switch( underChar ){
				case '#': level = 0; break;
				case '*': level = 1; break;
				case '=': level = 2; break;
				case '-': level = 3; break;
			}
			TexyModifier mod = new TexyModifier(groups.get(2).match);
			HeadingEvent event = new HeadingEvent(parser, groups.get(1).match, mod, level, false);
			return getTexy().invokeAroundHandlers( event );
		}
	};


	/**
	 * Callback for surrounded heading.
	 *
	 *   ### Heading .(title)[class]{style}>
	 *
	 * @param  TexyBlockParser
	 * @param  array      regexp matches
	 * @param  string     pattern name
	 * @return TexyHtml|string|FALSE
	 */
	private PatternHandler surroundedHeadingPH = new PatternHandler() {
		@Override public String getName(){ return "surrounded"; }

		@Override	public Node handle(TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern) throws TexyException {
			//    [1] => ###
			//    [2] => Content.
			//    [3] => .(title)[class]{style}<>
			String lineChars = groups.get(1).match;
			String content = groups.get(2).match;
			TexyModifier mod = new TexyModifier(groups.get(3).match);

			int level = Math.min(7, Math.max(2, lineChars.length() ));
			level = HeadingModule.this.opt.moreMeansHigher ? 7 - level : level - 2;
			content = StringUtils.stripEnd(content, lineChars.charAt(0)+" ");

			HeadingEvent event = new HeadingEvent(parser, content, mod, level, true);
			return getTexy().invokeAroundHandlers(event);
		}
	};





	/**
	 * Heading listener.
	 */
	private HeadingEventListener headingListener = new HeadingEventListener() {
		@Override public Class getEventClass() { return HeadingEvent.class; }

		@Override public Node onEvent(HeadingEvent event) throws TexyException {
			int level = event.level + HeadingModule.this.ctx.topLevel;
			level = Math.max(1, level);
			level = Math.min(6, level);
			DOMElement elm = new DOMElement("h"+level);
			event.getModifier().decorate(elm);  // TODO: Doesn't work?

			// Parse the heading content (e.g "New **Java** //library// - `JTexy`").
			new TexyLineParser(getTexy(), elm).parse( event.getText() );

			HeadingModule.this.ctx.toc.add( new HeadingInfo(elm, level, event.isSurrounded));

			return elm;
		}
	};





	/**
	 * BeforeParseEvent - reset internal state. TODO: Move elsewhere (some ParsingContext).
	 */
	private BeforeParseListener beforeParse = new BeforeParseListener() {
		@Override public Class getEventClass() { return BeforeParseEvent.class; }

		@Override public Node onEvent(TexyEvent event_) throws TexyException {
			BeforeParseEvent event = (BeforeParseEvent) event_;
			ctx.documentTitle = null;
			ctx.toc = new ArrayList<HeadingInfo>();
			ctx.usedIDs = new HashSet<String>();
			return null;
		}
	};



	/**
	 * AfterParseEvent -
	 */
	private AfterParseListener afterParse = new AfterParseListener() 
    {
		@Override public Class getEventClass() { return AfterParseEvent.class; }

		@Override public Node onEvent(TexyEvent event_) throws TexyException {
			AfterParseEvent event = (AfterParseEvent) event_;

			if( event.isSingleLine ) return null;


			// Sesypat underlined levely na hromadu, aby šly po sobě po jedné.
			if( opt.balancing == Options.Balancing.DYNAMIC  ) {
                balance( ctx.toc, ctx.topLevel );
			}


			// Generate IDs (where not set yet).
			if( opt.generateIDs ){
				int tocUID = 0;
				for( HeadingInfo item : ctx.toc ) {
					if( item.elm.getAttribute("id").length() != 0 ) continue;

					// TBD: Texy! has toText() here, which would mean to call
					// ProtectedHTMLWriter.fromElement(item.elm, getTexy().getProtector());
					// Isn't it too expensive? Is this safe?
					String titleText = item.elm.getTextTrim();
					if( "".equals( titleText ) ) continue;

					item.titleText = titleText;
                    
                    // ID HTML attribute.
                    // TODO: unprotectAll(titleText) to get full text.
                    log.finer( Debug.showCodes(titleText) );
                    String titleUnprot = event.getParser().getTexy().getProtector().unprotectAll(titleText);

                    String idAttr = StringUtils.defaultString(opt.idPrefix)
                            + JTexyStringUtils.webalize(titleUnprot);
                    
                    if( ctx.usedIDs.contains(idAttr) )
                        idAttr += (++tocUID); 
                    
					item.id  = idAttr;
					item.elm.setAttribute( "id", item.id );
					
				}// for each heading
			}// Generate IDs.

			
			// Document title. TBD: Move to (future) ParsingContext.
			if( ctx.documentTitle == null && ctx.toc.size() != 0 ){
				HeadingInfo item = ctx.toc.get(0);
                ctx.documentTitle = StringUtils.defaultString( item.titleText, item.elm.getTextTrim() );
			}

			return null;
		}// onEvent()
	};// AfterParseListener afterParse{}

    

    /**
     *  Balances the levels of underlined headings to make them ascending sequentially.
     */
    private static void balance( List<HeadingInfo> toc_, int topLevel_ ){
        int top = topLevel_;
        int min = 100;
        int max = 0;
        SortedSet<Integer> set = new TreeSet();
        int[] map = null;

        // Get the min, max bounds.
        for( HeadingInfo headingInfo : toc_ ) {
            if( headingInfo.type == Type.SURROUNDED ){
                min = Math.min( min, headingInfo.level );
                top = topLevel_ - min;
            }
            else if( headingInfo.type == Type.UNDERLINED ) {
                set.add( headingInfo.level );
                max = Math.max( max, headingInfo.level );
            }
        }
        // Beware: For each level used, there must be a match.
        map = new int[max+1];
        int realLevel = 0;
        for( Integer level : set )
            map[level] = realLevel++;

        // Determine the dynamic level of each heading.
        for( HeadingInfo item : toc_ ) {
            if( item.type == Type.SURROUNDED )
                item.level = item.level + top;
            else if( item.type == Type.UNDERLINED )
                item.level = map[item.level] + topLevel_;

            item.elm.setName("h"+item.level);
        }
    }// balance()


    
	
	protected enum Type { UNDERLINED, SURROUNDED; }

	protected static class HeadingInfo {
		protected DOMElement elm;
		protected int level;
		protected Type type;
		protected String titleText;
		protected String id;

		public HeadingInfo(DOMElement elm, int level, Type type) {
			this.elm = elm;
			this.level = level;
			this.type = type;
		}

		private HeadingInfo(DOMElement elm, int level, boolean surrounded) {
			this.elm = elm;
			this.level = level;
			this.type = surrounded ? Type.SURROUNDED : Type.UNDERLINED;
		}
	}



}
