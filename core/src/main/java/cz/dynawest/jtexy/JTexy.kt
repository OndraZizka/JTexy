package cz.dynawest.jtexy

import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter
import cz.dynawest.jtexy.events.*
import cz.dynawest.jtexy.modules.*
import cz.dynawest.jtexy.parsers.*
import cz.dynawest.jtexy.util.*
import org.apache.commons.lang.StringUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.util.logging.*

/**
 * Texy! - Convert plain text to XHTML format using [].
 *
 * `
 * $texy = new Texy();
 * $html = $texy.process($text);
` *
 *
 * @author Ondrej Zizka
 * @copyright  Copyright (c) 2004, 2008 David Grudl
 * @package    Texy
 */
class JTexy {
    // Options
    val options = Options()

    // Modules //
    // TBD: A dynamic map of modules, see #loadModules() and TexyModule#onRegiser().
    var linkModule: LinkModule? = null
    var phraseModule: PhraseModule? = null
    var paragraphModule: ParagraphModule? = null
    private var blockModule: BlockModule? = null
    var imageModule: ImageModule? = null
        private set
    private var headingModule: HeadingModule? = null
    private var horizLineModule: HorizontalLineModule? = null
    var listModule: ListModule? = null
    private var typoModule: TypographyModule? = null
    private var htmlOutputModule: HtmlOutputModule? = null

    /** Normal Handlers (incl. BeforeAfterHandler's).  */
    var normalHandlers: HandlersMap<TexyEventListener<TexyEvent>> = HandlersMap()
        protected set

    /** Around handlers.  */
    var aroundHandlers: HandlersMap<AroundEventListener<AroundEvent>> = HandlersMap()
        protected set

    /**
     * Initialization.
     */
    @Throws(TexyException::class)
    fun init() {

        // The order is vital - the modules' handlers are registered in this order.
        var module: TexyModule?

        // line parsing
        //module = this.scriptModule = new ScriptModule();
        //module = this.htmlModule = new HtmlModule();
        imageModule = ImageModule()
        module = imageModule
        registerModule(module)
        phraseModule = PhraseModule()
        module = phraseModule
        registerModule(module)
        linkModule = LinkModule()
        module = linkModule
        registerModule(module)
        //module = this.emoticonModule = new EmoticonModule();  this.registerModule( module );

        // block parsing
        paragraphModule = ParagraphModule()
        module = paragraphModule
        registerModule(module)
        blockModule = BlockModule()
        module = blockModule
        registerModule(module)
        //module = this.figureModule = new FigureModule();            this.registerModule( module );
        horizLineModule = HorizontalLineModule()
        module = horizLineModule
        registerModule(module)
        //module = this.blockQuoteModule = new BlockQuoteModule();    this.registerModule( module );
        //module = this.tableModule = new TableModule();              this.registerModule( module );
        headingModule = HeadingModule()
        module = headingModule
        registerModule(module)
        listModule = ListModule()
        module = listModule
        registerModule(module)

        // post process
        typoModule = TypographyModule()
        module = typoModule
        registerModule(module)
        //module = this.longWordsModule = new LongWordsModule();      this.registerModule( module );
        htmlOutputModule = HtmlOutputModule()
        module = htmlOutputModule
        registerModule(module) // TODO: Verify
    }

    // -- Registered patterns. -- //
    var linePatterns: MutableList<RegexpInfo> = ArrayList(50)
    var blockPatterns: MutableList<RegexpInfo> = ArrayList(50)


    // PostLine patterns not needed here.
    fun addPattern(ri: RegexpInfo) {
        when (ri.type) {
            RegexpInfo.Type.LINE -> linePatterns.add(ri)
            RegexpInfo.Type.BLOCK -> blockPatterns.add(ri)
            RegexpInfo.Type.POST_LINE -> {}
        }
    }

    /** What is allowed.
     * Will it always be a module? If not, perhaps that option should be in respective module.
     */
    private val allowedMap: MutableMap<String, Boolean> = HashMap()
    fun isAllowed(what: String): Boolean {
        val isAllowed = allowedMap[what]
        return null == isAllowed || isAllowed // Allowed by default.
    }

