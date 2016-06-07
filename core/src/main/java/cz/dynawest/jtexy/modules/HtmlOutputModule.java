package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.dtd.Dtd;
import cz.dynawest.jtexy.dtd.DtdElement;
import cz.dynawest.jtexy.dtd.HtmlDtdTemplate;
import cz.dynawest.jtexy.events.PostProcessEvent;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMText;


/**
 * Fix HTML structure, force conformance to DTD.
 * 
 * @author Ondrej Zizka
 */
public class HtmlOutputModule extends TexyModule
{
    private static final Logger log = Logger.getLogger( HtmlOutputModule.class.getName() );

	// -- Module meta-info -- //

	@Override public TexyEventListener<PostProcessEvent>[] getEventListeners() {
		return new TexyEventListener[]{postProcessListener};
	}

	@Override	protected PatternHandler getPatternHandlerByName(String name) {
        return null;
	}

    @Override protected String getPropsFilePath() {
        return null; // No props file.
    }
    
    

    
    
    // -- Config --
    
	/** Indent HTML code? */
	public boolean indent = true;

	/** Base indent level. */
	public int baseIndent = 0;

	/** Wrap width, doesn't include indent space. */
	public int lineWrapWidth = 80;

	/** Remove optional HTML end tags? */
	public boolean removeOptional = false;

	/** Output XML? */
	private boolean isXml;
    

    // -- Context --
	/** Indent space counter. */
	private int space;

	/**  */
	private CounterMap tagUsedCount;

	/**  */
	private Stack<StackItem> tagStack;

	/** Content DTD used, when context is not defined. */
	private DtdElement baseDTD;

    private HtmlDtdTemplate htmlDTD = new HtmlDtdTemplate();
    
    
    
    
    /**
     *   TODO.
     * 
     *  Converts <strong><em> ... </strong> ... </em>.
     *  into     <strong><em> ... </em></strong><em> ... </em>.
     *  And other neat tricks.
     */
    private final TexyEventListener<PostProcessEvent> postProcessListener =
            new  TexyEventListener<PostProcessEvent>() 
    {
        @Override public Class getEventClass() { return PostProcessEvent.class; }

        @Override public Node onEvent(PostProcessEvent event) throws TexyException {
            return new DOMText(HtmlOutputModule.this.reset().postProcess(event.getText()));
        }
    };
    
    
    /**
     *  Reset for the new invocation.
     */
    private HtmlOutputModule reset() {
		this.space = this.baseIndent;
		this.tagStack = new Stack<StackItem>(){
            @Override
            public synchronized StackItem peek() {
                if( this.isEmpty() ) return null;
                return super.peek();
            }
        };
		this.tagUsedCount  = new CounterMap();
		this.isXml = false; //texy.getOutputMode() & Constants.XML;
        
        // Special "base content" - Whatever can be in <div>, <html>, <body>. Plus <html> itself.

		this.baseDTD = new DtdElement("root");
        this.baseDTD.addAll( htmlDTD.getRootDtdElement().getElement("div").getElements() );
        this.baseDTD.addAll( htmlDTD.getRootDtdElement().getElement("html").getElements() );
        //this.baseDTD.addAll( htmlDTD.getDtd().getElement("head").getElements() );
        this.baseDTD.addAll( htmlDTD.getRootDtdElement().getElement("body").getElements() );
        this.baseDTD.add( htmlDTD.getRootDtdElement().getElement("html") );
        
        return this;
    }
    
    
    
