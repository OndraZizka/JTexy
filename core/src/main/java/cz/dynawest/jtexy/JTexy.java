package cz.dynawest.jtexy;

import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter;
import cz.dynawest.jtexy.modules.*;
import cz.dynawest.jtexy.parsers.AfterParseEvent;
import cz.dynawest.jtexy.parsers.AroundEvent;
import cz.dynawest.jtexy.parsers.AroundEventListener;
import cz.dynawest.jtexy.parsers.BeforeParseEvent;
import cz.dynawest.jtexy.events.PostProcessEvent;
import cz.dynawest.jtexy.events.TexyEvent;
import cz.dynawest.jtexy.parsers.TexyBlockParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.parsers.TexyLineParser;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.util.Debug;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.jtexy.util.JavaUtils;
import java.util.*;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;




/**
 * Texy! - Convert plain text to XHTML format using {@link process()}.
 *
 * <code>
 *     $texy = new Texy();
 *     $html = $texy.process($text);
 * </code>
 *
 * @author Ondrej Zizka
 * @copyright  Copyright (c) 2004, 2008 David Grudl
 * @package    Texy
 */
public class JTexy {
    private static final Logger log = Logger.getLogger( JTexy.class.getName() );

    /** Intended to turn off logging etc. */
    public static boolean debug = true;


    /**
     * Creates a JTexy instance and initializes it.
     * The "official" way to create a JTexy object.
     */
    public static JTexy create() throws TexyException {
        JTexy jtexy = new JTexy();
        jtexy.init();
        return jtexy;
    }

    // Options
    public final Options options = new Options();


    // Modules //
    // TBD: A dynamic map of modules, see #loadModules() and TexyModule#onRegiser().
    public LinkModule linkModule;
    public PhraseModule phraseModule;
    public ParagraphModule paragraphModule;
    private BlockModule blockModule;
    private ImageModule imageModule;
    private HeadingModule headingModule;
    private HorizontalLineModule horizLineModule;
    public ListModule listModule;
    
    private TypographyModule typoModule;
    private HtmlOutputModule htmlOutputModule;



    /** Normal Handlers (incl. BeforeAfterHandler's). */
    protected HandlersMap<TexyEventListener> normalHandlers = new HandlersMap();
    public HandlersMap<TexyEventListener> getNormalHandlers(){ return normalHandlers; }


    /** Around handlers. */
    protected HandlersMap<AroundEventListener> aroundHandlers = new HandlersMap();
    public HandlersMap<AroundEventListener> getAroundHandlers(){ return aroundHandlers; }



    /**
     * Initialization.
     */
    public void init() throws TexyException
    {

        // The order is vital - the modules' handlers are registered in this order.

        TexyModule module;

        // line parsing
        //module = this.scriptModule = new ScriptModule();
        //module = this.htmlModule = new HtmlModule();
        module = this.imageModule = new ImageModule();      this.registerModule( module );
        module = this.phraseModule = new PhraseModule();    this.registerModule( module );
        module = this.linkModule = new LinkModule();        this.registerModule( module );
        //module = this.emoticonModule = new EmoticonModule();  this.registerModule( module );

        // block parsing
        module = this.paragraphModule = new ParagraphModule();      this.registerModule( module );
        module = this.blockModule = new BlockModule();              this.registerModule( module );
        //module = this.figureModule = new FigureModule();            this.registerModule( module );
        module = this.horizLineModule = new HorizontalLineModule(); this.registerModule( module );
        //module = this.blockQuoteModule = new BlockQuoteModule();    this.registerModule( module );
        //module = this.tableModule = new TableModule();              this.registerModule( module );
        module = this.headingModule = new HeadingModule();          this.registerModule( module );
        module = this.listModule = new ListModule();                this.registerModule( module );

        // post process
        module = this.typoModule = new TypographyModule();          this.registerModule( module );
        //module = this.longWordsModule = new LongWordsModule();      this.registerModule( module );
        module = this.htmlOutputModule = new HtmlOutputModule();    this.registerModule( module ); // TODO: Verify

    }

	


    // -- Registered patterns. -- //

    List<RegexpInfo> linePatterns = new ArrayList(50);
    List<RegexpInfo> blockPatterns = new ArrayList(50);
    public List<RegexpInfo> getLinePatterns()  { return linePatterns; }
    public List<RegexpInfo> getBlockPatterns() { return blockPatterns; }
    
    // PostLine patterns not needed here.
    

    public void addPattern( RegexpInfo ri ){
        switch( ri.type ){
            case LINE:  this.linePatterns.add(ri);  break;
            case BLOCK: this.blockPatterns.add(ri); break;
            case POST_LINE: /* Nothing. */ break;
        }
    }