    fun setAllowed(what: String, isAllowed: Boolean) {
        allowedMap[what] = isAllowed
    }

    // Convenience methods.  what <? extends TexyModule|TexyParserEvent>.
    fun isAllowed(what: Class<*>?): Boolean {
        val isAllowed = allowedMap[what!!.name]
        return null == isAllowed || isAllowed // Allowed by default.
    }

    fun setAllowed(what: Class<*>, isAllowed: Boolean) {
        allowedMap[what.name] = isAllowed
    }

    /**
     * Registers given module.
     * @param module
     * @param handler
     */
    @Throws(TexyException::class)
    fun registerModule(module: TexyModule? /*, PatternHandler handler */) {
        log.fine("Registering module " + module!!.javaClass.name + "...")

        // This object's backref.
        module.texy = this // TBD: Change onRegister to onRegister( JTexy texy );

        // Module's callback - usually adds listeners.
        module.onRegister()


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
     * Process the text.
     */
    /**
     * Process the text. singleLine = false.
     */
    @JvmOverloads
    @Throws(TexyException::class)
    fun process(text: String, isSingleLine: Boolean = false): String {

        // Remove soft hyphens
        var text = text
        if (options.removeSoftHyphens) text = text.replace("\uC2AD", "")

        // Standardize line endings and spaces.
        text = JTexyStringUtils.normalize(text)

        // Replace tabs with spaces. BufferedReader?
        //text = JTexyStringUtils.expandTabs( text, this.options.tabWidth );


        // Create something to hold the result in.
        //Document dom = DocumentFactory.getInstance().createDocument( new DOMElement("html") );
        val elm = DOMElement(Constants.HOLDER_ELEMENT_NAME)

        // Create the parser.
        val parser = if (isSingleLine) TexyLineParser(this, elm) else TexyBlockParser(this, elm, false)

        // Invoke BeforeParseListener's.
        invokeNormalHandlers(BeforeParseEvent(parser, text))

        // Parse.
        parser.parse(text)

        // Invoke AfterParseListener's.
        invokeNormalHandlers(AfterParseEvent(parser, isSingleLine, elm))
        val texyString: String = ProtectedHTMLWriter.Companion.fromElement(elm, protector)
        var htmlCode = stringToHtml(texyString)

        // Hack, perhaps do systemicaly later?
        htmlCode = htmlCode!!.replace("<br/>", "<br/>\n")
        htmlCode = htmlCode.replace("<br />", "<br />\n")
        return htmlCode
    } // process()

    /**
     * Converts internal string representation (protected) to a HTML code.
     */
    @Throws(TexyException::class)
    private fun stringToHtml(texyString: String): String {

        // Decode HTML entities (just the basic set of < > &).
        var texyString: String = texyString
        texyString = JTexyStringUtils.unescapeHtml(texyString)
        log.finer("Before AfterLineEvent: " + Debug.showCodes(texyString))
        val blocks = StringUtils.splitPreserveAllTokens(texyString, ContentType.BLOCK.delimAsString)

        val handlers: List<TexyEventListener<AfterLineEvent>> = normalHandlers.getHandlersForEvent(AfterLineEvent::class.java)

        for (handler in handlers) {
            if (this.isAllowed(handler.eventClass)) continue
            for (i in blocks.indices) {
                val block = blocks[i]

                // WTF?  Probably 0 == outside of a block, 1 = inside (they can't be nested?)
                if (i % 2 == 0 && block.length != 0) {
                    // "Fire" the event.
                    val event = AfterLineEvent(DummyTexyParser, block)
                    blocks[i] = handler.onEvent(event)?.text
                }
            }
        }
        /**/texyString = StringUtils.join(blocks, ContentType.BLOCK.delim)
        log.finer("After  AfterLineEvent: " + Debug.showCodes(texyString))


        // Encode < > &.
        texyString = JTexyStringUtils.escapeHtml(texyString)

        // Until HtmlOutputModule is done. But it won't help?
        texyString = texyString.replace("\u00A0", "&nbsp;")

        //log.finer(Debug.showCodes(texyString));

        // Replace protected marks.
        val htmlCode = getProtector().unprotectAll(texyString)
        log.finer("After unprotectAll(): " + Debug.showCodes(htmlCode))

        // Wellformat and reformat HTML.
        // @ Only handled in HtmlOutputModule.
        // TODO: $this->invokeHandlers('postProcess', array($this, & $s));
        invokeNormalHandlers(PostProcessEvent(this, htmlCode))
        log.finer("After PostProcessEvent: " + Debug.showCodes(htmlCode))

        // Unfreeze spaces.
        // DONE: $s = self::unfreezeSpaces($s);
        JTexyStringUtils.unfreezeSpaces(htmlCode)
        return htmlCode
    } // stringToHtml()

    // String protection //
    var protector = ProtectorArray()
    @Throws(TexyException::class)
    fun unprotect(key: String): String {
        return protector.unprotectAll(key)
    }

    //public String protect(String str) {		return protector.protect(str);	}
    fun protect(str: String, contentType: ContentType): String {
        return protector.protect(str, contentType)
    }

    fun getProtector(): Protector {
        return protector
    }

    /**
     * This is for all except AroundEvent.
     */
    @Throws(TexyException::class)
    fun invokeNormalHandlers( /*BeforeAfterEvent*/
                              event: TexyEvent
    ) {
        val handlers = normalHandlers.getHandlersForEvent(event)
        if (null == handlers) {
            log.warning("No handlers for event: $event")
            return
        }
        val exceptions: MutableList<TexyException> = ArrayList()

        // For each handler...
        for (handler in handlers) {
            try {
                handler!!.onEvent(event)
            } catch (ex: TexyException) {
                exceptions.add(ex)
            }
        }
        TexyException.throwIfErrors(
            "Errors while invoking normal handlers for event {$event}:", exceptions
        )
    }

    /**
     * Has the invocation chain.
     *
     * @returns null if no Handlers, or a Node created in a handler.
     */
    @Throws(TexyException::class)
    fun invokeAroundHandlers(event: AroundEvent): Node? {
        val handlers = aroundHandlers.getHandlersForEvent(event)
        if (null == handlers) {
            log.warning("No handlers for event: $event")
            return null
        }
        return Invocation(event, handlers).proceed()
    }

    /**
     * Dynamically loads the modules from the given package.
     * Creates an instance of each class inherited from #TexyModule.
     *
     * Not used yet.
     *
     * @throws TexyException when anything bad happens.
     */
    @Throws(TexyException::class)
    protected fun loadModules(packageName: String) {
        log.fine("Loading modules from package: $packageName")
        val classesForPackage: Array<Class<*>>
        classesForPackage = try {
            JavaUtils.getClassesForPackage(packageName)
        } catch (ex: ClassNotFoundException) {
            throw TexyException("Package not found: $packageName")
        }
        val exceptions: MutableList<Exception> = ArrayList()

        for (clazz in classesForPackage) {
            log.finer("  Processing class: " + clazz.name)

            // Only care about instances of TexyModule.
            if (!TexyModule::class.java.isAssignableFrom(clazz)) continue
            var module: TexyModule = try {
                clazz.newInstance() as TexyModule
            } catch (ex: InstantiationException) {
                exceptions.add(ex)
                continue
            } catch (ex: IllegalAccessException) {
                exceptions.add(ex)
                continue
            }
            module.texy = this
            registerModule(module)
        }

        // Report exceptions. TODO: TexyMultiException
        if (exceptions.size > 0) {
            val sb = StringBuilder("Errors while loading '$packageName':")
            for (ex in exceptions) {
                sb.append("\n  ").append(ex.message)
            }
            throw TexyException(sb.toString())
        }
    }

    /** Settings convenience.  */
    fun setImagesUrlPrefix(str: String?): JTexy {
        imageModule!!.urlPrefix = str!!
        return this
    }

    companion object {
        private val log = Logger.getLogger(JTexy::class.java.name)

        /** Intended to turn off logging etc.  */
        var debug = true

        /**
         * Creates a JTexy instance and initializes it.
         * The "official" way to create a JTexy object.
         */
        @JvmStatic
        @Throws(TexyException::class)
        fun create(): JTexy {
            val jtexy = JTexy()
            jtexy.init()
            return jtexy
        }
    }
}