    /**
     *   TODO.
     * 
     *  Converts <strong><em> ... </strong> ... </em>.
     *  into     <strong><em> ... </em></strong><em> ... </em>.
     *  And other neat tricks.
     */
    private String postProcess( String str ){
        
        this.reset();

        // Wellform and reformat each element.
        /* @
		str = preg_replace_callback(
			'#(.*)<(?:(!--.*--)|(/?)([a-z][a-z0-9._:-]*)(|[ \n].*)\s*(/?))>()#Uis',
			array($this, 'cb'),
			str . '</end/>'
		);*/
        {
            StringBuffer sb = new StringBuffer( str.length() * 12 / 10 );
            Pattern pat = Pattern.compile("(?Uis)(.*)<(?:(!--.*--)|(/?)([a-z][a-z0-9._:-]*)(|[ \\n].*)\\s*(/?))>()");
            Matcher mat = pat.matcher(str);
            while( mat.find() ){
                String replacement = callback( mat, this.htmlDTD.getDtd() );
                mat.appendReplacement(sb, replacement);
            }
            // TODO: Does the loop do the last part?
        }
        
        

		// Empty the stack.
		for( StackItem elm : this.tagStack)
            str += "</" + elm.tag + ">"; //["close"];

		// Right trim.
		str = str.replaceAll("[\t ]+(\n|\r|$)", "$1");

		// Join double \r to single \n.
		str = str.replace("\r\r", "\n");
		str = str.replace('\r', '\n');

		// Greedy chars.
		str = str.replaceAll("\u0007 *", "");
		// Back-tabs.
		str = str.replaceAll("\\t? *\u0008", "");

		// Line wrap.
		if( this.lineWrapWidth > 0) {
			// @ str = preg_replace_callback( "#^(\t*)(.*)$#m",  array($this, "wrap"), str );
            Pattern pat = Pattern.compile("(?m)^(\\t*)(.*)$");
            Matcher mat = pat.matcher(str);
            while( mat.find() ){
                
            }
            // TODO
		}

		// Remove HTML 4.01 optional end tags.
		if( !this.isXml && this.removeOptional) {
			str = str.replaceAll("(?u)\\s*</(colgroup|dd|dt|li|option|p|td|tfoot|th|thead|tr)>", "");
		}
        
        return str;
    }
    
    
    
    static class StackItem {
        String tag;
        String open;
        String close;
        int indent;
        Set<DtdElement> dtdContent;

        public StackItem(String tag, String open, String close, Set<DtdElement> dtdContent, int indent) {
            this.tag = tag;
            this.open = open;
            this.close = close;
            this.indent = indent;
            this.dtdContent = dtdContent;
        }

        @Override public String toString() {
            return "StackItem{ tag=" + tag + ", indent=" + indent + ", open=" + open + ", close=" + close + ", dtdContent{" + dtdContent.size() + "}}";
        }
    }
    
    static class CounterMap {
        Map<String, Integer> map = new HashMap();
        
        public Integer get(String key){ return this.map.get(key); }
        public boolean used(String key){ return this.get(key, 0) != 0; }
        public boolean notUsed(String key){ return this.get(key, 0) == 0; }
        public int get(String key, int def){ 
            Integer val = this.map.get(key);
            return null == val ? def : val;
        }
        public int increment(String key){ return adjust(key,  1); }
        public int decrement(String key){ return adjust(key, -1); }
        public int adjust(String key, int delta){
            Integer val = this.map.get(key);
            if( val == null ) val = delta;
            else val += delta;
            this.map.put(key, val);
            return val;
        }
    }
    