    /** What is allowed.
     *  Will it always be a module? If not, perhaps that option should be in respective module.
     */
    private Map<String, Boolean> allowedMap = new HashMap();
    
    public boolean isAllowed( String what ) {
        Boolean isAllowed = this.allowedMap.get(what);
        return ( null == isAllowed  || isAllowed ); // Allowed by default.
    }
    public void setAllowed( String what, boolean isAllowed) {
        this.allowedMap.put(what, isAllowed);
    }

    // Convenience methods.  what <? extends TexyModule|TexyParserEvent>.
    public boolean isAllowed( Class what ) {
        Boolean isAllowed = this.allowedMap.get(what.getName());
        return ( null == isAllowed  || isAllowed ); // Allowed by default.
    }
    public void setAllowed( Class what, boolean isAllowed) {
        this.allowedMap.put(what.getName(), isAllowed);
    }




    /**
     * Registers given module.
     * @param module
     * @param handler
     */
    public void registerModule( TexyModule module /*, PatternHandler handler */ ) throws TexyException
    {

        log.fine("Registering module "+module.getClass().getName() + "...");

        // This object's backref.
        module.setTexy(this); // TBD: Change onRegister to onRegister( JTexy texy );

        // Module's callback - usually adds listeners.
        module.onRegister();


        /*
        // Add all module's patterns in the order it gave them. TBD: Move to onRegister().
        OrderedMap<String, RegexpInfo> regexpInfos = module.getRegexpInfos();

        log.fine("  "+regexpInfos.size()+" regexpInfos.");

        //for( RegexpInfo ri : module.getRegexpInfos(). ){
        OrderedMapIterator it = module.getRegexpInfos().orderedMapIterator();
        while( it.hasNext() ){
            RegexpInfo ri = (RegexpInfo)it.next();
            this.addPattern( ri );
        }
        */
    }




    /**
     * Process the text. singleLine = false.
     */
    public String process( String text ) throws TexyException {
        return process(text, false);
    }

    /**
     * Process the text.
     */
    public String process( String text, boolean isSingleLine ) throws TexyException {

        // Remove soft hyphens
        if( this.options.removeSoftHyphens )
            text = text.replace("\uC2AD", "");

        // Standardize line endings and spaces.
        text = JTexyStringUtils.normalize(text);

        // Replace tabs with spaces. BufferedReader?
        //text = JTexyStringUtils.expandTabs( text, this.options.tabWidth );



        // Create something to hold the result in.
        //Document dom = DocumentFactory.getInstance().createDocument( new DOMElement("html") );
        DOMElement elm = new DOMElement( Constants.HOLDER_ELEMENT_NAME );

        // Create the parser.
        TexyParser parser = isSingleLine ? new TexyLineParser( this, elm ) : new TexyBlockParser(this, elm, false);

        // Invoke BeforeParseListener's.
        this.invokeNormalHandlers( new BeforeParseEvent(parser, text) );

        // Parse.
        parser.parse(text);

        // Invoke AfterParseListener's.
        this.invokeNormalHandlers( new AfterParseEvent(parser, isSingleLine, elm) );



        String texyString = ProtectedHTMLWriter.fromElement(elm, protector);

        String htmlCode = this.stringToHtml( texyString );

        // Hack, perhaps do systemicaly later?
        htmlCode = htmlCode.replace("<br/>", "<br/>\n");
        htmlCode = htmlCode.replace("<br />", "<br />\n");

        return htmlCode;

    }// process()