	/**
	 * Callback function: <tag> | </tag> | ....
	 * 
     * TODO: Change string to StringBuilder.
	 */
	private String callback( Matcher matcher, Dtd dtd )
	{
		// html tag
		// list(, mText, $mComment, $mEnd, $mTag, $mAttr, $mEmpty) = $matches;
		//    [1] => text
		//    [1] => !-- comment --
		//    [2] => /
		//    [3] => TAG
		//    [4] => ... (attributes)
		//    [5] => /   (empty)
        
        String mText = matcher.group(1);
        String mComment = matcher.group(2);
        String mEnd = matcher.group(3);
        String mTag = matcher.group(4);
        String mAttr = matcher.group(5);
        String mEmpty = matcher.group(6);
        
        boolean bEndTag = "/".equals(mEnd);

		String str = "";

		// Phase #1 - stuff between tags.
		if( ! mText.isEmpty() ) {
            StackItem item = this.tagStack.peek(); // @ reset()
            DtdElement elm;
			// Text not allowed?
			if( item != null  &&  (elm = dtd.get(item.tag)) != null  &&  elm.getElement("%DATA") != null) { }

			// inside pre & textarea preserve spaces
            else if( this.tagUsedCount.get("pre", 0) != 0 
                  || this.tagUsedCount.get("textarea",0) != 0
                  || this.tagUsedCount.get("script", 0) != 0 )
				str = JTexyStringUtils.freezeSpaces(mText);

			// otherwise shrink multiple spaces
			else str = mText.replaceAll("[ \n]+", " ");
		}


		// Phase #2 - HTML comment.
		if( StringUtils.isNotEmpty(mComment) ) 
            return str + "<" + JTexyStringUtils.freezeSpaces(mComment) + ">";


		// Phase #3 - HTML tag.    // (empty means contains "/")
        boolean bEmpty = !mEmpty.isEmpty() || (dtd.contains(mTag) && dtd.get(mTag).hasNoChildren());
		if( bEmpty && bEndTag ) 
            return str; // Wrong tag: </tag/>


		if( bEndTag ) {  // End tag.

			// Has a start tag?
			if( 0 == this.tagUsedCount.get(mTag, 0))   // @ empty(this.tagUsed[$mTag])
                return str;

			// Autoclose the tags.
			Stack<StackItem> tmpStack = new Stack();
			boolean back = true;
            for( Iterator<StackItem> it = this.tagStack.iterator(); it.hasNext(); ) {
                StackItem item = it.next();
				String stackTag = item.tag;
				str += item.close;
				this.space -= item.indent;
                this.tagUsedCount.decrement( stackTag );
				back = back && htmlDTD.getInlineElements().contains( new DtdElement(stackTag) );  // isset(TexyHtml::$inlineElements[$tag]);
				it.remove(); // Otherwise ConcurrentModificationException. //this.tagStack.remove(item); // unset(this.tagStack[$i]);
				if( mTag.equals( stackTag ))  break;
				tmpStack.push(item); // array_unshift($tmp, $item);
			}

            //@ if (!$back || !$tmp) return $s;
			if(  (!back) || tmpStack.isEmpty() )  return str;

			// Allowed-check (nejspis neni ani potreba)
            {
                StackItem item = this.tagStack.peek();
                Set<DtdElement> dtdContent = ( item != null ) ? item.dtdContent : this.baseDTD.getElements();
                if( ! dtdContent.contains( new DtdElement(tmpStack.firstElement().tag) ) ) return str;  // TODO:  Change dtdContent to DtdElement.
            }

			// Autoopen tags.
			for( StackItem item : tmpStack )
			{
				str += item.open;
				this.space += item.indent;
				this.tagUsedCount.increment(item.tag);
				this.tagStack.push(item); // array_unshift(this.tagStack, $item);
			}


		} else { // start tag

			Set<DtdElement> dtdContent = this.baseDTD.getElements();
            
            boolean allowed;

            // Unknown (non-html) tag.
			if( ! this.htmlDTD.getDtd().contains( mTag )) {
				allowed = true;
				StackItem item = this.tagStack.peek(); // @ reset()
				if( item != null ) dtdContent = item.dtdContent;
			} 

            // HTML tag.
            else {
				// Optional end tag closing.
				//for( StackItem item : this.tagStack )  // $i => $item
                for( Iterator<StackItem> it = this.tagStack.iterator(); it.hasNext(); ) {
                    StackItem item = it.next();
                    
					// Is tag allowed here?
					dtdContent = item.dtdContent;
					if( dtdContent.contains( new DtdElement(mTag) ) )  break;

					String tag = item.tag;
                    // Auto-close hidden, optional and inline tags.
                    DtdElement tagDtd = new DtdElement(tag);
					if( (null != item.close)
                        && ! this.htmlDTD.getOptionalEndElements().contains(tagDtd)
                        && ! this.htmlDTD.getInlineElements().contains(tagDtd)
                    )  break;

					// Close it.
					str += item.close;
					this.space -= item.indent;
					this.tagUsedCount.decrement(tag);
					//this.tagStack.remove(item); // unset(this.tagStack[$i]);
                    it.remove();
					dtdContent = this.baseDTD.getElements();
				}

				// Is tag allowed in this content?
				allowed = dtdContent.contains( new DtdElement(mTag) );

				// Check deep element prohibitions.
				if( allowed ) {
                    Set<DtdElement> prohibs = this.htmlDTD.getDtd().getProbibitionsOf(new DtdElement(mTag) );
                    if( prohibs != null )
                    for( DtdElement dtdElement : prohibs ) {
                        if( 0 == this.tagUsedCount.get(dtdElement.getName(), 0) ){
                            allowed = false;
                            break;
                        }
                    }
				}
			}// else (is an HTML element)
            

			// Empty elements se neukladaji do zasobniku.
			if( !mEmpty.isEmpty() ) {
				if( ! allowed ) return str;

				if( this.isXml ) mAttr += " /";

				boolean indent_ = this.indent &&  this.tagUsedCount.notUsed("pre") && this.tagUsedCount.notUsed("textarea");

                int len = str.length() + mTag.length() + mAttr.length() + 5 + Math.max(0, this.space); // max() is a quick fix, indentation still broken.
				if( indent && "br".equals(mTag) ){
					// Formatting exception
					StringBuilder sb = new StringBuilder(len);
                    sb.append( str.replaceFirst("\\s+$", "") );
                    sb.append('<') .append(mTag).append(mAttr).append(">\n");
                    for( int i = 0; i < this.space; i++)  sb.append("\t");
                    return sb.append("\u0007").toString();
                }
                
				if( indent && ! this.htmlDTD.getInlineElements().contains(new DtdElement(mTag)) ) {
                    StringBuilder sb = new StringBuilder(len).append("\r");
                    sb.append(str);
                    for( int i = 0; i <= this.space; i++)  sb.append("\t"); 
					sb.append('<').append(mTag).append(mAttr).append('>');
                    for( int i = 0; i <= this.space; i++)  sb.append("\t"); 
                    return sb.toString();
				}

				return new StringBuilder(len).append(str).append('<').append(mTag).append(mAttr).append('>').toString();
			}

			String open = null;
			String close = null;
			int indent_ = 0;

			/* // @ Commented out in Texy
			if( !isset(TexyHtml::$inlineElements[$mTag])) {
				// block tags always decorate with \n
				str += "\n";
				$close = "\n";
			}
			*/

			if( allowed ) {
				open = new StringBuilder(2+mTag.length()+mAttr.length()).append('<').append(mTag).append(mAttr).append('>').toString();

				// Receive new content (ins & del are special cases). TBD: Move the exception to DTD?
                DtdElement elDtd = this.htmlDTD.getDtd().get(mTag);
                if( elDtd.hasChildren() && !"ins".equals(mTag) && !"del".equals(mTag))
                    dtdContent = elDtd.getElements();

				// Format output (if indent is enabled and it's not inline element).
				if( this.indent && ! this.htmlDTD.getInlineElements().contains( new DtdElement(mTag) ) )
                {
                    // Create indented close tag for this element.
                    close = new StringBuilder(6+mTag.length()+this.space)
                            .append("\u0008</").append(mTag).append(">\n").append( StringUtils.repeat("\t", this.space)).toString();
                    // Print indented open tag.
					str +=  new StringBuilder(3+open.length()+this.space)
                            .append('\n').append( StringUtils.repeat("\t", this.space++)).append(open).append("\u0007").toString();
					indent_ = 1;
				} else {
                    // Create plain close tag.
					close = new StringBuilder(3+mTag.length()).append("</").append(mTag).append('>').toString(); //"</mTag>";
                    // Print plain open tag.
					str += open;
				}

				// TODO: problematic formatting of select / options, object / params
			}


			// Open tag, put to stack, increase counter
			StackItem item = new StackItem( mTag, open, close, dtdContent, indent_ );
			this.tagStack.push(item); // array_unshift()
            this.tagUsedCount.increment(mTag);
		}

		return str;
	}// callback()



	/**
	 * Callback function: wrap lines.
	 */
	private String wrap( String space, String str ){
		return space + WordUtils.wrap(str, this.lineWrapWidth, "\n" + space, false);
	}

}// class HtmlModule