    /**
     *  Converts internal string representation (protected) to a HTML code.
     */
    private String stringToHtml(String texyString) throws TexyException {

        // Decode HTML entities (just the basic set of < > &).
        texyString = JTexyStringUtils.unescapeHtml(texyString);
        
        log.finer("Before AfterLineEvent: " + Debug.showCodes(texyString));
        String[] blocks = StringUtils.splitPreserveAllTokens( texyString, ContentType.BLOCK.getDelimAsString() );
        
        List<TexyEventListener> handlers = this.getNormalHandlers().getHandlersForEvent( AfterLineEvent.class );
        for( TexyEventListener<AfterLineEvent> handler : handlers ){
            if( this.isAllowed( handler.getEventClass() ) )  continue;
            
            for (int i = 0; i < blocks.length; i++) {
                String block = blocks[i];
                
                // WTF?  Probably 0 == outside of a block, 1 = inside (they can't be nested?)
                if( i % 2 == 0  &&  block.length() != 0 ) {
                    // "Fire" the event.
                    blocks[i] = handler.onEvent( new AfterLineEvent( null, block ) ).getText();
                }
            }
        }
        /**/
        texyString = StringUtils.join( blocks, ContentType.BLOCK.getDelim() );
        log.finer("After  AfterLineEvent: " + Debug.showCodes(texyString));


        // Encode < > &.
        texyString = JTexyStringUtils.escapeHtml(texyString);
        
        // Until HtmlOutputModule is done. But it won't help?
        texyString = texyString.replace("\u00A0", "&nbsp;");

        //log.finer(Debug.showCodes(texyString));
        
        // Replace protected marks.
        String htmlCode = this.getProtector().unprotectAll( texyString );
        log.finer("After unprotectAll(): " + Debug.showCodes(htmlCode));

        // Wellformat and reformat HTML.
        // @ Only handled in HtmlOutputModule.
        // TODO: $this->invokeHandlers('postProcess', array($this, & $s));
        this.invokeNormalHandlers( new PostProcessEvent(this, htmlCode) );
        log.finer("After PostProcessEvent: " + Debug.showCodes(htmlCode));

        // Unfreeze spaces.
        // DONE: $s = self::unfreezeSpaces($s);
        JTexyStringUtils.unfreezeSpaces(htmlCode);

        return htmlCode;

    }// stringToHtml()





    // String protection //
    ProtectorArray protector = new ProtectorArray();
    public String unprotect(String key) throws TexyException {		return protector.unprotectAll(key);	}
    //public String protect(String str) {		return protector.protect(str);	}
    public String protect(String str, ContentType contentType) { return protector.protect(str, contentType); }
    public Protector getProtector() {		return protector;	}





    /**
     *   This is for all except AroundEvent.
     */
    public void invokeNormalHandlers( /*BeforeAfterEvent*/ TexyEvent event ) throws TexyException {

        List<TexyEventListener> handlers = this.getNormalHandlers().getHandlersForEvent(event);
        if( null == handlers ){
            log.warning("No handlers for event: "+event);
            return;
        }

        List<TexyException> exceptions = new ArrayList();

        // For each handler...
        for( TexyEventListener handler : handlers ) {
            try {
                handler.onEvent(event);
            } catch (TexyException ex) {
                exceptions.add(ex);
            }
        }

        TexyException.throwIfErrors(
            "Errors while invoking normal handlers for event {"+event+"}:", exceptions );

    }


    /**
     *  Has the invocation chain.
     * 
     *	@returns null if no Handlers, or a Node created in a handler.
     */
    public Node invokeAroundHandlers( AroundEvent event ) throws TexyException
    {
        List<AroundEventListener> handlers = this.getAroundHandlers().getHandlersForEvent(event);
        if( null == handlers ){
            log.warning("No handlers for event: "+event);
            return null;
        }

        return new Invocation( event, handlers ).proceed();
    }




    /**
     * Dynamically loads the modules from the given package.
     * Creates an instance of each class inherited from #TexyModule.
     *
     * Not used yet.
     *
     * @throws TexyException when anything bad happens.
     */
    protected void loadModules( String packageName ) throws TexyException {
        log.fine("Loading modules from package: "+packageName);

        Class[] classesForPackage;
        try {
            classesForPackage = JavaUtils.getClassesForPackage(packageName);
        } catch (ClassNotFoundException ex) {
            throw new TexyException("Package not found: "+packageName);
        }

        List<Exception> exceptions = new ArrayList();

        for( Class clazz : classesForPackage ) {
            log.finer("  Processing class: "+clazz.getName());

            // Only care about instances of TexyModule.
            if( ! TexyModule.class.isAssignableFrom(clazz) )
                continue;

            TexyModule module = null;
            try {
                module = (TexyModule) clazz.newInstance();
            } catch( InstantiationException ex ) {
                exceptions.add(ex);	continue;
            } catch( IllegalAccessException ex ) {
                exceptions.add(ex);	continue;
            }

            module.setTexy(this);
            this.registerModule( module );

        }

        // Report exceptions. TODO: TexyMultiException
        if( exceptions.size() > 0 ){
            StringBuilder sb = new StringBuilder("Errors while loading '"+packageName+"':");
            for (Exception ex : exceptions) {
                sb.append("\n  ").append(ex.getMessage());
            }
            throw new TexyException(sb.toString());
        }

    }// loadModules();

    
    
    
    
    
    public ImageModule getImageModule() {
        return imageModule;
    }
    
    
    /** Settings convenience. */
    public JTexy setImagesUrlPrefix( String str ) {
        this.imageModule.urlPrefix = str;
        return this;
    }
    
    
    

}// class JTexy
